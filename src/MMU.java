import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class MMU {

    /**
     * MAX_VIRT_ADDRESS - Same as maximum physical address, last entry in virtual or physical memory, 4Gb
     * MAX_P1 - Number of entries in the outer page table, from 0 to 1023
     * MAX_P2 - Number of entries in the page of page table (POPT), happens to be the same as MAX_P1 due to 4Kb pages
     * MAX_OFFSET - Number of entries in one frame of physical memory, from 0 to 4095, 4Kb frames
     */
    private final static long MAX_VIRT_ADDRESS = 4294967295L;
    private final static int MAX_P1 = 1023;
    private final static int MAX_P2 = 1023;
    private final static int MAX_OFFSET = 4095;


    //private Registers registers; //MIGHT JUST BE FIELDS IN CPU
    private TLB tlb;
    //private Cache cache;
    private MainMemory memory;

    public MMU() {
        this.tlb = new TLB();
        this.memory = new MainMemory();
    }

    /**
     * Allocates memory for a process.
     * If not enough space in memory, will load what it can into mem and rest into storage
     *
     * @param pid the pid of the process being loaded, stored as key value in processTables
     * @param mem the amount of memory required for this process, expressed in kilobytes
     * @return a success code, 1 for main memory allocated, 0 if data add on storage, -1 if storage was full
     * //TODO Maybe change return value to a ptbr value
     */
    public int loadProc(int pid, int mem) {
        int numPages = mem / 4;
        if ((mem % 4) != 0) {
            numPages++;
        }

        //OLD IMPLEMENTATION, LOADS INTO ALL AVAILABLE PAGES ON MEMORY
        PageTable pTable = new PageTable(numPages);

        if (this.memory.getFreeFrames().size() < numPages) {
            System.out.println("Not enough space to load process: " + pid);
            System.out.println("Needed " + numPages + " frames. Found " + this.memory.getFreeFrames().size());
            int somePages = this.memory.getFreeFrames().size();
            int restPages = numPages - somePages;
            //TODO Implement swapping/virtual memory
            if (this.memory.getStorage().size() + restPages > MainMemory.STORAGE_SIZE) {
                System.out.println("Secondary Storage full");
                return -1;
            } else {
                int freeFrame = -1;
                for (int pNum = 0; pNum < somePages; pNum++) {
                    freeFrame = this.memory.getFreeFrames().poll();
                    pTable.setFrame(pNum, freeFrame);
                    pTable.valids.add(pNum);
                    pTable.setEntryAsValid(pNum, true);
                    this.memory.getFrameTable()[freeFrame] = pid;
                }
                this.memory.getStorage().put(pid, restPages);
                System.out.println("\nStored " + restPages + " pages of PID " + pid + " in storage\n");
                this.memory.getPageTables().put(pid, pTable);
                return 0;
            }
        } else {

            int freeFrame = -1;
            for (int pNum = 0; pNum < numPages; pNum++) {
                freeFrame = this.memory.getFreeFrames().poll();
                pTable.setFrame(pNum, freeFrame);
                pTable.setEntryAsValid(pNum, true);
                this.memory.getFrameTable()[freeFrame] = pid;
            }

            this.memory.getStorage().put(pid, 0);
            this.memory.getPageTables().put(pid, pTable);
            return 1;
        }
    }

    /**
     * Loads a requested page from storage
     * If memory is full, swap
     * else just load and decrement storage count
     *
     * @param pid
     * @param requestPage
     * @return the frame the page was stored in
     */
    public int loadPageFromStorage(int pid, int requestPage) {
        int frame = -1;
        if (this.memory.getFreeFrames().size() == 0) {
            int[] victims = calcVictimPage();
            frame = performSwap(victims[0], victims[1], pid, requestPage);
        } else {
            frame = this.memory.getFreeFrames().poll();
            this.memory.getPageTables().get(pid).setFrame(requestPage, frame);
            this.memory.getPageTables().get(pid).setEntryAsValid(requestPage, true);
            int storeFrames = this.memory.getStorage().get(pid);
            this.memory.getStorage().replace(pid, storeFrames - 1);
            this.memory.getFrameTable()[frame] = pid;
        }
        return frame;
    }

    /**
     * Selects a victim page
     *
     * @return int[0] == pid of process that owns victim page
     * int[1] == victim page
     */
    public int[] calcVictimPage() {
        int victimPid = -1;
        int victimPage = -50;
        int globalNumRef = Integer.MAX_VALUE;
        Iterator throughTables = this.memory.getPageTables().entrySet().iterator();
        while (throughTables.hasNext()) {
            Map.Entry pair = (Map.Entry) throughTables.next();
            int tempPid = (Integer) pair.getKey();
            PageTable tempTable = (PageTable) pair.getValue();
            System.out.println("Checking Pages of PID: " + tempPid);

            int[] victimAndNumRef = tempTable.selectVictim(globalNumRef);
            if (victimAndNumRef[0] >= 0) {
                victimPid = tempPid;
                victimPage = victimAndNumRef[0];
                globalNumRef = victimAndNumRef[1];
            }
        }
        return new int[]{victimPid, victimPage};
    }

    /**
     * Swaps with requested page with victim page
     *
     * @param victimPid
     * @param victimPage
     * @param pid
     * @param requestPage
     * @return
     */
    public int performSwap(int victimPid, int victimPage, int pid, int requestPage) {
        int frame = this.memory.getPageTables().get(victimPid).getFrame(victimPage);
        //Set values for requested page
        this.memory.getPageTables().get(pid).setFrame(requestPage, frame);
        this.memory.getPageTables().get(pid).setEntryAsValid(requestPage, true);
        this.memory.getPageTables().get(pid).resetReference(requestPage);
        this.memory.getFrameTable()[frame] = pid;
        this.memory.getStorage().replace(pid, this.memory.getStorage().get(pid) - 1);

        //Set values for victim page
        this.memory.getPageTables().get(victimPid).setEntryAsValid(victimPage, false);
        this.memory.getPageTables().get(victimPid).setShared(victimPage, false);
        this.memory.getPageTables().get(victimPid).resetReference(victimPage);
        this.memory.getStorage().replace(victimPid, this.memory.getStorage().get(victimPid) + 1);

        return frame;
    }


    /**
     * Creates a valid virtual address from the range of 0 to MAX_VIRT_ADDRESS
     *
     * @return a random virtual address
     * //TODO Possibly change minimum from 0 to account for kernel space?
     */
    public int[] getRandomAddress(int pid) {
        Random random = new Random();
        int numPages = this.memory.getPageTables().get(pid).length();
        int randomPage = RNGesus.randInRange(0, numPages);
        int offset = RNGesus.randInRange(0, 4095);
        int[] logicalAddres = new int[]{pid, 0, randomPage, offset};
        return logicalAddres;
    }

    /**
     * Process a virtual address, breaking it up into P1, P2, and Offset.
     * Converts the address into a string of its binary form, pads left with 0 if less than 32 bits,
     * and calculates P1, P2, and Offset from the first 10 bits, second 10 bits, and last 12 bits respectively.
     *
     * @param pid  the pid of the process requesting the address, just gets sent with the translated virtual address
     * @param addr A virtual address
     * @return Integer array of size 4, named logical
     * logical[0] == pid
     * logical[1] == P1
     * logical[2] == P2
     * logical[3] == Offset
     * @throws IllegalArgumentException if addr is not a valid address
     */
    public static int[] convertVirtualAddress(int pid, long addr) throws IllegalArgumentException {
        if (addr > MAX_VIRT_ADDRESS || addr < 0) {
            throw new IllegalArgumentException("MMU passed invalid virtual address: " + addr);
        }

        String binary = Long.toBinaryString(addr);
        if (binary.length() > 32) {
            throw new IllegalArgumentException("MMU passed invalid virtual address: " + addr);
        }

        while (binary.length() < 32) {
            binary = "0" + binary;
        }

        String p1Bin = binary.substring(0, 10);
        String p2Bin = binary.substring(10, 20);
        String offsetBin = binary.substring(20, 32);

        int p1 = Integer.parseInt(p1Bin, 2);
        int p2 = Integer.parseInt(p2Bin, 2);
        int offset = Integer.parseInt(offsetBin, 2);

        //Verify all values are valid
        if (p1 > MAX_P1 || p2 > MAX_P2 || offset > MAX_OFFSET) {
            throw new IllegalArgumentException("MMU passed invalid virtual address: " + addr);
        }

        int[] logical = new int[4];
        logical[0] = pid;
        logical[1] = p1;
        logical[2] = p2;
        logical[3] = offset;

        return logical;
    }

    /**
     * Takes in a logical address returned by convertVirtualAddress(int pid, long addr), if not in memory, attempts to load from storage.
     * If cannot load from storage for some reason, return -1 for physical address and frame
     *
     * @param logical
     * @return an array where:
     * long[0] = physical address
     * long[1] = frame
     */
    private long[] processAddress(int[] logical) {

        int pid = logical[0];
        int p1 = logical[1];
        int p2 = logical[2];
        int offset = logical[3];
        int frame = -1;

        if (!memory.getPageTables().get(pid).isValid(p2)) {
            frame = loadPageFromStorage(pid, p2);
            if (frame < 0) {
                return new long[]{-1, -1};
            }
        }

        PageTable pTable = memory.getPageTables().get(pid);

        for (int i = 0; i < pTable.length(); i++) {
            int f = pTable.getFrame(i);
            System.out.println("Page: " + i + " Frame: " + f);
        }

        pTable.makeReference(p2);
        frame = pTable.getFrame(p2); //TODO implement double-decker taco page table

        System.out.println("\n" + offset + "\n");

        long physFrame = frame * MainMemory.FRAME_SIZE;
        long phys = physFrame + offset;
        String physBinary = Long.toBinaryString(phys);
        while (physBinary.length() < 32) {
            physBinary = "0" + physBinary;
        }
        System.out.println(physBinary);
        System.out.println(phys);

        return new long[]{phys, frame};
    }

    public long requestAddress(int[] logicalAddress) {
        long tlbHitMis = this.tlb.get(logicalAddress);
        if (tlbHitMis > 0) {
            System.out.println("TLB HIT!! ----- " + tlbHitMis);
            long physFrame = tlbHitMis * MainMemory.FRAME_SIZE;
            long phys = physFrame + logicalAddress[3];
            return phys;
        } else {
            System.out.println("TLB MISS :'( ");
            long[] memoryAccess = this.processAddress(logicalAddress);
            long physicalAddress = memoryAccess[0];
            int returnFrame = (int) memoryAccess[1];
            if (physicalAddress >= 0) {
                this.tlb.swap(logicalAddress, returnFrame);
            } else {
                System.out.println("\n\n\nSomething went horribly wrong");
                return -69;
            }
            return physicalAddress;
        }
    }

    /**
     * Creates child with shared memory space to parent, adds this shared space to end of parent page table
     *
     * @param pid
     * @param ppid
     * @param parentMemory
     * @return
     */
    public int fork(int pid, int ppid, int parentMemory) {
        int numPages = parentMemory / 4;
        if ((parentMemory % 4) != 0) {
            numPages++;
        }
        numPages++;
        System.out.println("Creating child " + pid + " to " + ppid);

        int[] victims = calcVictimPage();
        int frame = this.memory.getPageTables().get(victims[0]).getFrame(victims[1]);
        this.memory.getPageTables().get(ppid).addSharedSpace(frame);
        performSwap(victims[0], victims[1], ppid, numPages);
        this.memory.getStorage().replace(ppid, this.memory.getStorage().get(ppid) + 1);

        int success = loadProc(pid, parentMemory + 4);
        this.memory.getPageTables().get(pid).setFrame(numPages, frame);
        this.memory.getPageTables().get(pid).setShared(numPages, true);
        return success;
    }

    public int getMemFramesUsed(){
        int totalFrames =  MainMemory.NUM_FRAMES;
        int numFreeFrames = this.memory.getFreeFrames().size();
        int numOccFrames = totalFrames - numFreeFrames;
        return numOccFrames;
    }

    public double getMemFramesPercent(){
        return this.memory.calcMemData();
    }

    public int getStorageFramesUsed(){
        Iterator throughStorage = this.memory.getStorage().entrySet().iterator();
        int storageOcc = 0;
        while (throughStorage.hasNext()) {
            Map.Entry pair = (Map.Entry) throughStorage.next();
            int pSpace = (Integer) pair.getValue();
            storageOcc += pSpace;
        }
        return storageOcc;
    }

    public void free(int pid){
        this.memory.releaseProcessData(pid);
    }

    public static void main(String[] args) {
        MMU mmu = new MMU();

        for (int i = 1; i <= MainMemory.NUM_FRAMES; i++) {
            mmu.loadProc(i, i * 4);
        }

        //PRINTING CONTENTS OF MAIN MEMORY
        System.out.println("\nMAIN MEMORY");
        for (int i = 0; i < mmu.memory.getFrameTable().length; i++) {
            System.out.println(i + " ---- [" + mmu.memory.getFrameTable()[i] + "]");
        }

        //PRINTING MEMORY AND STORAGE USAGE
        int memFrames = mmu.getMemFramesUsed();
        double memPercent = mmu.getMemFramesPercent();
        int storeUsed = mmu.getStorageFramesUsed();

        System.out.println("\nMemory Frames Used: " + memFrames + " Mem Percent: " + memPercent + " Store Frames Used: " + storeUsed + "\n");


        //TESTING REQUEST ACCESSING AND PAGING/SWAPPING
        /*for (int i = 1; i < 20; i++) {
            int[] logAddr = {i, 0, i - 1, 0};
            long physical = mmu.requestAddress(logAddr);
            System.out.println("Physical: " + physical);
        }*/

        //TESTING REQUEST ACCESSING AND PAGING/SWAPPING
        int[] logAddr0 = {20, 0, 0, 0};
        int[] logAddr1 = {20, 0, 1, 0};
        int[] logAddr2 = {20, 0, 2, 0};
        int[] logAddr3 = {21, 0, 0, 0};
        int[] logAddr4 = {4, 0, 0, 0};
        long physical0 = mmu.requestAddress(logAddr0);
        System.out.println("Physical: " + physical0);
        physical0 = mmu.requestAddress(logAddr0);
        System.out.println("Physical: " + physical0);
        physical0 = mmu.requestAddress(logAddr0);
        System.out.println("Physical: " + physical0);
        long physical1 = mmu.requestAddress(logAddr1);
        System.out.println("Physical: " + physical1);
        long physical2 = mmu.requestAddress(logAddr2);
        System.out.println("Physical: " + physical2);
        long physical3 = mmu.requestAddress(logAddr3);
        System.out.println("Physical: " + physical3);

        //PRINTING CONTENTS OF MAIN MEMORY
        System.out.println("\nMAIN MEMORY");
        for (int i = 0; i < mmu.memory.getFrameTable().length; i++) {
            System.out.println(i + " ---- [" + mmu.memory.getFrameTable()[i] + "]");
        }

        //Release data from a few processes
        mmu.memory.releaseProcessData(20);
        mmu.memory.releaseProcessData(6);

        /*for (int i = 0; i < 64; i++) {
            int[] logAddr = {64, 0, i, 0};
            long phys = mmu.requestAddress(logAddr);
            System.out.println("Physical: " + phys);
        }*/

        //PRINTING CONTENTS OF MAIN MEMORY
        System.out.println("\nMAIN MEMORY");
        for (int i = 0; i < mmu.memory.getFrameTable().length; i++) {
            System.out.println(i + " ---- [" + mmu.memory.getFrameTable()[i] + "]");
        }
    }
}

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

public class MainMemory {

    /**
     * Constants for frame size and number of frames.
     * FRAME_SIZE used in memory allocation for processes.
     */
    //TODO Determine proper values for these numbers
    public static final int FRAME_SIZE = 4096;

    /**
     * Number of frames in RAM, broken up into 4KB frames
     */
    //private static final int NUM_FRAMES = 1048576;
    private static final int NUM_FRAMES = 64;

    /**
     * Number of frames in storage.
     */
    //private static final int STORAGE_SIZE = 1048576; //TODO Calculate proper value, storage should be 6GB
    private static final int STORAGE_SIZE = 2048;

    /**
     * Each frame is just an integer storing the pid of the process that is storing "data" in it.
     * We do not deal with the actual data stored in a frame for this project, so memory is just a 1D array of frames.
     */
    private int[] frameTable = new int[NUM_FRAMES];
    /**
     * Queue of free frames in memory.
     * Implemented as priority queue so that spaces that have been free the longest have highest priority
     */
    private BlockingQueue<Integer> freeFrames = new LinkedBlockingQueue<>();
    /**
     * Map of page tables in memory, using pid (or maybe ptbr, idk) as key and the page table as the value.
     */
    private Map<Integer, PageTable> pageTables = new HashMap<>();
    /**
     * Map of processes in storage, using pid as key and number of pages to store as the value
     * //TODO Maybe change for demand paging, demand paging can use replace(key, value) to dec/inc value by 1
     */
    private Map<Integer, Integer> storage = new HashMap<>();


    /**
     * Queue of which frames are shared/critical
     * //TODO Make it so this does not have to be searched for every memory access?
     */
    private BlockingQueue<Integer> sharedFrames = new LinkedBlockingQueue<>();

    /**
     * No-Arg Constructor.
     * Initializes the list of free frames as all frames, since nothing has been allocated.
     */
    public MainMemory() {
        for (int i = 0; i < this.frameTable.length; i++) {
            this.frameTable[i] = -1;
            this.freeFrames.add(i);
        }
    }

    /**
     * Allocates memory for a process //TODO This requires enough space for whole process right now, change for demand paging
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
        PageTable pTable = new PageTable(numPages);

        if (this.freeFrames.size() < numPages) {
            System.out.println("Not enough space to load process: " + pid);
            System.out.println("Needed " + numPages + " frames. Found " + this.freeFrames.size());
            //TODO Implement swapping/virtual memory
            if (storage.size() + numPages > STORAGE_SIZE) {
                System.out.println("Secondary Storage full");
                return -1;
            } else {
                storage.put(pid, numPages);
                return 0;
            }
        } else {
            int freeFrame = -1;
            for (int pNum = 0; pNum < numPages; pNum++) {
                freeFrame = this.freeFrames.poll();
                pTable.setFrame(pNum, freeFrame);
                this.frameTable[freeFrame] = pid;
            }
            this.pageTables.put(pid, pTable);

            return 1;
        }
    }

    /**
     * Loads a process from storage to memory.
     *
     * @param pid
     * @return 1 if process was loaded into memory, -1 if not
     */
    public int loadFromStorage(int pid) {
        if (!this.storage.containsKey(pid)) {
            System.out.println("Invalid pid");
            return -1;
        }
        int numPages = this.storage.get(pid);
        if (this.freeFrames.size() < numPages) {
            System.out.println("Not enough space to load process from storage: " + pid);
            System.out.println("Needed " + numPages + " frames. Found " + this.freeFrames.size());
            //TODO Implement swapping/virtual memory
            return -1;
        } else {
            PageTable pTable = new PageTable(numPages);
            int freeFrame = -1;
            for (int pNum = 0; pNum < numPages; pNum++) {
                freeFrame = this.freeFrames.poll();
                pTable.setFrame(pNum, freeFrame);
                this.frameTable[freeFrame] = pid;
            }
            this.pageTables.put(pid, pTable);

            return 1;
        }
    }

    /**
     * Takes in a logical address fro MMU, if not in memory, attempts to load from storage.
     *      If cannot load from storage, return -1 for physical address and frame
     * @param logical
     * @return an array where:
     *          long[0] = physical address
     *          long[1] = frame
     */
    public long[] requestAddress(int[] logical) {

        int pid = logical[0];
        int outerPage = logical[1];
        int innerPage = logical[2];
        int offset = logical[3];
        int frame = -1;

        System.out.println("\n" + offset + "\n");

        if (!this.pageTables.containsKey(pid)) {
            int success = loadFromStorage(pid);
            if (success < 0) {
                return new long[]{-1, -1};
            }
        }

        PageTable pTable = this.pageTables.get(pid);

        for (int i = 0; i < pTable.length(); i++) {
            int f = pTable.getFrame(i);
            System.out.println("Page: " + i + " Frame: " + f);
        }

        frame = pTable.getFrame(innerPage); //TODO implement double-decker taco page table

        System.out.println("\n" + offset + "\n");

        long physFrame = frame * FRAME_SIZE;
        long phys = physFrame + offset;
        String physBinary = Long.toBinaryString(phys);
        while (physBinary.length() < 32) {
            physBinary = "0" + physBinary;
        }
        System.out.println(physBinary);
        System.out.println(phys);

        /*String frameBinary = Long.toBinaryString(frame);
        while (frameBinary.length() < 20){
            frameBinary = "0" + frameBinary;
        }
        String offsetBinary = Integer.toBinaryString(offset);
        while (offsetBinary.length() < 12){
            offsetBinary = "0" + frameBinary;
        }
        String addressBinary = frameBinary + offsetBinary;
        long address = Long.parseLong(addressBinary, 2);
        System.out.println(addressBinary);
        System.out.println(address);*/

        return new long[]{phys, frame};
    }

    /**
     * Selects a victim page of virtual memory to swap with desired page
     * @param logical Logical address holding desired page
     */
    public void swapPage(int[] logical){
        //TODO FINISH THIS
        System.out.println("i dont do anything yet. sorry\n");
        System.out.println("¯\\_(ツ)_/¯");
    }

    /**
     * Takes a frame from the front of freeFrames and puts data in it, removes that frame from the list of free frames.
     *
     * @param data The data to go into a frame //TODO determine what goes in this space (possibly pid or page table number)
     */
    public int initFrame(int data) {
        Integer freeFrame = this.freeFrames.poll();
        if (freeFrames.size() == 0) {
            //TODO handle case where poll() returns null (i.e. there are no free frames)
        }
        this.frameTable[freeFrame] = data;
        return freeFrame;
    }

    /**
     * Checks to see if frame is already free, otherwise adds it to list of free frames. THIS IS SLOW MAY NEED BETTER IMPLEMENTATION USING HASHING
     *
     * @param frame The frame of memory to be released
     */
    public void releaseFrame(int frame) {
        if (!this.freeFrames.contains(frame)) {
            this.frameTable[frame] = -2;
            this.freeFrames.add(frame);
        }
    }

    /**
     * Goes into frameTable and releases all frames associated with this page table
     *
     * @param pTable The page table of a given process
     */
    public void releaseProcessData(PageTable pTable) {
        for (int pNum = 0; pNum < pTable.length(); pNum++) {
            this.releaseFrame(pTable.getFrame(pNum));
        }
    }

    /**
     * Calculates and returns the percentage of frames that are currently in use.
     * Might want to format this number when displaying to GUI
     *
     * @return The percent of physical memory being used
     */
    public double calcMemData() {
        double totalFrames = (double) NUM_FRAMES;
        double numFreeFrames = (double) freeFrames.size();
        double numOccFrames = totalFrames - numFreeFrames;
        return (numOccFrames / totalFrames) * 100;
    }

    /**
     * MOCK PCB CLASS FOR TESTING, LOADING, AND RELEASING MEMORY
     */
    static class PCB {
        int pid;
        int memRequired;
        PageTable ptbr;
        int parentPID;

        public PCB(int id, int mem, int ppid) {
            this.pid = id;
            this.memRequired = mem;

            int numPages = mem / 4;
            if ((mem % 4) != 0) {
                numPages++;
            }
            this.ptbr = new PageTable(numPages);

            this.parentPID = ppid;
        }
    }

    public static void main(String[] args) {
        MainMemory test = new MainMemory();
        TLB tlb = new TLB();
        Random random = new Random();
        for (int i = 1; i <= NUM_FRAMES; i++) {
            test.loadProc(i, i * 4);
        }

        Iterator throughProcess = test.pageTables.entrySet().iterator();

        while (throughProcess.hasNext()) {
            Map.Entry pair = (Map.Entry) throughProcess.next();
            int pid = (Integer) pair.getKey();
            PageTable p = (PageTable) pair.getValue();
            System.out.println("PID: " + pid + " needs: " + p.length());
        }

        int freeFrame = -1;

        System.out.println("\nMAIN MEMORY");
        for (int i = 0; i < test.frameTable.length; i++) {
            System.out.println(i + " ---- [" + test.frameTable[i] + "]");
        }

        double stat = test.calcMemData();
        System.out.println("\nMemory Used: " + stat + "%\n");


        for (int i = 1; i<20;i++) {
            int[] logAddr = {i, 0, 0, 0};
            long tlbHitMis = tlb.get(logAddr);
            if (tlbHitMis > 0) {
                System.out.println("TLB HIT!! ----- " + tlbHitMis);
            } else {
                System.out.print("TLB MISS :'( ");
                long[] memoryAccess = test.requestAddress(logAddr);
                long requestSuccess = memoryAccess[0];
                int returnFrame = (int) memoryAccess[1];
                if (requestSuccess >= 0) {
                    tlb.swap(logAddr, returnFrame);
                }
                else {
                    test.swapPage(logAddr);
                }
            }
        }


        System.out.print(tlb.toString());

        /*test.releaseProcessData(test.pageTables.get(1));
        test.releaseProcessData(test.pageTables.get(4));
        test.releaseProcessData(test.pageTables.get(9));
        test.loadProc(23, 23*4);

        System.out.println("\nMAIN MEMORY");
        for (int i = 0; i < test.frameTable.length; i++) {
            System.out.println(i + " ---- [" + test.frameTable[i] + "]");
        }

        stat = test.calcMemData();
        System.out.println("\nMemory Used: " + stat + "%\n");*/

        /*int pid = 23;
        PCB pControlBlock = test.storage.get(pid);
        if (test.freeFrames.size() < pControlBlock.ptbr.length()) {
            System.out.println("Not enough space to load process: " + pControlBlock.pid);
            System.out.println("Needed " + pControlBlock.ptbr.length() + " frames. Found " + test.freeFrames.size());
            //TODO Implement storage/swapping/virtual memory
        } else {
            freeFrame = -1;
            for (int pNum = 0; pNum < pControlBlock.ptbr.length(); pNum++) {
                freeFrame = test.freeFrames.poll();
                pControlBlock.ptbr.setFrame(pNum, freeFrame);
                test.frameTable[freeFrame] = pControlBlock.pid;
            }
            storage.remove(pid);
        }*/

        /*Iterator throughStore = storage.entrySet().iterator();

        while(throughStore.hasNext()){
            Map.Entry pair = (Map.Entry) throughProcess.next();
            int pid = (Integer) pair.getKey();
            PCB pControlBlock = (PCB) pair.getValue();
            if (test.freeFrames.size() < pControlBlock.ptbr.length()) {
                System.out.println("Not enough space to load process: " + pControlBlock.pid);
                System.out.println("Needed " + pControlBlock.ptbr.length() + " frames. Found " + test.freeFrames.size());
                //TODO Implement storage/swapping/virtual memory
            } else {
                freeFrame = -1;
                for (int pNum = 0; pNum < pControlBlock.ptbr.length(); pNum++) {
                    freeFrame = test.freeFrames.poll();
                    pControlBlock.ptbr.setFrame(pNum, freeFrame);
                    test.frameTable[freeFrame] = pControlBlock.pid;
                }
                storage.remove(pid);
            }
        }*/
    }
}

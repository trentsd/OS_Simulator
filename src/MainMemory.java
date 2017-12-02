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
    public static final int NUM_FRAMES = 64;

    /**
     * Number of frames in storage.
     */
    //private static final int STORAGE_SIZE = 1048576; //TODO Calculate proper value, storage should be 6GB/4KB frames
    public static final int STORAGE_SIZE = 2048;

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
     * Allocates memory for a process.
     * If not enough space in memory, will load what it can into mem and rest into storage
     *
     * @return a success code, 1 for main memory allocated, 0 if data add on storage, -1 if storage was full
     * //TODO Maybe change return value to a ptbr value
     * @parampid the pid of the process being loaded, stored as key value in processTables
     * @parammem the amount of memory required for this process, expressed in kilobytes
     */
    /*public int loadProc(int pid, int mem) {
        int numPages = mem / 4;
        if ((mem % 4) != 0) {
            numPages++;
        }
        PageTable pTable = new PageTable(numPages);

        if (this.freeFrames.size() < numPages) {
            System.out.println("Not enough space to load process: " + pid);
            System.out.println("Needed " + numPages + " frames. Found " + this.freeFrames.size());
            int somePages = this.freeFrames.size();
            int restPages = numPages - somePages;
            //TODO Implement swapping/virtual memory
            if (storage.size() + restPages > STORAGE_SIZE) {
                System.out.println("Secondary Storage full");
                return -1;
            } else {
                int freeFrame = -1;
                for (int pNum = 0; pNum < somePages; pNum++) {
                    freeFrame = this.freeFrames.poll();
                    pTable.setFrame(pNum, freeFrame);
                    pTable.setEntryAsValid(pNum, true);
                    this.frameTable[freeFrame] = pid;
                }
                storage.put(pid, restPages);
                System.out.println("\nStored " + restPages +  " pages of PID " + pid + " in storage\n");
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
    }*/
    public int[] getFrameTable() {
        return frameTable;
    }

    public BlockingQueue<Integer> getFreeFrames() {
        return freeFrames;
    }

    public Map<Integer, PageTable> getPageTables() {
        return pageTables;
    }

    public Map<Integer, Integer> getStorage() {
        return storage;
    }

    public BlockingQueue<Integer> getSharedFrames() {
        return sharedFrames;
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
    private void releaseFrame(int frame) {
        if (!this.freeFrames.contains(frame)) {
            this.frameTable[frame] = -2;
            this.freeFrames.add(frame);
        }
    }

    /**
     * Goes into frameTable and releases all frames associated with this page table, unless they are shared
     *
     * @param pid The pid of the process to be released
     */
    public void releaseProcessData(int pid) {
        PageTable pTable = this.pageTables.get(pid);
        Iterator through = pTable.valids.iterator();
        while (through.hasNext()){
            int page = (int) through.next();
            releaseFrame(pTable.getFrame(page));
            pTable.valids.remove(page);
        }
    }

    /**
     * Calculates and returns the percentage of frames that are currently in use.
     * Might want to format this number when displaying to GUI
     *
     * @return double[0] == Percent of main mem being used
     * double[1] == Percent of storage being used
     * //TODO Maybe just return a string
     */
    public double calcMemData() {
        double totalFrames = (double) NUM_FRAMES;
        double numFreeFrames = (double) freeFrames.size();
        double numOccFrames = totalFrames - numFreeFrames;
        double memUsed = (numOccFrames / totalFrames);


        return memUsed;
    }

    public double calcStorageData(){
        Iterator throughStorage = this.storage.entrySet().iterator();
        double storageOcc = 0;
        while (throughStorage.hasNext()) {
            Map.Entry pair = (Map.Entry) throughStorage.next();
            int pSpace = (Integer) pair.getValue();
            storageOcc += (double) pSpace;
        }
        double storageUsed = (storageOcc / STORAGE_SIZE);

        return storageUsed;
    }
}
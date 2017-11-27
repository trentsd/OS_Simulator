import java.util.Iterator;
import java.util.PriorityQueue;

public class MainMemory {

    /**
     * Constants for frame size and number of frames.
     * FRAME_SIZE used in memory allocation for processes.
     */
    //TODO Determine proper values for these numbers
    private static final int FRAME_SIZE = 4;
    private static final int NUM_FRAMES = 8;

    /**
     * Each frame is just an integer storing the pid of the process that is storing "data" in it.
     * We do not deal with the actual data stored in a frame for this project, so memory is just a 1D array of frames.
     */
    private int[] frameTable = new int[NUM_FRAMES];

    /**
     * Queue of free frames in memory.
     * Implemented as priority queue so that spaces that have been free the longest have highest priority
     */
    private PriorityQueue<Integer> freeFrames = new PriorityQueue<>();

    /**
     * No-Arg Constructor.
     * Initializes the list of free frames as all frames, since nothing has been allocated.
     */
    public MainMemory() {
        for (int i = 0; i < frameTable.length; i++) {
            freeFrames.add(i);
        }
    }

    /**
     * Takes a frame from the front of freeFrames and puts data in it, removes that frame from the list of free frames.
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
            this.freeFrames.add(frame);
        }
    }

    /**
     * Goes into frameTable and releases all frames associated with this page table
     *
     * @param pTable The page table of a given process
     */
    public void releaseProcessData(PageTable pTable) {
        for (int i = 0; i < pTable.length(); i++) {
            this.releaseFrame(pTable.getFrame(i));
        }
    }

    /**
     * Calculates and returns the percentage of frames that are currently in use
     * @return The percent of physical memory being used
     */
    public double calcMemData(){
        double totalFrames = (double) NUM_FRAMES;
        double numFreeFrames = (double) freeFrames.size();
        double numOccFrames = totalFrames - numFreeFrames;
        return (numOccFrames/totalFrames)*100;
    }

    public static void main(String[] args) {
        MainMemory test = new MainMemory();
        PageTable pTable1 = new PageTable(4);
        PageTable pTable2 = new PageTable(3);
        int[] dataz = {11, 22, 33, 44};

        Iterator<Integer> through = test.freeFrames.iterator();

        while (through.hasNext()) {
            System.out.println(through.next());
        }

        //int nums;
        for (int i = 0; i < 4; i++) {
            pTable1.setFrame(i, test.initFrame(dataz[i]));
            //nums = pTable1.getFrame(i);
            //System.out.println(nums);
        }

        for (int x = 0; x < NUM_FRAMES; x++) {
            System.out.println(test.frameTable[x] + " ");
        }

        through = test.freeFrames.iterator();
        while (through.hasNext()) {
            System.out.println(through.next());
        }

        int[] moredataz = {-11, -22, -33, -44};

        for (int i = 0; i < 3; i++) {
            pTable2.setFrame(i, test.initFrame(moredataz[i]));
            //nums = pTable2.getFrame(i);
            //System.out.println(nums);
        }

        for (int x = 0; x < NUM_FRAMES; x++) {
            System.out.println(test.frameTable[x] + " ");
        }

        through = test.freeFrames.iterator();
        while (through.hasNext()) {
            System.out.println(through.next());
        }

        test.releaseProcessData(pTable1);
        through = test.freeFrames.iterator();
        while (through.hasNext()) {
            System.out.println(through.next());
        }

        test.releaseProcessData(pTable2);
        through = test.freeFrames.iterator();
        while (through.hasNext()) {
            System.out.println(through.next());
        }
    }
}

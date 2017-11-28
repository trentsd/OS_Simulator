import java.util.Iterator;
import java.util.PriorityQueue;

public class MainMemory {

    /**
     * Constants for frame size and number of frames
     */
    //TODO Determine proper values for these numbers
    private static final int FRAME_SIZE = 4;
    private static final int NUM_FRAMES = 8;

    /**
     * Each frame is just an array of integers (our fake data)
     * Memory is just an array of frameTable
     */
    private int[] frameTable = new int[NUM_FRAMES];

    /**
     * Queue of free frames in memory.
     * Implemented as priority queue so that spaces that have been free the longest have highest priority
     */
    private PriorityQueue<Integer> freeFrames = new PriorityQueue<>();

    /**
     * No-Arg Constructor.
     * Initializes frameTable to empty 2D int array.
     * Initializes usedFrames to 0.
     * Initializes freeFrames to NUM_FRAMES.
     */
    public MainMemory() {
        for (int i = 0; i < frameTable.length; i++) {
            freeFrames.add(i);
        }
    }

    /**
     * @param data The data to go into a frame. This should come from the Memory Management Unit.
     *             It should not be possible to pass an array larger than a single frame.
     * @throws IllegalArgumentException if the int[] data is larger than a single frame
     */
    public int initFrame(int data) throws IllegalArgumentException {
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

    public double calcMemData(){
        double totalFrames = (double) NUM_FRAMES;
        double numFreeFrames = (double) freeFrames.size();
        double numOccFrames = totalFrames - numFreeFrames;
        return numOccFrames/totalFrames;
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

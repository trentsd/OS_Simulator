import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
    private BlockingQueue<Integer> freeFrames = new LinkedBlockingQueue<>();
    /**
     * Queue of which frames are shared/critical
     * //TODO Make it so this does not have to be searched for every memory access?
     */
    private BlockingQueue<Integer> sharedFrames = new LinkedBlockingQueue<>();
    /**
     * Linked list that will act as a process table. Links ProcessControlBlock objects.
     */
    private BlockingQueue<ProcessControlBlock> processTable = new LinkedBlockingQueue<>();

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
    public void releaseProcessData(PageTable pTable){
        for (int pNum = 0; pNum < pTable.length(); pNum++) {
            this.releaseFrame(pTable.getFrame(pNum));
        }
    }

    /**
     *
     * @param pcb
     */
    public void addProcess(ProcessControlBlock pcb){
        this.processTable.add(pcb);
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
        int pid0 = 0;
        int pid1 = 1;
        int pid2 = 2;
        int pid3 = 3;
        int pid4 = 4;
        int pid5 = 5;
        int pid6 = 6;
        PageTable pTable0 = new PageTable(4);
        PageTable pTable1 = new PageTable(3);
        PageTable pTable2 = new PageTable(2);
        PageTable pTable3 = new PageTable(1);
        PageTable pTable4 = new PageTable(1);
        PageTable pTable5 = new PageTable(1);
        PageTable pTable6 = new PageTable(3);

        Iterator<Integer> through = test.freeFrames.iterator();

        while (through.hasNext()) {
            System.out.println(through.next());
        }

        int freeFrame = -1;


        if (test.freeFrames.size() < pTable0.length()){
            System.out.println("Not enough space to load process: " + pid0);
            System.out.println("Needed " + pTable0.length() + " frames. Found " + test.freeFrames.size());
            //TODO Implement storage/swapping/virtual memory
        }
        else {
            freeFrame = -1;
            for (int pNum = 0; pNum < pTable0.length(); pNum++) {
                freeFrame = test.freeFrames.poll();
                pTable0.setFrame(pNum, freeFrame);
                test.frameTable[freeFrame] = pid0;
            }
            System.out.println("Page Table for pid: " + pid0);
            for (int i = 0; i < pTable0.length(); i++) {
                System.out.println("Page: " + i + "    Frame: " + pTable0.getFrame(i));
            }
            System.out.println();
        }








        if (test.freeFrames.size() < pTable1.length()){
            System.out.println("Not enough space to load process: " + pid1);
            System.out.println("Needed " + pTable1.length() + " frames. Found " + test.freeFrames.size());
            //TODO Implement storage/swapping/virtual memory
        }
        else {
            freeFrame = -1;
            for (int pNum = 0; pNum < pTable1.length(); pNum++) {
                freeFrame = test.freeFrames.poll();
                pTable1.setFrame(pNum, freeFrame);
                test.frameTable[freeFrame] = pid1;
            }
            System.out.println("Page Table for pid: " + pid1);
            for (int i = 0; i < pTable1.length(); i++) {
                System.out.println("Page: " + i + "    Frame: " + pTable1.getFrame(i));
            }
            System.out.println();
        }






        if (test.freeFrames.size() < pTable2.length()){
            System.out.println("Not enough space to load process: " + pid2);
            System.out.println("Needed " + pTable2.length() + " frames. Found " + test.freeFrames.size());
            //TODO Implement storage/swapping/virtual memory
        }
        else {
            freeFrame = -1;
            for (int pNum = 0; pNum < pTable2.length(); pNum++) {
                freeFrame = test.freeFrames.poll();
                pTable2.setFrame(pNum, freeFrame);
                test.frameTable[freeFrame] = pid2;
            }
            System.out.println("Page Table for pid: " + pid2);
            for (int i = 0; i < pTable2.length(); i++) {
                System.out.println("Page: " + i + "    Frame: " + pTable2.getFrame(i));
            }
            System.out.println();
        }





        test.releaseProcessData(pTable0);

        if (test.freeFrames.size() < pTable3.length()){
            System.out.println("Not enough space to load process: " + pid3);
            System.out.println("Needed " + pTable3.length() + " frames. Found " + test.freeFrames.size());
            //TODO Implement storage/swapping/virtual memory
        }
        else {
            freeFrame = -1;
            for (int pNum = 0; pNum < pTable3.length(); pNum++) {
                freeFrame = test.freeFrames.poll();
                pTable3.setFrame(pNum, freeFrame);
                test.frameTable[freeFrame] = pid3;
            }
        }
        if (test.freeFrames.size() < pTable2.length()){
            System.out.println("Not enough space to load process: " + pid2);
            System.out.println("Needed " + pTable2.length() + " frames. Found " + test.freeFrames.size());
            //TODO Implement storage/swapping/virtual memory
        }
        else {
            freeFrame = -1;
            for (int pNum = 0; pNum < pTable2.length(); pNum++) {
                freeFrame = test.freeFrames.poll();
                pTable2.setFrame(pNum, freeFrame);
                test.frameTable[freeFrame] = pid2;
            }
        }
        if (test.freeFrames.size() < pTable4.length()){
            System.out.println("Not enough space to load process: " + pid4);
            System.out.println("Needed " + pTable4.length() + " frames. Found " + test.freeFrames.size());
            //TODO Implement storage/swapping/virtual memory
        }
        else {
            freeFrame = -1;
            for (int pNum = 0; pNum < pTable4.length(); pNum++) {
                freeFrame = test.freeFrames.poll();
                pTable4.setFrame(pNum, freeFrame);
                test.frameTable[freeFrame] = pid4;
            }
        }
        if (test.freeFrames.size() < pTable5.length()){
            System.out.println("Not enough space to load process: " + pid5);
            System.out.println("Needed " + pTable5.length() + " frames. Found " + test.freeFrames.size());
            //TODO Implement storage/swapping/virtual memory
        }
        else {
            freeFrame = -1;
            for (int pNum = 0; pNum < pTable5.length(); pNum++) {
                freeFrame = test.freeFrames.poll();
                pTable5.setFrame(pNum, freeFrame);
                test.frameTable[freeFrame] = pid5;
            }
        }


        System.out.println("MAIN MEMORY");
        for (int i = 0; i<test.frameTable.length; i++){
            System.out.println("[" + test.frameTable[i] + "]");
        }
        System.out.println();



        test.releaseProcessData(pTable2);
        test.releaseProcessData(pTable5);

        if (test.freeFrames.size() < pTable6.length()){
            System.out.println("Not enough space to load process: " + pid6);
            System.out.println("Needed " + pTable6.length() + " frames. Found " + test.freeFrames.size());
            //TODO Implement storage/swapping/virtual memory
        }
        else {
            freeFrame = -1;
            for (int pNum = 0; pNum < pTable6.length(); pNum++) {
                freeFrame = test.freeFrames.poll();
                pTable6.setFrame(pNum, freeFrame);
                test.frameTable[freeFrame] = pid6;
            }
        }

        System.out.println("MAIN MEMORY");
        for (int i = 0; i<test.frameTable.length; i++){
            System.out.println("[" + test.frameTable[i] + "]");
        }

    }
}

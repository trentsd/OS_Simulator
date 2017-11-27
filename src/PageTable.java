public class PageTable {
    /**
     * The page table for a process
     */
    private int[] pTable;

    public PageTable(int numPages){
        this.pTable = new int[numPages];
    }

    public void setFrame(int pageNum, int frameNum){
        this.pTable[pageNum] = frameNum;
    }

    public int getFrame(int pageNum){
        return this.pTable[pageNum];
    }

    /**
     * Used for releasing the data held by a process in MainMemory.java.
     * @return The number of pages/frames allocated with this table.
     */
    public int length(){
        return this.pTable.length;
    }
}
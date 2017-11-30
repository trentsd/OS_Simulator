public class PageTable {
    /**
     * The page table for a process
     */
    private TableEntry[] pTable;

    public PageTable(int numPages){
        this.pTable = new TableEntry[numPages];
        for (int i=0; i<this.pTable.length; i++){
            this.pTable[i] = new TableEntry();
        }
    }

    /**
     * Uses Least Recently Used algorithm with counter implementation
     * @return the location in page table to be replaced
     */
    public int selectVictim(){
        int victim = Integer.MAX_VALUE;
        for (int i=0; i<this.pTable.length; i++){
            if (this.pTable[i].reference < victim){
                victim = i;
            }
        }

        if (victim == Integer.MAX_VALUE){ //Ensures a victim was found
            return -1;
        }
        return victim;
    }

    public void swap(int desiredPage){
        int victim = selectVictim();

    }

    public int getFrame(int pageNum){
        return this.pTable[pageNum].getFrame();
    }

    public void setFrame(int pageNum, int frameNum){
        this.pTable[pageNum].setFrame(frameNum);
    }

    /**
     * Used for releasing the data held by a process in MainMemory.java.
     * @return The number of pages/frames allocated with this table.
     */
    public int length(){
        return this.pTable.length;
    }

    class TableEntry{
        boolean valid;
        boolean shared;
        int reference;
        int frame;

        public TableEntry() {
            this.frame = -1;
            this.valid = false;
            this.shared = false;
            this.reference = 0;
        }

        public void setInvalid(){
            this.valid = false;
        }

        public void reference(int clock){
            this.reference = clock;
        }

        public void setFrame(int f){
            this.frame = f;
        }

        public int getFrame(){
            return this.frame;
        }

        public void setShared(boolean share){
            this.shared = share;
        }
    }
}
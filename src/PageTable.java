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
    public int[] selectVictim(int globalNumRef){
        int victimPage = -1;
        int numRef = globalNumRef;
        for (int pNum = 0; pNum < pTable.length; pNum++){
            if (this.pTable[pNum].validBit && this.pTable[pNum].reference < numRef && !this.pTable[pNum].shared){
                victimPage = pNum;
                numRef = referencesTo(pNum);
            }
        }
        return new int[]{victimPage, numRef};
    }

    /**
     * When loading or swapping a page, use this to change the valid bit
     * @param pNum the index of the PageTableEntry in question
     * @param valid what to set the valid bit to
     */
    public void setEntryAsValid(int pNum, boolean valid){
        this.pTable[pNum].validBit = valid;
    }

    public int getFrame(int pageNum){
        return this.pTable[pageNum].frame;
    }

    public void setFrame(int pageNum, int frameNum){
        this.pTable[pageNum].frame = frameNum;
    }

    public void setShared(int pageNum, boolean share){
        this.pTable[pageNum].shared = share;
    }

    public boolean isShared(int pageNum){
        return this.pTable[pageNum].shared;
    }

    /**
     * Used for releasing the data held by a process in MainMemory.java.
     * @return The number of pages/frames allocated with this table.
     */
    public int length(){
        return this.pTable.length;
    }

    /**
     * Returns number of references to this entry in the page table since it was loaded into memory
     */
    public int referencesTo(int pNum){
        return this.pTable[pNum].reference;
    }

    public void makeReference(int pNum){
        this.pTable[pNum].reference++;
    }

    public void resetReference(int pNum){
        this.pTable[pNum].reference = 0;
    }

    /**
     * Returns whether or not this entry in the page table is valid
     */
    public boolean isValid(int pNum){
        return this.pTable[pNum].validBit;
    }

    /**
     * Checks to see if the frame referenced by this page table entry is being shared
     */


    class TableEntry{
        boolean validBit;
        boolean shared;
        int reference;
        int frame;

        public TableEntry() {
            this.frame = -1;
            this.validBit = false;
            this.shared = false;
            this.reference = 0;
        }

        public void resetRefCount(){
            this.reference = 0;
        }
    }
}
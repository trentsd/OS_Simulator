import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PageTable implements Cloneable{
    /**
     * The page table for a process
     */
    private TableEntry[] pTable;

    public BlockingQueue<Integer> valids;
    /**
     * Whether or not this pTable has any pages in memory, check this after removing a page
     */

    public PageTable(int numPages){
        this.pTable = new TableEntry[numPages];
        valids = new LinkedBlockingQueue<>();
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

    public TableEntry copyEntry(int pNum){
        TableEntry copy = new TableEntry();
        copy.frame = this.pTable[pNum].frame;
        copy.validBit = this.pTable[pNum].validBit;
        copy.shared = this.pTable[pNum].shared;
        copy.reference = this.pTable[pNum].reference;
        return copy;
    }

    /**
     * Returns whether or not this entry in the page table is valid
     */
    public boolean isValid(int pNum){
        return this.pTable[pNum].validBit;
    }

    /**
     * Creates an identical pTable with one extra entry that is set to shared
     * @param frame the value that will go in this entry, calculated by calcVictim in MMU
     */
    public void addSharedSpace(int frame){
        PageTable clone = new PageTable(this.length()+1);
        for (int pNum=0; pNum<this.length(); pNum++){
            clone.pTable[pNum] = this.copyEntry(pNum);
        }
        TableEntry sharedSpace = new TableEntry(true);
        sharedSpace.frame = frame;
        clone.pTable[this.length()] = sharedSpace;

        this.pTable = clone.pTable;
    }

    public PageTable copy() {
        int numPages = this.length();
        PageTable clone = new PageTable(numPages);
        for (int pNum = 0; pNum<numPages; pNum++){
            clone.pTable[pNum] = this.copyEntry(pNum);
        }
        return clone;
    }

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

        public TableEntry(boolean shared){
            this.frame = -1;
            this.validBit = false;
            this.shared = true;
            this.reference = 0;
        }

        public void resetRefCount(){
            this.reference = 0;
        }
    }
}
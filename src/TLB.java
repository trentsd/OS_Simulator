import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class TLB {

    /**
     * Size of TLB. This number can be changed.
     */
    private static final int TLB_SIZE = 1;

    /**
     * Implementing TLB as HashTable for very fast lookups
     */
    private ConcurrentHashMap<Integer, int[]> tlb;

    /**
     * Priority blocking queue for TLB entries. This allows for oldest entry to be replaced
     */
    private BlockingQueue<Integer> queue;

    public TLB(){
        this.tlb = new ConcurrentHashMap<>();
        this.queue = new PriorityBlockingQueue<Integer>();
        for (int i = 0; i<TLB_SIZE; i++){
            this.tlb.put(-i, new int[]{-1, -1});
            this.queue.add(-i);
        }
    }

    public int get(Integer page, int pid){
        int[] tlbEntry = {-1, -1};
        if (this.tlb.contains(page)){
            tlbEntry = this.tlb.get(page);
        }
        else{
            return -1;
        }
        if (tlbEntry[1] == pid){
            return tlbEntry[0];
        }
        return -1;
    }

    public void put(int page, int[] tlbEntry){
        Integer i = this.queue.poll();
        if (i != null){
            this.tlb.remove(i);
        }

        this.queue.add(page);
        this.tlb.put(page, tlbEntry);
    }
}

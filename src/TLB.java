import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class TLB {

    /**
     * Size of TLB. This number can be changed.
     */
    public static final int TLB_SIZE = 4;

    /**
     * //Bad bad bad bad bad
     * [index][page, pid, frame]
     */
    private int[][] tlb;

    /**
     * Priority blocking queue for TLB entries. This allows for oldest entry to be replaced
     */
    private Queue<Integer> queue;

    public TLB(){
        this.tlb = new int[TLB_SIZE][3];
        this.queue = new LinkedList<>();
        for (int i = 0; i<TLB_SIZE; i++){
            this.tlb[i] = new int[]{-1, -1, -1};
            this.queue.add(i);
        }
    }

    /**
     * Checks TLB for page number then if correct pid.
     * @param logical the logical[] given by MMU
     * @return the physical address of the request if TLB hit, -1 if TLB miss
     * //TODO COMBINATION PIZZA HUT AND TACO BELL
     */
    public long get(int[] logical){
        int pid = logical[0];
        int p1 = logical[1];
        int p2 = logical[2];
        int offset = logical[3];

        int frame = -1;
        for (int i = 0; i<TLB_SIZE; i++){
            if (this.tlb[i][0] == p2){
                if (this.tlb[i][1] == pid){
                    frame = this.tlb[i][2];
                }
            }
        }
        if (frame < 0){
            return -1;
        }
        frame = frame * MainMemory.FRAME_SIZE;
        return (long) frame + offset;
    }


    public void swap(int[] logical, int frame){
        int pid = logical[0];
        int p1 = logical[1];
        int p2 = logical[2];
        int[] tlbEntry = {p2, pid, frame};
        int victim = selectVictim();
        this.tlb[victim] = tlbEntry;
        this.queue.add(p2);
    }

    public int selectVictim(){
        int victim = this.queue.poll();
        return victim;
    }

    public String toString(){
        String ret = "";
        for (int i = 0; i<TLB_SIZE; i++){
            ret += ("Page: " + tlb[i][0] + " PID: " + tlb[i][1] + " Frame: " + tlb[i][2] + "\n");
        }
        return ret;
    }
}

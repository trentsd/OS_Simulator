import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;

public class ProcessControlBlock {
    private String name;
    private int cyclesRequired;
    private int cyclesRemaining;
    private int pid;
    //private PageTable ptbr;
    public int blockTime = 0;
    public int incubateTime;
    public int state;
    public int parentId = -1;
    public int reqMem; //in KB

    private LinkedList commandQueue = new LinkedList();


    public ProcessControlBlock(int incubateTime, int cycles, String name){
        this.name = name;
        this.incubateTime = incubateTime;
        this.cyclesRequired = cycles;
        cyclesRemaining = cycles;
        if(incubateTime > 0)
            Main.clock.incubatingProcs.add(this);
        else
            spawn();
    }


    public String getName() {
        return name;
    }

    /*public void setName(String name) {
        this.name = name;
    }*/

    public int getCyclesRequired() {
        return cyclesRequired;
    }

    public void setCyclesRequired(int cyclesRequired) {
        this.cyclesRequired = cyclesRequired;
    }

    public int getCyclesRemaining() {
        return cyclesRemaining;
    }

    public void decCycles() {
        if(blockTime <= 0)
            cyclesRemaining --;
        else
            blockTime--;
    }

    public int getPid() {
        return pid;
    }

    public int getState() {
        return state;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    /*public PageTable getPtbr() {
        return ptbr;
    }*/

    /*public void setPtbr(PageTable ptbr) {
        this.ptbr = ptbr;
    }*/

    public int getNextCommand(){
        return (int)commandQueue.poll();
    }

    public boolean incubate(){
        incubateTime--;
        if(incubateTime <= 0) return true;
        else return false;
    }

    public void spawn(){
        Main.clock.incubatingProcs.remove(this);
        Main.clock.newProcs.add(this);//spawn proc into new queue
        state = States.NEW;
        //request mem
        Main.clock.newProcs.remove(this);//move proc into the ready queue once it has memory allocated
        try {
            Main.queue.put(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        state = States.READY;
    }
}

class Commands {
    public static final int CALCULATE = 0;
    public static final int IO = 1;
    public static final int YIELD = 2;
    public static final int OUT = 3;
}

class States {
    public static final int NEW = 0;
    public static final int READY = 1;
    public static final int RUN = 2;
    public static final int WAIT = 3;
    public static final int EXIT = 4;
}

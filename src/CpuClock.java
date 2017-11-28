import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class CpuClock extends Thread{
    private int currentProc;
    private final BlockingQueue queue;
    private static final int MAX_RUNNING_PROCS = 25;
    public static ArrayList procs = new ArrayList(MAX_RUNNING_PROCS);
    public final int ROUND_ROBIN = 0, FIFS = 1, SRTF = 2;
    protected int scheduler = ROUND_ROBIN;

    public CpuClock (BlockingQueue q){
        queue = q;
    }

    @Override
    public void run() {

        /*try {
            while (true){
                currentProc = (int)procs.get(0);
                int total = currentProc;//todo temp
                
                while (currentProc > 0){
                    currentProc--;
                    checkForProcs();
                    Thread.sleep(1);
                }
                System.out.println("log: " + total + " Process finished");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        schedule();
    }


    private void schedule(){
        if (scheduler == ROUND_ROBIN){
            try {
                roundRobin();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        /*else if(scheduler == FIFS){

        }
        else if(scheduler == SRTF){

        }*/
        else{
            System.out.println("log: ERROR Invalid scheduler selected. Setting to Round Robin.");
            scheduler = ROUND_ROBIN;
            schedule();
            return;
        }
    }

    private void roundRobin() throws InterruptedException {
        final int Q = 10;//time each process gets per turn
        int turn, index = 0;
        ProcessControlBlock proc;

        while(scheduler == ROUND_ROBIN){
            //wait for process
            checkForProcs();
            if(procs.size() == 0)
                continue;

            turn = Q;
            proc = (ProcessControlBlock)procs.get(index);
            //the processes' turn
            while(turn > 0){
                if(proc.getCyclesRemaining() > 1) {
                    //proc--;
                    proc.setCyclesRemaining(proc.getCyclesRemaining() - 1);//sub one from the remaining cycles
                    Thread.sleep(1);
                    turn--;
                }
                else {
                    System.out.println("log: process " + proc.getName() +" finished.");
                    procs.remove(index);
                    index--;
                    break;
                }

            }
            checkForProcs();

            //loop through procs
            if(index < procs.size()-1)
                index++;
            else
                index = 0;
        }

        schedule();

    }

    private void checkForProcs(){
        if(queue.peek() != null)
            procs.add(queue.poll());
        return;
    }


}
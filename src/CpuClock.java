import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class CpuClock extends Thread{
    private final BlockingQueue queue;
    private static final int MAX_RUNNING_PROCS = 25;
    public static List runningProcs = Collections.synchronizedList(new ArrayList(MAX_RUNNING_PROCS));
    public final int ROUND_ROBIN = 0, FIFS = 1, SRTF = 2;
    protected int scheduler = ROUND_ROBIN;
    private final int IO_CHANCE = 1;

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
        ProcessControlBlock proc1, proc2, proc3, proc4;

        while(scheduler == ROUND_ROBIN){
            //wait for process
            //checkForProcs();
            if(queue.size() == 0)
                continue;

            turn = Q;

            proc1 = (ProcessControlBlock)queue.poll();
            runningProcs.add(proc1);
            proc2 = (ProcessControlBlock)queue.poll();
            runningProcs.add(proc2);
            proc3 = (ProcessControlBlock)queue.poll();
            runningProcs.add(proc3);
            proc4 = (ProcessControlBlock)queue.poll();
            runningProcs.add(proc4);
            //the processes' turn
            while(turn > 0){
                if(proc1 == null){}
                else if(proc1.getCyclesRemaining() > 1) {
                    proc1.setCyclesRemaining(proc1.getCyclesRemaining() - 1);//sub one from the remaining cycles
                }
                else {
                    System.out.println("log: process " + proc1.getName() +" finished.");
                    runningProcs.remove(proc1);
                    index--;
                    proc1 = null;
                }

                if(proc2 == null){}
                else if(proc2.getCyclesRemaining() > 1) {
                    proc2.setCyclesRemaining(proc2.getCyclesRemaining() - 1);//sub one from the remaining cycles
                }
                else {
                    System.out.println("log: process " + proc2.getName() +" finished.");
                    runningProcs.remove(proc2);
                    index--;
                    proc2 = null;
                }

                if(proc3 == null){}
                else if(proc3.getCyclesRemaining() > 1) {
                    proc3.setCyclesRemaining(proc3.getCyclesRemaining() - 1);//sub one from the remaining cycles
                }
                else {
                    System.out.println("log: process " + proc3.getName() +" finished.");
                    runningProcs.remove(proc3);
                    index--;
                    proc3 = null;
                }

                if(proc4 == null){}
                else if(proc4.getCyclesRemaining() > 1) {
                    proc4.setCyclesRemaining(proc4.getCyclesRemaining() - 1);//sub one from the remaining cycles
                }
                else {
                    System.out.println("log: process " + proc4.getName() +" finished.");
                    runningProcs.remove(proc4);
                    index--;
                    proc4 = null;
                }


                Thread.sleep(1);
                turn--;

            }
            if(proc1 != null) {
                queue.put(proc1);
                runningProcs.remove(proc1);
            }
            if(proc2 != null) {
                queue.put(proc2);
                runningProcs.remove(proc2);
            }
            if(proc3 != null) {
                queue.put(proc3);
                runningProcs.remove(proc3);
            }
            if(proc4 != null) {
                queue.put(proc4);
                runningProcs.remove(proc4);
            }

            randomIO();
        }

        schedule();

    }

    /*private void checkForProcs(){
        if(queue.peek() != null)
            procs.add(queue.poll());
        return;
    }*/

    private void randomIO(){
        int cycles;
        if(Math.random()*100 < IO_CHANCE) {
            cycles = 25 + (int) (Math.random() * ((50 - 25) + 1));
            System.out.println("log: IO occurred taking " + cycles + " cycles.");
            while (cycles != 0){
                cycles--;
            }
            return;
        }
        else
            return;
    }


}
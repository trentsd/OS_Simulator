import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CpuClock extends Thread{
    private static final int MAX_RUNNING_PROCS = 100;

    private final BlockingQueue readyProcs;
    public static List runningProcs = Collections.synchronizedList(new ArrayList(MAX_RUNNING_PROCS));
    public static List waitingProcs = Collections.synchronizedList(new ArrayList(MAX_RUNNING_PROCS));
    public static BlockingQueue newProcs = new LinkedBlockingQueue();
    public static List incubatingProcs = Collections.synchronizedList(new ArrayList(MAX_RUNNING_PROCS));
    public static BlockingQueue allProcs = new LinkedBlockingQueue();

    public final int ROUND_ROBIN = 0, FCFS = 1, SRTF = 2;
    protected int scheduler = ROUND_ROBIN;
    private final int IO_CHANCE = 1;
    private boolean contProc;
    public int execute = 0;

    public CpuClock (BlockingQueue q){
        readyProcs = q;
    }

    @Override
    public void run() {
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
        else if(scheduler == FCFS){
            try {
                firstCome();
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        /*else if(scheduler == SRTF){

        }*/
        else{
            System.out.println("log: ERROR Invalid scheduler selected. Setting to Round Robin.");
            scheduler = ROUND_ROBIN;
            schedule();
            return;
        }
    }

    private synchronized void roundRobin() throws InterruptedException {
        final int Q = 10;//time each process gets per turn
        ProcessControlBlock proc1 = null, proc2 = null, proc3 = null, proc4 = null;
        int turn1 =0 , turn2 = 0, turn3 = 0, turn4 = 0;
        execute = 0;

        while(scheduler == ROUND_ROBIN){

            if(execute <= 0){
                Thread.sleep(100);
                continue;
            }

            incubateProcs();

            synchronized (runningProcs) {
                //Thread One
                if (turn1 < Q && proc1 != null) {
                    //process is still running
                    if (proc1.getCyclesRemaining() > 0) {
                        //checkCommand(proc1);
                        proc1.setCyclesRemaining(proc1.getCyclesRemaining() - 1);
                        turn1++;
                    } else { //process is done
                        System.out.println("log: process " + proc1.getName() + " finished.");
                        runningProcs.remove(proc1);
                        proc1.state = States.EXIT;
                        proc1 = (ProcessControlBlock) readyProcs.poll();
                        runningProcs.add(proc1);
                        readyProcs.remove(proc1);
                        proc1.state = States.RUN;
                        turn1 = 0;
                    }
                } else {//turn over
                    if (proc1 != null) {
                        readyProcs.add(proc1);
                        runningProcs.remove(proc1);
                        proc1.state = States.READY;
                    }
                    proc1 = (ProcessControlBlock) readyProcs.poll();
                    runningProcs.add(proc1);
                    readyProcs.remove(proc1);
                    proc1.state = States.RUN;
                    turn1 = 0;
                }

                //Thread Two
                if (turn2 < Q && proc2 != null) {
                    //process is still running
                    if (proc2.getCyclesRemaining() > 0) {
                        //checkCommand(proc2);
                        proc2.setCyclesRemaining(proc2.getCyclesRemaining() - 1);
                        turn2++;
                    } else { //process is done
                        System.out.println("log: process " + proc2.getName() + " finished.");
                        runningProcs.remove(proc2);
                        proc2.state = States.EXIT;
                        proc2 = (ProcessControlBlock) readyProcs.poll();
                        runningProcs.add(proc2);
                        readyProcs.remove(proc2);
                        proc2.state = States.RUN;
                        turn2 = 0;
                    }
                } else {//turn over
                    if (proc2 != null) {
                        readyProcs.add(proc2);
                        runningProcs.remove(proc2);
                        proc2.state = States.READY;
                    }
                    proc2 = (ProcessControlBlock) readyProcs.poll();
                    runningProcs.add(proc2);
                    readyProcs.remove(proc2);
                    proc2.state = States.RUN;
                    turn2 = 0;
                }

                //Thread Three
                if (turn3 < Q && proc3 != null) {
                    //process is still running
                    if (proc3.getCyclesRemaining() > 0) {
                        //checkCommand(proc3);
                        proc3.setCyclesRemaining(proc3.getCyclesRemaining() - 1);
                        turn3++;
                    } else { //process is done
                        System.out.println("log: process " + proc3.getName() + " finished.");
                        runningProcs.remove(proc3);
                        proc3.state = States.EXIT;
                        proc3 = (ProcessControlBlock) readyProcs.poll();
                        runningProcs.add(proc3);
                        readyProcs.remove(proc3);
                        proc3.state = States.RUN;
                        turn3 = 0;
                    }
                } else {//turn over
                    if (proc3 != null) {
                        readyProcs.add(proc3);
                        runningProcs.remove(proc3);
                        proc3.state = States.READY;
                    }
                    proc3 = (ProcessControlBlock) readyProcs.poll();
                    runningProcs.add(proc3);
                    readyProcs.remove(proc3);
                    proc3.state = States.RUN;
                    turn3 = 0;
                }

                //Thread Four
                if (turn4 < Q && proc4 != null) {
                    //process is still running
                    if (proc4.getCyclesRemaining() > 0) {
                        //checkCommand(proc4);
                        proc4.setCyclesRemaining(proc4.getCyclesRemaining() - 1);
                        turn4++;
                    } else { //process is done
                        System.out.println("log: process " + proc4.getName() + " finished.");
                        runningProcs.remove(proc4);
                        proc4.state = States.EXIT;
                        proc4 = (ProcessControlBlock) readyProcs.poll();
                        runningProcs.add(proc4);
                        readyProcs.remove(proc4);
                        proc4.state = States.RUN;
                        turn4 = 0;
                    }
                } else {//turn over
                    if (proc4 != null) {
                        readyProcs.add(proc4);
                        runningProcs.remove(proc4);
                        proc4.state = States.READY;
                    }
                    proc4 = (ProcessControlBlock) readyProcs.poll();
                    runningProcs.add(proc4);
                    readyProcs.remove(proc4);
                    proc4.state = States.RUN;
                    turn4 = 0;
                }
            }


            //randomIO();

            updateList();

            execute--;

        }

        schedule();

    }

    private void firstCome() throws InterruptedException {
        execute = 0;
        ProcessControlBlock proc;

        while(execute <= 0){
            Thread.sleep(500);
        }
        incubateProcs();
        proc = (ProcessControlBlock) readyProcs.poll();
        runningProcs.add(proc);
        proc.state = States.RUN;

        while (scheduler == FCFS){

            while(execute > 0) {
                incubateProcs();

                if(proc == null){
                    execute--;
                    try {
                        proc = (ProcessControlBlock) readyProcs.poll();
                        runningProcs.add(proc);
                        proc.state = States.RUN;
                    }catch (NullPointerException e){
                        continue;
                    }

                }

                if (proc.getCyclesRemaining() > 0) {
                    //checkCommand
                    proc.setCyclesRemaining(proc.getCyclesRemaining() - 1);
                    execute --;
                }
                else {
                    proc.state = States.EXIT;
                    runningProcs.remove(proc);
                    System.out.println("log: " + proc.getName() + " finished.");

                    proc = (ProcessControlBlock) readyProcs.poll();
                    if(proc == null){
                        execute = 0;
                        break;
                    }
                    runningProcs.add(proc);
                    proc.state = States.RUN;

                    updateList();
                }

            }
            Thread.sleep(2);//prevent high cpu usage

            updateList();

        }
        schedule();
    }

    private void incubateProcs(){
        for(int i = 0; i < incubatingProcs.size(); i++){
            //(ProcessControlBlock)(incubatingProcs.get(i)).incubateTime -= 1;
            // I DONT KNOW WHY THIS DOESNT WORK, ITS BEING CAST SO I HAD TO USE THIS STUPID TEMP VAR
            ProcessControlBlock temp = (ProcessControlBlock)incubatingProcs.get(i);
            if(temp.incubate()){
                temp.spawn();
            }
        }
    }

    private void updateList(){
        allProcs.clear();

        try {
            allProcs.addAll(runningProcs);
        }catch(NullPointerException e){
            //System.out.println(e.toString());
        }
        allProcs.addAll(waitingProcs);
        allProcs.addAll(newProcs);
        allProcs.addAll(readyProcs);
        allProcs.remove(Collections.singleton(null));
    }

    private void randomIO(){
        int cycles;
        if(Math.random()*100 < IO_CHANCE) {
            cycles = 25 + (int)(Math.random() * ((50 - 25) + 1));
            System.out.println("log: IO occurred taking " + cycles + " cycles.");
            while (cycles != 0){
                cycles--;
            }
            return;
        }
        else
            return;
    }

    private void checkCommand(ProcessControlBlock proc){
        switch (proc.getNextCommand()){
            case Commands.CALCULATE:
                proc.setCyclesRemaining(proc.getCyclesRemaining() - 1);
                contProc = true;
                break;
            case Commands.IO:
                proc.state = States.WAIT;
                proc.blockTime = 25 + (int)(Math.random() * ((50 - 25) + 1));
                waitingProcs.add(proc);
                runningProcs.remove(proc);
                contProc = false;
                break;
            case Commands.YIELD:
                try {
                    readyProcs.put(proc);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runningProcs.remove(proc);
                contProc = false;
                break;
            case Commands.OUT:
                break;
            default:
                System.out.println("log: Command queue empty.");
                break;
        }
    }


}
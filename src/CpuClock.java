import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CpuClock extends Thread {
    private static final int MAX_RUNNING_PROCS = 100;

    private final BlockingQueue readyProcs;
    public static List runningProcs = Collections.synchronizedList(new ArrayList(MAX_RUNNING_PROCS));
    public static List waitingProcs = Collections.synchronizedList(new ArrayList(MAX_RUNNING_PROCS));
    public static BlockingQueue newProcs = new LinkedBlockingQueue();
    public static List incubatingProcs = Collections.synchronizedList(new ArrayList(MAX_RUNNING_PROCS));
    public static BlockingQueue allProcs = new LinkedBlockingQueue();
    private static ProcessControlBlock proc1, proc2, proc3, proc4;

    public final int ROUND_ROBIN = 0, FCFS = 1, SRTF = 2;
    protected int scheduler = ROUND_ROBIN;
    private final int IO_CHANCE = 1;
    public int execute = 0;

    public CpuClock(BlockingQueue q) {
        readyProcs = q;
    }

    @Override
    public void run() {
        schedule();
    }

    private void schedule() {
        //set scheduler
        if (scheduler == ROUND_ROBIN) {
            try {
                roundRobin();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (scheduler == FCFS) {
            try {
                threadedFirstCome();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        /*else if(scheduler == SRTF){

        }*/
        else {
            System.out.println("log: ERROR Invalid scheduler selected. Setting to Round Robin.");
            scheduler = ROUND_ROBIN;
            schedule();
            return;
        }
    }


    private synchronized void roundRobin() throws InterruptedException {
        final int Q = 10;//time each process gets per turn
        int turn1 = 0, turn2 = 0, turn3 = 0, turn4 = 0;
        execute = 0;

        while (scheduler == ROUND_ROBIN) {

            //wait for the user to allow execution
            if (execute <= 0) {
                updateList();
                Thread.sleep(100);
                continue;
            }

            //decrement the timer on procs waiting to spawn
            incubateProcs();


            //Thread One
            if (turn1 < Q && proc1 != null) {
                //process is still running
                if (proc1.getCyclesRemaining() > 0) {
                    proc1 = checkCommand(proc1);//do command
                    proc1 = randomIO(proc1);//check for random external IO
                    turn1++;
                }
                else { //process is done
                    System.out.println("log: process " + proc1.getName() + " finished.");
                    runningProcs.remove(proc1);
                    proc1.state = States.EXIT;
                    Main.mmu.free(proc1.getPid());
                    proc1 = (ProcessControlBlock) readyProcs.poll();//pull next proc into running
                    if (proc1 != null) {//make sure a proc was pulled
                        runningProcs.add(proc1);
                        readyProcs.remove(proc1);
                        proc1.state = States.RUN;
                    }
                    turn1 = 0;
                }
            } else {//turn over or nothing in thread
                if (proc1 != null) {//if it is a completed turn
                    readyProcs.add(proc1);
                    runningProcs.remove(proc1);
                    proc1.state = States.READY;
                }
                proc1 = (ProcessControlBlock) readyProcs.poll();//try to pull new proc into running
                if (proc1 != null) {
                    runningProcs.add(proc1);
                    readyProcs.remove(proc1);
                    proc1.state = States.RUN;
                }
                turn1 = 0;
            }

            //Thread Two
            if (turn2 < Q && proc2 != null) {
                //process is still running
                if (proc2.getCyclesRemaining() > 0) {
                    proc2 = checkCommand(proc2);
                    proc2 = randomIO(proc2);
                    turn2++;
                } else { //process is done
                    System.out.println("log: process " + proc2.getName() + " finished.");
                    runningProcs.remove(proc2);
                    proc2.state = States.EXIT;
                    Main.mmu.free(proc2.getPid());
                    proc2 = (ProcessControlBlock) readyProcs.poll();
                    if (proc2 != null) {
                        runningProcs.add(proc2);
                        readyProcs.remove(proc2);
                        proc2.state = States.RUN;
                    }
                    turn2 = 0;
                }
            } else {//turn over or nothing in thread
                if (proc2 != null) {
                    readyProcs.add(proc2);
                    runningProcs.remove(proc2);
                    proc2.state = States.READY;
                }
                proc2 = (ProcessControlBlock) readyProcs.poll();
                if (proc2 != null) {
                    runningProcs.add(proc2);
                    readyProcs.remove(proc2);
                    proc2.state = States.RUN;
                }
                turn2 = 0;
            }

            //Thread Three
            if (turn3 < Q && proc3 != null) {
                //process is still running
                if (proc3.getCyclesRemaining() > 0) {
                    proc3 = checkCommand(proc3);
                    proc3 = randomIO(proc3);
                    turn3++;
                } else { //process is done
                    System.out.println("log: process " + proc3.getName() + " finished.");
                    runningProcs.remove(proc3);
                    proc3.state = States.EXIT;
                    Main.mmu.free(proc3.getPid());
                    proc3 = (ProcessControlBlock) readyProcs.poll();
                    if (proc3 != null) {
                        runningProcs.add(proc3);
                        readyProcs.remove(proc3);
                        proc3.state = States.RUN;
                    }
                    turn3 = 0;
                }
            } else {//turn over or nothing in thread
                if (proc3 != null) {
                    readyProcs.add(proc3);
                    runningProcs.remove(proc3);
                    proc3.state = States.READY;
                }
                proc3 = (ProcessControlBlock) readyProcs.poll();
                if (proc3 != null) {
                    runningProcs.add(proc3);
                    readyProcs.remove(proc3);
                    proc3.state = States.RUN;
                }
                turn3 = 0;
            }

            //Thread Four
            if (turn4 < Q && proc4 != null) {
                //process is still running
                if (proc4.getCyclesRemaining() > 0) {
                    proc4 = checkCommand(proc4);
                    proc4 = randomIO(proc4);
                    turn4++;
                } else { //process is done
                    System.out.println("log: process " + proc4.getName() + " finished.");
                    runningProcs.remove(proc4);
                    proc4.state = States.EXIT;
                    Main.mmu.free(proc4.getPid());
                    proc4 = (ProcessControlBlock) readyProcs.poll();
                    if (proc4 != null) {
                        runningProcs.add(proc4);
                        readyProcs.remove(proc4);
                        proc4.state = States.RUN;
                    }
                    turn4 = 0;
                }
            } else {//turn over or nothing in thread
                if (proc4 != null) {
                    readyProcs.add(proc4);
                    runningProcs.remove(proc4);
                    proc4.state = States.READY;
                }
                proc4 = (ProcessControlBlock) readyProcs.poll();
                if (proc4 != null) {
                    runningProcs.add(proc4);
                    readyProcs.remove(proc4);
                    proc4.state = States.RUN;
                }
                turn4 = 0;
            }

            updateWaiting();//update timer on threads waiting on IO
            updateList();//update current procs list

            execute--;//decrement cycles ran

        }

        schedule();

    }


    private void threadedFirstCome() throws InterruptedException {
        execute = 0;

        while (execute <= 0) {//wait for the user to allow execution
            updateList();
            Thread.sleep(500);
        }
        incubateProcs();//update procs waiting to spawn

        //intial attempt at loading procs into cpu
        proc1 = (ProcessControlBlock) readyProcs.poll();
        if(proc1 != null){
            runningProcs.add(proc1);
            proc1.state = States.RUN;
        }

        proc2 = (ProcessControlBlock) readyProcs.poll();
        if(proc2 !=null) {
            runningProcs.add(proc2);
            proc2.state = States.RUN;
        }

        proc3 = (ProcessControlBlock) readyProcs.poll();
        if(proc3 != null) {
            runningProcs.add(proc3);
            proc3.state = States.RUN;
        }

        proc4 = (ProcessControlBlock) readyProcs.poll();
        if(proc4 != null) {
            runningProcs.add(proc4);
            proc4.state = States.RUN;
        }


        while (scheduler == FCFS) {

            //execute for amount of cycles user requested
            while (execute > 0) {
                incubateProcs();

                proc1 = fcfsHelper(proc1);
                proc2 = fcfsHelper(proc2);
                proc3 = fcfsHelper(proc3);
                proc4 = fcfsHelper(proc4);

                updateWaiting();

                execute--;

            }
            Thread.sleep(2);//prevent high cpu usage

            updateList();


        }
        schedule();
    }

    private ProcessControlBlock fcfsHelper(ProcessControlBlock proc) {
        if (proc == null) {//no process is loaded

            proc = (ProcessControlBlock) readyProcs.poll();//attempt to pull a proc onto the cpu
            if(proc != null) {
                runningProcs.add(proc);
                proc.state = States.RUN;
            }
            else
                return null;

        }

        if (proc.getCyclesRemaining() > 1) {//process has cycles remaining
            proc = checkCommand(proc);
            proc = randomIO(proc);

        }
        else {//process is finished
            proc = checkCommand(proc);//execute last command

            //wrap up proc
            proc.state = States.EXIT;
            Main.mmu.free(proc.getPid());
            runningProcs.remove(proc);
            System.out.println("log: " + proc.getName() + " finished.");

            //attempt to pull in next proc to cpu
            proc = (ProcessControlBlock) readyProcs.poll();
            if (proc == null) {
                return null;
            }
            runningProcs.add(proc);
            proc.state = States.RUN;
            updateList();
        }

        return proc;
    }


    private void incubateProcs() {
        for (int i = 0; i < incubatingProcs.size(); i++) {

            ProcessControlBlock temp = (ProcessControlBlock) incubatingProcs.get(i);
            if (temp.incubate()) {//check if it is time for proc to spawn
                temp.spawn();
            }
        }
    }

    private void updateWaiting(){
        for(int i = 0; i < waitingProcs.size(); i++){
            ProcessControlBlock temp = (ProcessControlBlock)waitingProcs.get(i);
            if(temp.waitForIO()){//check if IO is done
                waitingProcs.remove(temp);
                readyProcs.add(temp);
            }
        }
    }

    private void updateList() { //update list of all processes
        allProcs.clear();

        try {
            allProcs.addAll(runningProcs);
        } catch (NullPointerException e) { }

        allProcs.addAll(waitingProcs);
        allProcs.addAll(newProcs);
        allProcs.addAll(readyProcs);
        allProcs.remove(Collections.singleton(null));
    }

    private ProcessControlBlock randomIO(ProcessControlBlock proc) {
        int cycles;
        if(proc == null)
            return null;
        if (Math.random() * 100 < IO_CHANCE) {
            cycles = RNGesus.randInRange(25, 50);
            System.out.println("log: external IO occured in proc " + proc.getPid() + " of length " + cycles + ". Blocking.");
            //put process in blocking state
            proc.state = States.WAIT;
            proc.blockTime = cycles;
            waitingProcs.add(proc);
            runningProcs.remove(proc);
            return null;
        } else
            return proc;
    }

    private ProcessControlBlock checkCommand(ProcessControlBlock proc) {
        switch (proc.getNextCommand()) {
            case Commands.CALCULATE:
                proc.decCycles();
                return proc;

            case Commands.IO:
                int cycles;
                proc.decCycles();
                proc.state = States.WAIT;
                cycles = RNGesus.randInRange(25, 50);
                proc.blockTime = cycles;
                System.out.println("log: proc " + proc.getPid() + " encountered internal IO of " + cycles + " cycles");
                waitingProcs.add(proc);
                runningProcs.remove(proc);
                return null;

            case Commands.YIELD:
                System.out.println("log: proc " + proc.getPid() + " yielding");
                try {
                    proc.decCycles();
                    readyProcs.put(proc);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runningProcs.remove(proc);
                return null;

            case Commands.OUT:
                proc.decCycles();
                Main.gui.displayText(proc.getNextOut());
                return proc;

            default:
                System.out.println("log: Command queue empty.");
                return null;

        }
    }

    public void reset() {
        //interupt/ stop running cycles
        execute = 0;
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        execute = 0;

        //free memory
        Main.mmu.clearAll();

        //dump processes from queues/ lists
        proc1 = null;
        proc2 = null;
        proc3 = null;
        proc4 = null;

        allProcs.clear();
        runningProcs.clear();
        readyProcs.clear();
        incubatingProcs.clear();
        waitingProcs.clear();
        readyProcs.clear();

    }


}
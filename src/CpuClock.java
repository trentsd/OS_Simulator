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
    private boolean contProc;
    public int execute = 0;

    public CpuClock(BlockingQueue q) {
        readyProcs = q;
    }

    @Override
    public void run() {
        schedule();
    }

    private void schedule() {
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
        //ProcessControlBlock proc1 = null, proc2 = null, proc3 = null, proc4 = null;
        int turn1 = 0, turn2 = 0, turn3 = 0, turn4 = 0;
        execute = 0;

        while (scheduler == ROUND_ROBIN) {

            if (execute <= 0) {
                updateList();
                Thread.sleep(100);
                continue;
            }

            incubateProcs();


            //Thread One
            if (turn1 < Q && proc1 != null) {
                //process is still running
                if (proc1.getCyclesRemaining() > 0) {
                    //checkCommand(proc1);
                    proc1.decCycles();
                    turn1++;
                } else { //process is done
                    System.out.println("log: process " + proc1.getName() + " finished.");
                    runningProcs.remove(proc1);
                    proc1.state = States.EXIT;
                    proc1 = (ProcessControlBlock) readyProcs.poll();
                    if (proc1 != null) {
                        runningProcs.add(proc1);
                        readyProcs.remove(proc1);
                        proc1.state = States.RUN;
                    }
                    turn1 = 0;
                }
            } else {//turn over
                if (proc1 != null) {
                    readyProcs.add(proc1);
                    runningProcs.remove(proc1);
                    proc1.state = States.READY;
                }
                proc1 = (ProcessControlBlock) readyProcs.poll();
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
                    //checkCommand(proc2);
                    proc2.decCycles();
                    turn2++;
                } else { //process is done
                    System.out.println("log: process " + proc2.getName() + " finished.");
                    runningProcs.remove(proc2);
                    proc2.state = States.EXIT;
                    proc2 = (ProcessControlBlock) readyProcs.poll();
                    if (proc2 != null) {
                        runningProcs.add(proc2);
                        readyProcs.remove(proc2);
                        proc2.state = States.RUN;
                    }
                    turn2 = 0;
                }
            } else {//turn over
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
                    //checkCommand(proc3);
                    proc3.decCycles();
                    turn3++;
                } else { //process is done
                    System.out.println("log: process " + proc3.getName() + " finished.");
                    runningProcs.remove(proc3);
                    proc3.state = States.EXIT;
                    proc3 = (ProcessControlBlock) readyProcs.poll();
                    if (proc3 != null) {
                        runningProcs.add(proc3);
                        readyProcs.remove(proc3);
                        proc3.state = States.RUN;
                    }
                    turn3 = 0;
                }
            } else {//turn over
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
                    //checkCommand(proc4);
                    proc4.decCycles();
                    turn4++;
                } else { //process is done
                    System.out.println("log: process " + proc4.getName() + " finished.");
                    runningProcs.remove(proc4);
                    proc4.state = States.EXIT;
                    proc4 = (ProcessControlBlock) readyProcs.poll();
                    if (proc4 != null) {
                        runningProcs.add(proc4);
                        readyProcs.remove(proc4);
                        proc4.state = States.RUN;
                    }
                    turn4 = 0;
                }
            } else {//turn over
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


            //randomIO();

            updateList();

            execute--;

        }

        schedule();

    }


    private void firstCome() throws InterruptedException { //OUTDATED FOR REFERENCE ONLY
        execute = 0;
        ProcessControlBlock proc;

        while (execute <= 0) {
            Thread.sleep(500);
        }
        incubateProcs();
        proc = (ProcessControlBlock) readyProcs.poll();
        runningProcs.add(proc);
        proc.state = States.RUN;

        while (scheduler == FCFS) {

            while (execute > 0) {
                incubateProcs();

                if (proc == null) {//no process is loaded
                    execute--;
                    try {
                        proc = (ProcessControlBlock) readyProcs.poll();
                        runningProcs.add(proc);
                        proc.state = States.RUN;
                    } catch (NullPointerException e) {
                        continue;
                    }

                }

                if (proc.getCyclesRemaining() > 0) {
                    //checkCommand
                    proc.decCycles();
                    execute--;
                } else {
                    proc.state = States.EXIT;
                    runningProcs.remove(proc);
                    System.out.println("log: " + proc.getName() + " finished.");

                    proc = (ProcessControlBlock) readyProcs.poll();
                    if (proc == null) {
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
    }//FOR REFERENCE ONLY--OUTDATED

    private void threadedFirstCome() throws InterruptedException {
        execute = 0;

        while (execute <= 0) {
            updateList();
            Thread.sleep(500);
        }
        incubateProcs();

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

            while (execute > 0) {
                incubateProcs();

                proc1 = fcfsHelper(proc1);
                proc2 = fcfsHelper(proc2);
                proc3 = fcfsHelper(proc3);
                proc4 = fcfsHelper(proc4);

                execute--;

            }
            Thread.sleep(2);//prevent high cpu usage

            updateList();

        }
        schedule();
    }

    private ProcessControlBlock fcfsHelper(ProcessControlBlock proc) {
        if (proc == null) {//no process is loaded

            proc = (ProcessControlBlock) readyProcs.poll();
            if(proc != null) {
                runningProcs.add(proc);
                proc.state = States.RUN;
            }
            else
                return null;

        }

        if (proc.getCyclesRemaining() > 1) {//process has cycles remaining
            proc = checkCommand(proc);
            //proc.decCycles();

        }
        else {//process is finished
            proc = checkCommand(proc);
            //proc.decCycles();

            proc.state = States.EXIT;
            runningProcs.remove(proc);
            System.out.println("log: " + proc.getName() + " finished.");

            proc = (ProcessControlBlock) readyProcs.poll();
            if (proc == null) {
                return null;
            }
            runningProcs.add(proc);
            proc.state = States.RUN;
            //execute++;
            updateList();
        }

        return proc;
    }


    private void incubateProcs() {
        for (int i = 0; i < incubatingProcs.size(); i++) {
            //(ProcessControlBlock)(incubatingProcs.get(i)).incubateTime -= 1;
            // I DONT KNOW WHY THIS DOESNT WORK, ITS BEING CAST SO I HAD TO USE THIS STUPID TEMP VAR
            ProcessControlBlock temp = (ProcessControlBlock) incubatingProcs.get(i);
            if (temp.incubate()) {
                temp.spawn();
            }
        }
    }

    private void updateList() {
        allProcs.clear();

        try {
            allProcs.addAll(runningProcs);
        } catch (NullPointerException e) {
            //System.out.println(e.toString());
        }
        allProcs.addAll(waitingProcs);
        allProcs.addAll(newProcs);
        allProcs.addAll(readyProcs);
        allProcs.remove(Collections.singleton(null));
    }

    private void randomIO(ProcessControlBlock proc) {
        int cycles;
        if (Math.random() * 100 < IO_CHANCE) {
            cycles = RNGesus.randInRange(25, 50);
            System.out.println("log: IO occurred taking " + cycles + " cycles.");
            proc.state = States.WAIT;
            proc.blockTime = cycles;
            return;
        } else
            return;
    }

    private ProcessControlBlock checkCommand(ProcessControlBlock proc) {
        switch (proc.getNextCommand()) {
            case Commands.CALCULATE:
                proc.decCycles();
                contProc = true;
                return proc;

            case Commands.IO:
                proc.decCycles();
                proc.state = States.WAIT;
                proc.blockTime = 25 + (int) (Math.random() * ((50 - 25) + 1));
                waitingProcs.add(proc);
                runningProcs.remove(proc);
                contProc = false;
                return null;

            case Commands.YIELD:
                try {
                    proc.decCycles();
                    readyProcs.put(proc);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runningProcs.remove(proc);
                contProc = false;
                return null;

            case Commands.OUT:
                proc.decCycles();
                return proc;

            default:
                System.out.println("log: Command queue empty.");
                return null;

        }
    }

    public void reset() {
        execute = 0;
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        execute = 0;
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
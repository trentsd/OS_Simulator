import javafx.application.Application;
import javafx.stage.Stage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    static CommandLine cli;
    static CpuClock clock;
    static BlockingQueue queue = new LinkedBlockingQueue();
    static GraphicalUserInterface gui;

    public static void main(String[] args) {


        clock = new CpuClock(queue);
        new Thread(clock).start();

        cli = new CommandLine(queue, false);

        //Application.launch(GraphicalUserInterface.class);

        new Thread(){
            @Override
            public void run(){
                Application.launch(GraphicalUserInterface.class);
            }
        }.start();
        gui = GraphicalUserInterface.waitForGui();

        /*
        MainMemory memory = new MainMemory();
        PageTable pTable = new PageTable(4);
        int[] dataz = {11, 22, 33, 44};
        for (int i = 0; i < 4; i++) {
            pTable.setFrame(i, memory.initFrame(dataz[i]));
            pTable.getFrame(i);
        }
        double memData = memory.calcMemData();
        */


        //System.exit(0);

    }

    //todo move me. I do not belong here.
    public static void selectScheduler(int scheduler){
        if(scheduler >= 0 && scheduler <= 2)
            clock.scheduler = scheduler;
        else
            System.out.println("log: ERROR invalid scheduler called");
    }

    public static void shutDown(){
        gui.closeProgram();
        System.exit(0);
    }

}

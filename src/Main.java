import javafx.application.Application;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    static CommandLine cli;
    static CpuClock clock;
    static BlockingQueue queue = new LinkedBlockingQueue();

    public static void main(String[] args) throws Exception {


        clock = new CpuClock(queue);
        new Thread(clock).start();

        cli = new CommandLine(queue, false);

        Application.launch(GraphicalUserInterface.class);

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


        System.exit(0);

    }

    public static void selectScheduler(int scheduler){
        if(scheduler >= 0 && scheduler <= 2)
            clock.scheduler = scheduler;
        else
            System.out.println("log: ERROR invalid scheduler called");
    }

}

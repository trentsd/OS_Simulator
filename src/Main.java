import javafx.application.Application;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    static CommandLine cli;
    static CpuClock clock;

    public static void main(String[] args) throws Exception {

        BlockingQueue queue = new LinkedBlockingQueue();
        clock = new CpuClock(queue);
        new Thread(clock).start();

        cli = new CommandLine(queue, false);

        Application.launch(GraphicalUserInterface.class);

        System.exit(0);

    }

    public static void selectScheduler(int scheduler){
        if(scheduler >= 0 && scheduler <= 2)
            clock.scheduler = scheduler;
        else
            System.out.println("log: ERROR invalid scheduler called");
    }

}

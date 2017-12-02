import javafx.application.Application;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    static CommandLine cli;
    static CpuClock clock;
    static BlockingQueue queue = new LinkedBlockingQueue();
    static GraphicalUserInterface gui;
    static int pid = 0; // I really should lose my programming license for this
    static MMU mmu = new MMU();

    public static void main(String[] args) {


        clock = new CpuClock(queue);
        new Thread(clock).start();

        cli = new CommandLine(queue, false);


        new Thread(() -> Application.launch(GraphicalUserInterface.class)).start();
        gui = GraphicalUserInterface.waitForGui();


    }

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

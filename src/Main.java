import javafx.application.Application;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    public static void main(String[] args){
        BlockingQueue queue = new LinkedBlockingQueue();
        CpuClock clock = new CpuClock(queue);
        new Thread(clock).start();

        CommandLine cli = new CommandLine(queue, true);


        Application.launch(GraphicalUserInterface.class);

    }

}

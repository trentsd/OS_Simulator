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

        MainMemory memory = new MainMemory();
        PageTable pTable = new PageTable(4);
        int[] dataz = {11, 22, 33, 44};
        for (int i = 0; i < 4; i++) {
            pTable.setFrame(i, memory.initFrame(dataz[i]));
            pTable.getFrame(i);
        }
        double memData = memory.calcMemData();



    }

}

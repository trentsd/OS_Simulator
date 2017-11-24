import java.util.Collection;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public class CpuClock extends Thread{
    private PriorityQueue<Integer> procQueue = new PriorityQueue<>();
    private int currentProc;
    private final BlockingQueue queue;

    public CpuClock (BlockingQueue q){
        queue = q;
    }

    @Override
    public void run() {
        /*while(true){

            if(procQueue.peek() != null){
                currentProc = procQueue.poll();
                while (currentProc > 0){
                    currentProc--;
                    //todo add logic for what happens on cycle
                }
                System.out.println("Proccess Finished.");
            }
        }*/
        try {
            while (true){
                currentProc = (int)queue.take();
                while (currentProc > 0){
                    currentProc--;
                }
                System.out.println("Process finished");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addProc(int cycles){ //todo make this take process files/ info or something
        procQueue.add(cycles);
        //System.out.println("added proc");
    }

    public void dispose(){
        return;
    }

}
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;


/**
 * CommandLine is a class that performs command line functions for the OS User Interface.
 *
 * @author Conklin
 */
public class CommandLine {
    private BufferedReader userInput;
    private Scanner cli;
    private final byte STATUS_NORMAL = 0;
    private final BlockingQueue queue;

    public CommandLine(BlockingQueue q){
        this.userInput = new BufferedReader(new InputStreamReader(System.in));
        this.queue = q;
        runCLI();
    }

    private void runCLI(){
        while(true) {
            System.out.print("USER$ ");
            try {
                String input = userInput.readLine();
                this.cli = new Scanner(input);
                String commandToken = cli.next();

                switch(commandToken.toUpperCase()){
                    case "PROC":
                        //do PROC here
                        System.out.println("PROC\n"); //debug
                        break;
                    case "MEM":
                        //do MEM here
                        System.out.println("MEM\n"); //debug
                        break;
                    case "LOAD":
                        //do LOAD here
                        System.out.println("LOAD\n"); //debug
                        break;
                    case "EXE":
                        //do EXE here
                        System.out.println("EXE\n"); //debug
                        queue.put((int)100);
                        //todo make this dynamic, add flags for input
                        break;
                    case "RESET":
                        //do RESET here
                        System.out.println("RESET\n"); //debug
                        break;
                    case "EXIT":
                        System.out.println("Exiting!");
                        System.exit(STATUS_NORMAL);
                        break;
                    default:
                        System.out.println(commandToken + " is not recognized as a command. Please try again.");
                }

            } catch(IOException e){
                System.out.println("HAHA LOL GIT GUD SCRUB");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

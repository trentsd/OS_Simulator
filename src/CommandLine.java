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

    public CommandLine(BlockingQueue q, boolean debug){
        this.queue = q;

        if(debug) {
            this.userInput = new BufferedReader(new InputStreamReader(System.in));
            runDebugCLI();
        }
    }

    private void runDebugCLI(){
        while(true) {
            System.out.print("USER$ ");
            try {
                String input = userInput.readLine();
                interpretInput(input);
            } catch(IOException e){
                System.out.println("HAHA LOL GIT GUD SCRUB");
            }
        }
    }

    public String interpretInput(String line){
        String output;

        this.cli = new Scanner(line);
        String commandToken = cli.next();

        switch(commandToken.toUpperCase()){
            case "PROC":
                //do PROC here
                output = "PROC\n"; //debug
                break;
            case "MEM":
                //do MEM here
                output = "MEM\n"; //debug
                break;
            case "LOAD":
                //do LOAD here
                output = "LOAD\n"; //debug
                break;
            case "EXE":
                //do EXE here
                output = "EXE\n"; //debug
                try {
                    queue.put(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //todo make this dynamic, add flags for input
                break;
            case "RESET":
                //do RESET here
                output = "RESET\n"; //debug
                break;
            case "EXIT":
                output = "Exiting!";
                System.exit(STATUS_NORMAL);
                break;
            default:
                output = commandToken + " is not recognized as a command. Please try again.";
        }

        return output;
    }
}

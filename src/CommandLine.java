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
    private final String loadAppend = ".txt";

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
                output = doProc();
                break;
            case "MEM":
                output = doMem();
                break;
            case "LOAD":
                output = doLoad();
                break;
            case "EXE":
                output = doExe();
                break;
            case "RESET":
                output = doReset();
                break;
            case "EXIT":
                output = doExit();
                break;
            default:
                output = commandToken + " is not recognized as a command. Please try again.";
        }

        return output;
    }

    private String doProc(){
        String str = "PROC\n";
        return str;
    }

    private String doMem(){
        String str = "MEM\n";
        return str;
    }

    private String doLoad(){
        String str = "LOAD\n";
        return str;
    }

    private String doExe(){
        String str = "EXE\n";
        try {
            new ProcessControlBlock(queue, 25, "one");
            new ProcessControlBlock(queue, 1000, "two");
            Thread.sleep(400);
            new ProcessControlBlock(queue, 2000, "three");
            new ProcessControlBlock(queue, 100, "four");
            Thread.sleep(2000);
            new ProcessControlBlock(queue, 3000, "five");
            new ProcessControlBlock(queue, 1000, "six");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return str;
    }

    private String doReset(){
        String str = "RESET\n";
        return str;
    }

    private String doExit(){
        String str = "Exiting!";
        //Main.shutDown();
        return str;
    }


}

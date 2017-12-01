import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    //private final byte STATUS_NORMAL = 0;
    private final BlockingQueue queue;
    private final String LOAD_APPEND = ".txt";
    private Path jobFileDirectory;

    public CommandLine(BlockingQueue q, boolean debug){
        this.queue = q;
        setJobFileDirectory("C:\\Users\\jccon\\IdeaProjects\\cmsc312_os_simulator\\files"); //debug path bc I'm lazy
        if(debug) {
            this.userInput = new BufferedReader(new InputStreamReader(System.in));
            runDebugCLI();
        }
    }

    public void setJobFileDirectory(String dir){
        this.jobFileDirectory = Paths.get(dir);
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
                String filename = cli.next();

                try {
                    // if filename does not end in ".txt", append ".txt"
                    // please excuse the lame hack, sensei
                    if (!(filename.substring(filename.length() - 4, filename.length()).equals(LOAD_APPEND))) {
                        filename += LOAD_APPEND;
                    }
                } catch (StringIndexOutOfBoundsException e){
                    // filename is too short to contain ".txt"
                    // append ".txt"
                    filename += LOAD_APPEND;
                }
                Path filepath = Paths.get(this.jobFileDirectory.toString(), filename);
                System.out.println(filepath);
                output = doLoad(filepath);
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
                output = commandToken + " is not recognized as a command. Please try again.\n";
        }

        return output;
    }

    private String doProc(){
        ArrayList procs = new ArrayList();
        procs.addAll(Main.clock.allProcs);
        StringBuilder output = new StringBuilder();
        output.append("\n");

        for(int i = 0; i < procs.size(); i++){
            ProcessControlBlock temp = (ProcessControlBlock)procs.get(i);
            output.append(temp.getName() + " " + temp.getCyclesRemaining() +"\n");
        }

        Main.gui.displayText(output.toString());
        return output.toString();
    }

    private String doMem(){
        String str = "MEM\n";
        return str;
    }

    private String doLoad(Path filename){
        String str = "LOAD\n";
        /*new ProcessControlBlock(0, 25, "a");
        new ProcessControlBlock(0, 3, "b");
        new ProcessControlBlock(0, 20, "c");
        new ProcessControlBlock(0, 2500, "one");
        new ProcessControlBlock(0, 1000, "two");
        new ProcessControlBlock(0, 2000, "three");
        new ProcessControlBlock(0, 10000, "four");
        new ProcessControlBlock(0, 3000, "five");
        new ProcessControlBlock(0, 1000, "six");
*/
        FileParser.parse(filename);
        Main.gui.displayText("Loaded processes");
        return str;
    }

    private String doExe(){
        String str = "EXE\n";
        Main.clock.execute = 100;
        Main.gui.displayText("Running for 100 cycles");
        return str;
    }

    private String doReset(){
        String str = "RESET\n";
        return str;
    }

    private String doExit(){
        String str = "Exiting!";
        Main.shutDown();
        return str;
    }
}

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.NoSuchElementException;
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
    private Path jobFileDirectory = Paths.get("");

    public CommandLine(BlockingQueue q, boolean debug){
        this.queue = q;
        setJobFileDirectory(jobFileDirectory.toString() + "files");
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

        try {
            String commandToken = cli.next();

            switch (commandToken.toUpperCase()) {
                case "CD":
                    output = doCD();
                    break;
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
                case "DEBUG":
                    doDebug();
                    output = "debug";
                    break;
                default:
                    Main.gui.displayText(commandToken + " is not recognized as a command. Please try again.");
                    output = commandToken + " is not recognized as a command. Please try again.\n";
            }

            return output;
        }catch(NoSuchElementException e){
            Main.gui.displayText("Error!");
        }

        return "error";
    }

    private String doCD(){
        try{
            this.jobFileDirectory = Paths.get(cli.next());
        }catch (NoSuchElementException e){
            Main.gui.displayText("Please specify a directory to switch to.");
        }
        return "Changed directory";
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

    private String doLoad(){
        String str = "LOAD\n";
        try {
            String filename = cli.next();
            String procName = filename;
            boolean appended = false;   //on the advice of my counsel and pursuant to my rights under the 5th appendment,
                                // I respectfully decline to answer that question

            try {
                // if filename does not end in ".txt", append ".txt"
                // please excuse the lame hack, sensei
                if (!(filename.substring(filename.length() - 4, filename.length()).equals(LOAD_APPEND))) {
                    filename += LOAD_APPEND;
                    appended = true;
                }
            } catch (StringIndexOutOfBoundsException e) {
                // filename is too short to contain ".txt"
                // append ".txt"
                filename += LOAD_APPEND;
                appended = true;
            }

            if(!appended){
                procName = procName.substring(0, procName.length() - 4);
            }
            Path filepath = Paths.get(this.jobFileDirectory.toString(), filename);
            System.out.println(filepath);
            String loadType = cli.next().toUpperCase();
            switch (loadType) {
                case "PROG":
                    int[] cycleTime = new int[2];
                    for (int i = 0; i < cycleTime.length; i++) {
                        if (cli.hasNextInt()) {
                            cycleTime[i] = cli.nextInt();
                        } else {
                            Main.gui.displayText("Malformed load command reached. 2 integers are required " +
                                    "after program load commands.");
                        }
                    }

                    int waitCycles = RNGesus.randInRange(cycleTime[0], cycleTime[1]);
                    FileParser.parse(filepath, waitCycles, procName);
                    break;
                case "JOB":
                    FileParser.parse(filepath);
                    break;
                default:
                    Main.gui.displayText("Malformed load command reached. Please state what filetype.");
            }
            Main.gui.displayText("Loaded processes");
        }catch (NoSuchElementException e){
            System.out.println("log: error in doLoad()");
            Main.gui.displayText("Malformed LOAD command. Please try again.");
        }
        return str;
    }

    private String doExe(){
        String str = "EXE\n";
        int cycles = 2500;
        if(cli.hasNextInt()){
            try {
                cycles = cli.nextInt();
                Main.gui.displayText("Running for " + cycles + " cycles");
            }catch(NoSuchElementException e){
                System.out.println("log: NoSuchElementException in doExe()");
            }
        }
        else {
            Main.gui.displayText("Running...");
        }

        Main.clock.execute = cycles;
        return str;
    }

    private String doReset(){
        String str = "RESET\n";

        Main.clock.reset();
        return str;
    }

    private String doExit(){
        String str = "Exiting!";
        Main.shutDown();
        return str;
    }

    private void doDebug(){
        new ProcessControlBlock(0, 25, "a");
        new ProcessControlBlock(0, 3, "b");
        new ProcessControlBlock(0, 20, "c");
        new ProcessControlBlock(0, 25, "one");
        new ProcessControlBlock(0, 50, "two");
        new ProcessControlBlock(0, 40, "three");
        new ProcessControlBlock(0, 2, "four");
        new ProcessControlBlock(0, 10, "five");
        new ProcessControlBlock(0, 20, "six");

        Main.gui.displayText("Processes loaded");
    }
}

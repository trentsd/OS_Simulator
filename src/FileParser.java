import java.io.*;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileParser {

    public static void parse(Path filename){
        File f = filename.toFile();

        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            parseJob(r);
        } catch (FileNotFoundException e) {
            Main.gui.displayText("File " + filename.toAbsolutePath().toString() + " not found. Check to make " +
                    "sure working directory is correct or try changing directories with CD <directory> .");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Exception caught in parse(filename)");
        }
    }

    /**
     * Parses program files
     * @param filename
     * @param waitCycles
     */
    public static void parse(Path filename, int waitCycles, String procName){
        File f = filename.toFile();

        try(BufferedReader r = new BufferedReader(new FileReader(f))){
            parseProgram(r, waitCycles, procName);
        }catch (FileNotFoundException e) {
            Main.gui.displayText("File " + filename.toAbsolutePath().toString() + " not found. Check to make " +
                    "sure working directory is correct or try changing directories with CD <directory> .");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Exception caught in parse(filename, waitCycles)");
        }
    }

    private static String selectType(String line){
        Scanner in = new Scanner(line);

        if(in.hasNextInt()){
            in.close();
            return "PROGRAM";
        }
        else {
            in.close();
            return "JOB";
        }
    }

    private static void parseJob(BufferedReader r){
        String line;
        try {
            while ((line = r.readLine()) != null){
                Main.cli.interpretInput(line);
            }
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("Exception caught in parseJob()");
        }
    }

    private static void parseProgram(BufferedReader r, int waitCycle, String procName){
        String line;
        LinkedList commandQueue = new LinkedList<Integer>();
        LinkedList outQ = new LinkedList<String>();
        try {
            Scanner in = new Scanner(r.readLine());
            int memReq = in.nextInt();
            while ((line = r.readLine()) != null) {
                in = new Scanner(line);

                String commandToken = in.next();

                switch(commandToken.toUpperCase()){
                    case "CALCULATE":
                        int[] randArray = new int[2]; // The Truth is not for all men, but only for those who seek it.
                        for(int i = 0; i < randArray.length; i++){
                            if(in.hasNextInt()){
                                randArray[i] = in.nextInt();
                            }
                            else{
                                Main.gui.displayText("Malformed CALCULATE command reached. 2 integers are required " +
                                        "after program load commands.");
                            }
                        }

                        int numCalculate = RNGesus.randInRange(randArray[0], randArray[1]);

                        for(int i = 0; i < numCalculate; i++)
                            commandQueue.add(Commands.CALCULATE);
                        break;
                    case "YIELD":
                        commandQueue.add(Commands.YIELD);
                        break;
                    case "I/O":
                        commandQueue.add(Commands.IO);
                        break;
                    case "OUT":
                        in.useDelimiter("\"");
                        Pattern p = Pattern.compile("\"([^\"]*)\"");
                        Matcher m = p.matcher(line);
                        if(m.find()) {
                            System.out.println(m.group(1));
                            String outText = m.group(1);
                            outQ.add(outText);
                            commandQueue.add(Commands.OUT);
                        }
                        else{
                            Main.gui.displayText("Malformed OUT command reached. Please check syntax");
                        }
                        break;
                    default:
                        String outText = "Malformed command reached. Please check syntax.";
                        Main.gui.displayText(outText);
                }
            }

            new ProcessControlBlock(commandQueue, procName, memReq, waitCycle, outQ);
        } catch(IOException e){
            e.printStackTrace();
            System.out.println("Exception caught in parseProgram()");
        }
    }
}

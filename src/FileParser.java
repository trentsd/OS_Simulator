import java.io.*;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.nio.file.Path;

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
    public static void parse(Path filename, int waitCycles){
        File f = filename.toFile();

        try(BufferedReader r = new BufferedReader(new FileReader(f))){
            parseProgram(r, waitCycles);
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

    private static void parseProgram(BufferedReader r, int waitCycle){
        String line;
        LinkedList commandQueue = new LinkedList<Integer>();
        try {
            Scanner in = new Scanner(r.readLine());
            int memReq = in.nextInt();
            while ((line = r.readLine()) != null) {
                in = new Scanner(line);

                String commandToken = in.next();

                switch(commandToken.toUpperCase()){
                    case "CALCULATE":
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
                        try {
                            String outText = in.next();
                            Main.gui.displayText(outText);
                        } catch(NoSuchElementException e){
                            String outText = "Malformed OUT command reached. Please use correct syntax.";
                            Main.gui.displayText(outText);
                        }
                        break;
                    default:
                        String outText = "Malformed command reached. Please check syntax.";
                        Main.gui.displayText(outText);
                }
            }
        } catch(IOException e){
            e.printStackTrace();
            System.out.println("Exception caught in parseProgram()");
        }
    }
}

import java.io.*;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.nio.file.Path;

public class FileParser {

    public static void parse(Path filename){
        File f = filename.toFile();

        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line = r.readLine();
            String type = selectType(line);

            if(type.equals("PROGRAM")){
                parseProgram(line, r);
            }
            else if(type.equals("JOB")){
                parseJob(line, r);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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

    private static void parseJob(String firstLine, BufferedReader r){
        Main.cli.interpretInput(firstLine);
        String line;
        try {
            while ((line = r.readLine()) != null){
                Main.cli.interpretInput(line);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private static void parseProgram(String line, BufferedReader r){
        Scanner in = new Scanner(line);
        int memReq = in.nextInt();
        try {
            while ((line = r.readLine()) != null) {
                in = new Scanner(line);

                String commandToken = in.next();

                switch(commandToken.toUpperCase()){
                    case "CALCULATE":
                        break;
                    case "YIELD":
                        break;
                    case "I/O":
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
        }
    }
}

import java.io.*;
import java.util.Scanner;

public class FileParser {

    public void parse(String filename){
        File f = new File(filename);

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
                        
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}

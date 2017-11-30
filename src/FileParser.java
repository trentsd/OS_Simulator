import java.io.*;
import java.util.Scanner;

public class FileParser {
    public static void parse(String filename){
        File f = new File(filename);

        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line = r.readLine();
            String type = selectType(line);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String selectType(String line){
        Scanner in = new Scanner(line);

        if(in.hasNextInt()){
            return "PROGRAM";
        }
        else return "JOB";
    }

    private static void parseJob(){

    }

    private static void parseProgram(){

    }
}

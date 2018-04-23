
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class InitiateListCheck {
    public static void main(String[] args) throws IOException{
       new InitiateListCheck(args);
    }

    File file;
    List<String> list;
    Connect connect;
    public InitiateListCheck(String[] args) throws IOException {
        this.file = new File(args[0]); //uses first args to declare the File variable to the args
        this.list = new ArrayList();

        BufferedReader br = new BufferedReader(new FileReader(file));

        String outputLine;

        System.out.println("Adding url from predefined list:");
        while((outputLine = br.readLine()) != null){
            list.add(outputLine);
            System.out.println("Added: " + outputLine);
        }

        connect = new Connect(this.list, Integer.parseInt(args[1]));


    }

}


package Sapphire;

import java.util.HashMap;
import java.util.Scanner;
import java.io.*;
import java.nio.file.Files;
import Sapphire.Menu.Menu;

public class StringReader {
    static String inputfile;
    static HashMap<String,String> strings;
    public StringReader() throws FileNotFoundException{
        inputfile = System.getProperty("user.dir")+"/resources/strings.input";      // change secret.input to strings.input
        strings = new HashMap<String,String>();
        InputStream is;
        try {
            File input = new File(inputfile);
            if(!input.exists()){
                System.err.println("Input file not found");
                return;
            }
            is = new FileInputStream(input);
        } catch (Exception e) {
            System.err.println("Input file not found");
            return;
        }
        Scanner inputReader = new Scanner(is);
        Scanner userInput = new Scanner(System.in);
        while(inputReader.hasNext()){
            String nextLine = inputReader.nextLine();
            if(nextLine.length()>0){
                if(nextLine.charAt(0)=='#'){continue;}  //basically a comment
                String[] seperatedLine = nextLine.split("::");
                if(seperatedLine.length==1){
                    if(seperatedLine[0].equals("ClientAuthToken")){
                        strings.put(seperatedLine[0],"");    
                        continue;
                    }
                    System.out.println("Please provide an input for "+seperatedLine[0]);
                    System.out.println("All inputs can be changed later in the config menu");
                    String input = userInput.next();
                    strings.put(seperatedLine[0],input);
                    writeInput(seperatedLine[0],input);
                }else if(seperatedLine[1].equals("http://:44344")){ //Add regex to verify IP input
                    System.out.println("Please provide the IP of the Server\nPlease note that errors will need to be changed manually");
                    String ip = "http://"+userInput.next()+":44344";
                    strings.put(seperatedLine[0],ip);
                    writeInput(seperatedLine[0], ip);
                }else{
                    strings.put(seperatedLine[0],seperatedLine[1]);
                }
            }
        }
        inputReader.close();
    }
    public String getString(String stringName){
        return strings.get(stringName);
    }

    public static void writeInput(String key,String val){
        try {
            InputStream is = new FileInputStream(new File(inputfile));
            Scanner scan = new Scanner(is);
            String updated = "";
            while(scan.hasNext()){
                String nextLine = scan.nextLine();
                String[] seperatedLine = nextLine.split("::");
                //System.out.println(seperatedLine[0]);
                if(seperatedLine[0].equals(key)){
                    updated = updated.concat(key+"::"+val+"\n");
                }else{
                    updated = updated.concat(nextLine+"\n");
                }
            }
            scan.close();
            //System.out.println(updated);
            Files.writeString(new File(inputfile).toPath(), updated);
        } catch (Exception e) {
            System.err.println("failed to update input file");
            return;
        }
        
    }
}

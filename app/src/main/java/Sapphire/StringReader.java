package Sapphire;

import java.util.HashMap;
import java.util.Scanner;
import java.io.*;
import java.nio.file.Files;

public class StringReader {
    static String inputfile;
    static HashMap<String,String> strings;
    public StringReader() throws FileNotFoundException{
        inputfile = System.getProperty("user.dir")+"/resources/secret.input";      // change secret.input to strings.input
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
        while(inputReader.hasNext()){
            String nextLine = inputReader.nextLine();
            if(nextLine.length()>0){
                if(nextLine.charAt(0)!='#'){ //basically a comment
                    String[] seperatedLine = nextLine.split("::");
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
                if(seperatedLine[0]==key){
                    updated = updated.concat(key+"::"+val+"\n");
                }else{
                    updated = updated.concat(nextLine+"\n");
                }
            }
            scan.close();
            Files.writeString(new File(inputfile).toPath(), updated);
        } catch (Exception e) {
            System.err.println("failed to update input file");
            return;
        }
        
    }
}

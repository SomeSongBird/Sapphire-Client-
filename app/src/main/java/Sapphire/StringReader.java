package Sapphire;

import java.util.HashMap;
import java.util.Scanner;
import java.io.*;
import java.nio.file.Files;

public class StringReader {
    static String inputfile;
    static String inputPath;
    static HashMap<String,String> strings;
    public StringReader() throws FileNotFoundException{
        inputPath = System.getProperty("user.dir")+"/resources/";
        inputfile = inputPath+"strings.input";      // change secret.input to strings.input
        strings = new HashMap<String,String>();
        strings.put("TemporaryFilePath",inputPath);
        strings.put("ExternalDirectoryFilesPath",inputPath+"/externDirs/");
        File extern = new File(strings.get("ExternalDirectoryFilesPath"));
        if(!extern.exists()){
            try{
                extern.createNewFile();
            }catch(IOException ioe){
                System.out.println("could not create externalDirectory storage file");
            }
        }
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
                    while(true){
                        System.out.println("Please provide an input for "+seperatedLine[0]);
                        System.out.println("All inputs can be changed later in the config menu");
                        String input = userInput.next();
                        File f = new File(input);
                        if(f.exists()){
                            if(f.isDirectory()){
                                strings.put(seperatedLine[0],input);
                                writeInput(seperatedLine[0],input);
                                break;
                            }
                        }
                        System.out.println(input+" is not a valid directory");
                    }
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

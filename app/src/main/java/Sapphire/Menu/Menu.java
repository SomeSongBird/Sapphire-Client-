package Sapphire.Menu;

//#region imports
import java.io.File;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import org.checkerframework.checker.units.qual.Temperature;

import Sapphire.*;
//#endregion imports
import Sapphire.Utilities.DirectoryWalker;
import Sapphire.Utilities.MockDir;

public class Menu implements Runnable{
    //#region init
    Scanner userInput = new Scanner(System.in);
    public Boolean shutdown = false;
    Client mc;
    public Menu(Client client){
        mc = client;
    }
    public void run(){
        mainMenu();
        return;
    }
    //#endregion init

    //#region helpers
    private boolean cancel(String userInput){
        String ui = userInput.toLowerCase();
        if(ui.equals("cancel")||ui.equals("exit")||ui.equals("quit")){
            return true;
        }
        return false;
    }

    private String getUserInput(){
        String in = userInput.nextLine();
        return in;
    }

    private String parseDirectory(int id){
       // HashMap<Integer,String> otherClients = mc.getOtherClients();
        MockDir topDir = null;
        String input;
/* 
        //device selection
        System.out.println("ID: 0 | This device");
        for(int id : otherClients.keySet()){
            System.out.println("ID: "+id+" | "+otherClients.get(id));
        }

        //parser setup
        input = getUserInput();
        if(cancel(input)){return null;}
        if(input.equals("0")){
            DirectoryWalker walker = new DirectoryWalker(mc.getDirPermissions());
            topDir = new MockDir(walker.fileStructure,null);
        }else{
            try{
                int id = Integer.parseInt(input);
                topDir = mc.readExternDir(id);
            }catch(Exception e){
                return;
            }
        }
 */
        if(id==0){
            DirectoryWalker walker = new DirectoryWalker(mc.getDirPermissions());
            topDir = new MockDir(walker.fileStructure);
        }else{
            topDir = mc.readExternDir(id);
        }
        MockDir current = topDir;

        //parser
        //clearScreen();
        System.out.println(current.fullName);
        while(true){
            input = getUserInput().strip();
            if(cancel(input)) return null;
            String[] splitInput = input.split(" ",2);
            switch(splitInput[0]){
                case "ls":
                    System.out.println(current);
                    break;
                case "cd":
                    if(splitInput.length>1){
                        //System.out.println(splitInput[1]);
                        if(splitInput[1].equals("..")){
                            current = current.parent;
                        }else {
                            for(MockDir temporary : current.nextLayer){
                                if(temporary.name.equals(splitInput[1])&&temporary.isDirectory()){
                                    current = temporary;
                                }
                            }
                            /* MockDir temporary = current.getNextDir(splitInput[1]);
                            //System.out.println(temporary.fullName);
                            if(temporary.isDirectory()||true){
                                current = temporary; 
                                //System.out.println(current.fullName);
                            }  */
                                
                        }
                    }
                    System.out.println(current.fullName);
                    break;
                case "select":
                    if(splitInput.length>1){
                        MockDir temporary = current.getNextDir(splitInput[1]);
                        if(!temporary.isDirectory()){
                            return temporary.getPath();
                        }else{
                            System.out.println("unable to return that directory");
                        }
                    }    
            }
        }
    }

    public static void waitForNextKeystroke(){
        try{
            while(System.in.available()==0){
                Thread.sleep(100);
            }
            System.in.read();
        }catch(Exception e){}
    }

    private static void clearScreen() {  
        System.out.print("\033[H\033[2J");  
        System.out.flush();  
    } 
    //#endregion helpers

    //#region MainMenu 
    private void mainMenu(){
        System.out.println("Welcome to the Sapphire Client CLI");
        boolean printMenu = true;
        while(true){
            if(Thread.currentThread().isInterrupted()) return;
            if(printMenu){    
                clearScreen();
                String connection = (mc.connected)? "\033[32mConnected \033[0m" : "\033[31mNot Connected\033[0m";
                System.out.println("Status: "+connection);
                System.out.println("Enter the number of your action:");
                System.out.println("\t1. Transfer File");
                System.out.println("\t2. Start Remote Application");
                System.out.println("\t3. Refersh Directories");
                System.out.println("\t4. Configuration");
                System.out.println("\t5. Refresh");
                printMenu = false;
            }
            
            switch(getUserInput()){
                case "1":
                    fileTransfer();
                    break;
                case "2":
                    remoteStart();
                    break;
                case "3":
                    refreshDirectories();
                    break;
                case "4":
                    config();
                    break;
                case "5":
                    break;
                case "quit": // replace with return statement if resume execution is implemented
                case "shutdown":
                    shutdown = true;
                    return;
                case "h":
                case "H":
                case "help":
                case "Help":
                    break;
                default:
                    continue;
            }
            printMenu = true;
        }
    }
    //#endregion MainMenu

    //#region FileTransfer
    private void fileTransfer(){
        while(true){
            if(Thread.currentThread().isInterrupted()) return;
            clearScreen();
            System.out.println("Send or Pull a file:\n\t1.  Send\n\t2.  Pull\n\t3.  Go Back");
            String input = getUserInput();
            if(cancel(input)){break;}
            try {
                switch(input){
                    case "1":
                        sendFile();
                        break;
                    case "2":
                        pullFile();
                        break;
                    case "3":
                        return;
                }
            } catch (Exception e) {
                System.out.println("Error :"+e.getMessage());
                waitForNextKeystroke();
                return;
            }
        }
        clearScreen();
    }

    private void pullFile() throws Exception{
        int deviceID = -1;
        String filename = "";
        String localName = "";
        clearScreen();
        while(true){
            if(Thread.currentThread().isInterrupted()) return;
            System.out.println("What device would you like to pull from?");
            HashMap<Integer,String> clientList = mc.getOtherClients();
            for(int id : clientList.keySet()){
                System.out.println("ID: "+id+" | "+clientList.get(id));
            }
            String sdeviceID = getUserInput();
            if(cancel(sdeviceID)){
                return;
            }
            try{
                deviceID = Integer.parseInt(sdeviceID);
                clearScreen();
                break;
            }catch(Exception e){
                clearScreen();
                System.out.println("Invalid device ID");
            }
        }
        while(true){
            System.out.println("Selected device: "+deviceID);
            System.out.println("Enter the full path and name of the file you want to pull");
            filename = parseDirectory(deviceID);
            if(filename==null) return;
            break;
        }
        clearScreen();
        while(true){
            System.out.println("Selected device: "+deviceID+"\nSelected file: "+filename);
            System.out.println("Enter local destination and name");
            localName = getUserInput();
            if(cancel(localName)){
                return;
            }
            break;
        }
        mc.pullFile(deviceID, filename, localName);
    }

    private void sendFile() throws Exception{
        int deviceID = -1;
        String filename = "";
        String desinationName = "";
        clearScreen();
        while(true){
            if(Thread.currentThread().isInterrupted()) return;
            System.out.println("What device would you like to send to?");
            HashMap<Integer,String> clientList = mc.getOtherClients();
            for(int id : clientList.keySet()){
                System.out.println("ID: "+id+" | "+clientList.get(id));
            }
            String sdeviceID = getUserInput();
            if(cancel(sdeviceID)){
                return;
            }
            try{
                deviceID = Integer.parseInt(sdeviceID);
                clearScreen();
                break;
            }catch(Exception e){
                clearScreen();
                System.out.println("Invalid device ID");
            }
        }
        while(true){
            System.out.println("Selected device: "+deviceID);
            // replace with dir parser
            System.out.println("Enter the full path and name of the file you want to send");
            filename = parseDirectory(0);
            if(cancel(filename)){
                return;
            }
            if(filename.charAt(0)=='/'||filename.charAt(0)=='\\'){
                filename=filename.substring(1,filename.length());
            }
            File f = new File(mc.getDirPermissions()+filename);
            if(!f.exists()||filename.contains("..")){
                System.out.println("Invalid file name or path");
            }else{
                break;
            }
        }
        clearScreen();
        while(true){
            System.out.println("Selected device: "+deviceID+"\nSelected file: "+filename);
            System.out.println("Enter destination path and name");
            desinationName = getUserInput();
            if(cancel(desinationName)){
                return;
            }
            break;
        }
        mc.sendFile(deviceID, desinationName, filename);
        waitForNextKeystroke();
    }
    
    //#endregion FileTransfer

    //#region RemoteStart
    private void remoteStart(){
        System.out.println("Not implemented");
        waitForNextKeystroke();
    }    

    //#endregion RemoteStart

    //#region RefreshDirectories
    private void refreshDirectories(){

    }
    //#endregion RefreshDirectories

    //#region config
    private void config(){
        boolean printMenu=true;
        while(true){
            if(Thread.currentThread().isInterrupted()) return;
            if(printMenu){
                printMenu = false;
                clearScreen();
                System.out.println("Configuration Menu");
                System.out.println("Enter the number of your action:");
                System.out.println("\t1. Get Authentication");
                System.out.println("\t2. Set Directory Permissions");
                System.out.println("\t3. Set Remote Start Permissions");
                System.out.println("\t4. Go Back");
            }
            String input = getUserInput();
            if(cancel(input))return;
            switch(input){
                case "1":
                    generateAuthenticationKey();
                    break;
                case "2":
                    setDirPermissions();
                    break;
                case "3":
                    setRSPermissions();
                    break;
                case "4":
                    return;
                default:
                continue;
            }
            printMenu = true;
        }
    }

    private void generateAuthenticationKey(){
        System.out.println("Get current(c) key or generate new(n) key?");
        while(true){
            String input = getUserInput();
            if(cancel(input)) return;
            switch(input.toLowerCase()){
                case "new":
                case "n":
                    char[] characters = new char[7];
                    Random r = new Random();
                    for(int i=0;i<7;i++){
                        characters[i] = (char)(r.nextInt(126-33)+33);
                    }
                    String generatedString = new String(characters);
                    mc.setAuthToken(generatedString);
                    System.out.println("Auth Token : "+mc.getAuthToken());
                    waitForNextKeystroke();
                    return;
                case "current":
                case "c":
                    System.out.println("Auth Token : "+ mc.getAuthToken());
                    waitForNextKeystroke();
                    return;
            }
        }
    }
    private void setDirPermissions(){
        while(true){
            System.out.println("Current accessible directory: "+mc.getDirPermissions());
            System.out.println("Enter the new accessible directory");
            String input = getUserInput();
            if(cancel(input)) return;
            File dir = new File(input);
            if(dir.exists()){
                if(dir.isDirectory()){
                    if(dir.canRead() && dir.canWrite()){
                        mc.setDirPermissions(input);

                        System.out.println("Successfuly updated managed accessible directory");
                        waitForNextKeystroke();
                        return;
                    }else{
                        System.out.println("Unauthorized to access selected directory");
                    }
                }else{
                    System.out.println("Entered path is not a directory");
                }
            }else{
                System.out.println("Entered path does not exist");
            }
            waitForNextKeystroke();
        }
    }
    private void setRSPermissions(){
        //Not implemented
    }
    //#endregion config

}

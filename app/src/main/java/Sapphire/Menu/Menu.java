package Sapphire.Menu;

//#region imports
import java.nio.file.Files;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import Sapphire.*;
//#endregion imports

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
        if(ui.equals("cancel")||ui.equals("exit")){
            return true;
        }
        return false;
    }

    private String getUserInput(){
        String in = userInput.next();
        return in;
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
                System.out.println("Enter the number of your action:");
                System.out.println("\t1. Transfer File");
                System.out.println("\t2. Start Remote Application");
                System.out.println("\t3. Refersh Directories");
                System.out.println("\t4. Configuration");
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
            System.out.println("Send or Pull a file:\n\t1.  Send\n\t2.  Pull");
            String input = getUserInput();
            if(cancel(input)){
                break;
            }
            try {
                switch(input){
                    case "1":
                        pullFile();
                        break;
                    case "2":
                        sendFile();
                        break;
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
            // display all 
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
            System.out.println("Enter the full path and name of the file you want to pull");
            // for now, assume input is correct
            filename = getUserInput();
            if(cancel(filename)){
                return;
            }
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
            // display all 
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
            // for now, assume input is correct
            filename = getUserInput();
            if(cancel(filename)){
                return;
            }
            break;
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
        mc.sendFile(deviceID, filename, desinationName);
    }
    
    //#endregion FileTransfer

    //#region RemoteStart
    private void remoteStart(){

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
                    byte[] array = new byte[7]; // length is bounded by 7
                    new Random().nextBytes(array);
                    String generatedString = new String(array);
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

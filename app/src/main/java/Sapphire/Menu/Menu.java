package Sapphire.Menu;

//#region imports
import java.util.HashMap;
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
        System.out.println("Welcome to the server's GUI");
        boolean printMenu = true;
        while(true){
            if(printMenu){    
                clearScreen();
                System.out.println("Enter the number of your action:");
                System.out.println("\t1. Manage Registered Devices");
                System.out.println("\t2. View Activity Logs");
                System.out.println("\t3. Make New Request");
                printMenu = false;
            }
            
            switch(getUserInput()){
                case "1":
                    
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

    //#region activityLog
    private void viewActivityLog(){
        /* idk hwo I'm going to view data from the logger, hell I dont even know how I'm storing that yet */
        clearScreen();
        System.out.println("Not yet Implemented");
        waitForNextKeystroke();
        return;
    }

    //#endregion activityLog

    //#region customRequests
    private void makeNewRequest(){
        /*  */
        boolean printMenu = true;
        while(true){
            if(printMenu){
                clearScreen();
                System.out.println("Enter the number of your action:");
                System.out.println("\t1. Update Directory Structure");
                System.out.println("\t2. Fetch File");
                System.out.println("\t3. Send File");
                System.out.println("\t4. Return to Main Menu");
                printMenu = false;
            }
            try{
                switch(getUserInput()){
                    case "1":
                    System.out.println("not implemented\nPress Enter to return");
                    waitForNextKeystroke();
                    break;
                    case "2":
                    pullFile();
                    break;
                    case "3":
                        sendFile();
                        break;
                    case "4":
                    return;
                    case "h":
                    case "H":
                    case "help":
                    case "Help":
                    break;
                    default:
                    continue;
                }    
            }catch(Exception e){
                System.out.println("An error occured, try again another time");
                waitForNextKeystroke();
            }
            printMenu = true;
        }
    }

    private void pullFile() throws Exception{
        int deviceID = -1;
        String filename = "";
        String localName = "";
        clearScreen();
        while(true){
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
    
    //#endregion customRequests
}

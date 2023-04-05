
package Sapphire;

import java.io.FileNotFoundException;
import java.nio.charset.CoderResult;

import Sapphire.Menu.*;

public class MainController {
    private static Client client;
    private static Menu menu;
    
    public static boolean init(){
        try{
            client = new Client(new StringReader());
            menu = new Menu(client);
        }catch(FileNotFoundException e){
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        if(!init()){
            System.err.println("unable to start client, make sure everything is setup correctly.");
            return;
        }
        Thread thread = new Thread(menu);
        thread.start();
        while(!menu.shutdown){
            try {
                client.update();
                Thread.sleep(2500);
            } catch (Exception e) {
                System.err.println("Error: "+e.getMessage());
            }
        }
    }
}

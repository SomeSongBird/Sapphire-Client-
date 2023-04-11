
package Sapphire;

import java.io.FileNotFoundException;

import Sapphire.Menu.*;

public class MainController {
    private Client client;
    private Menu menu;
    
    private boolean init(){
        try{
            client = new Client(new StringReader());
            menu = new Menu(client);
        }catch(FileNotFoundException e){
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    private void loop(){
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
                thread.interrupt();
                break;
            }
        }
        System.out.println("shutting down");
    }
    public static void main(String[] args) {
        MainController mc = new MainController();
        if(mc.init()){
            mc.loop();
        }
    }
}

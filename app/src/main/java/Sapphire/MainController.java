
package Sapphire;

import java.io.FileNotFoundException;

import Sapphire.Menu.*;

public class MainController {
    private Client client;
    private Menu menu;
    
    private boolean init(){
        try{
            client = new Client(new StringReader());
            client.update();
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

        int refreshClientList = 0;
        while(!menu.shutdown){
            try {
                client.update();
                if(refreshClientList==0){
                    client.getClientList();
                    refreshClientList = 12;
                }
                refreshClientList--;
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

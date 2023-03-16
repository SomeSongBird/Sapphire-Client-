
package Sapphire;

public class MainController {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        System.out.println(new MainController().getGreeting());
    }
}

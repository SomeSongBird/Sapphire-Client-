/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package Sapphire;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    @Test void appHasAGreeting() {
        MainController classUnderTest = new MainController();
        assertNotNull(classUnderTest.getGreeting(), "app should have a greeting");
    }
}

import gui.LoginFrame;
import javax.swing.SwingUtilities;

/**
 * Main.java - Entry point for the Hall Booking Management System.
 *
 * This class simply launches the login screen.
 * All user interaction happens through the JFrame-based GUI.
 *
 * How to compile (from the project root directory):
 *   javac -d out src/model/*.java src/util/*.java src/gui/*.java src/Main.java
 *
 * How to run:
 *   java -cp out Main
 */
public class Main {

    public static void main(String[] args) {
        // Use SwingUtilities.invokeLater to ensure the GUI is created on the
        // Event Dispatch Thread (EDT) — this is the correct way to start a Swing app
        SwingUtilities.invokeLater(() -> {
            new LoginFrame();
        });
    }
}

import java.util.*;

/**
 * Main class that coordinates the entire UNO game
 * Entry point of the application
 */
public class Main {
    private static Menu menu;
    private static boolean gameRunning = true;

    /**
     * Main method - entry point of the program
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        menu = new Menu();

        // Main program loop
        while (gameRunning) {
            showMainMenu();
        }

        System.out.println("Programm beendet. Auf Wiedersehen!");
    }

    /**
     * Shows main menu and handles user's choice
     */
    private static void showMainMenu() {
        int choice = menu.showMainMenu();

        switch (choice) {
            case 1:
                startNewGame();
                break;
            case 2:
                menu.displayRules();
                break;
            case 3:
                if (menu.confirmQuit()) {
                    gameRunning = false;
                }
                break;
            default:
                System.out.println("Ungültige Auswahl! Bitte wähle 1-3.");
                break;
        }
    }

    /**
     * Starts a new UNO game
     */
    private static void startNewGame() {
        try {
            // Initialize the game
            Initialization initialization = new Initialization();
            Initialization.GameSetup gameSetup = initialization.initializeGame();

            // Run the game
            Run gameRunner = new Run(gameSetup);
            gameRunner.runGame();

        } catch (Exception e) {
            System.err.println("Ein Fehler ist aufgetreten: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Zurück zum Hauptmenü...");
        }
    }
}

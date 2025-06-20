import java.util.*;

/**
 * Main class that coordinates the entire UNO game.
 * Entry point of the application.
 */
public class Main {
    // Single shared Scanner instance to avoid resource leaks
    private static final Scanner scanner = new Scanner(System.in);

    // Menu handler instance for user interaction
    private static final Menu menu = new Menu();

    // Flag to control the main application loop
    private static boolean gameRunning = true;

    /**
     * Main method - entry point of the program.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        System.out.println("🎮 Welcome to UNO!");
        System.out.println("=================");

        // Main loop continues until the player chooses to exit
        while (gameRunning) {
            try {
                showMainMenu();
            } catch (Exception e) {
                System.err.println("An unexpected error occurred: " + e.getMessage());
                System.out.println("The game will continue...\n");
            }
        }

        // Close scanner before exiting to release resources
        scanner.close();
        System.out.println("Program ended. Goodbye!");
    }

    /**
     * Displays the main menu and handles user input.
     */
    private static void showMainMenu() {
        int choice = menu.showMainMenu();

        switch (choice) {
            case 1:
                runGameSessionLoop(); // Start one or more game sessions
                break;
            case 2:
                menu.displayRules(); // Show game rules
                break;
            case 3:
                if (menu.confirmQuit()) { // Confirm before quitting
                    gameRunning = false;
                }
                break;
            default:
                System.out.println("Invalid selection! Please choose between 1 and 3.");
                break;
        }
    }

    /**
     * Handles playing multiple rounds in one session if the user chooses to play again.
     */
    private static void runGameSessionLoop() {
        boolean playAgain;

        // Loop for replaying the game as long as the player wants
        do {
            playAgain = startNewGame();
        } while (playAgain);
    }

    /**
     * Starts a new UNO game session.
     * Initializes the game and manages error handling.
     *
     * @return true if the player wants to play again, false otherwise.
     */
    private static boolean startNewGame() {
        try {
            System.out.println("\n🚀 Starting a new game...");

            // Game initialization: players, deck, etc.
            Initialization initialization = new Initialization();
            Initialization.GameSetup gameSetup = initialization.initializeGame();

            // Handle failed initialization
            if (gameSetup == null) {
                System.out.println("Game initialization failed. Returning to main menu.");
                return false;
            }

            // Ensure valid player count
            if (gameSetup.players == null || gameSetup.players.size() < 2) {
                System.out.println("Not enough players. At least 2 are required to start the game.");
                return false;
            }

            // Ensure the deck is correctly initialized
            if (gameSetup.deck == null) {
                System.out.println("Deck could not be initialized.");
                return false;
            }

            // Run the actual game
            Run gameRunner = new Run(gameSetup);
            gameRunner.runGame();

            // Ask if the player wants to play another game
            System.out.print("\nWould you like to play another round? (y/n): ");
            String playAgain = scanner.nextLine().trim().toLowerCase();
            return playAgain.equals("y") || playAgain.equals("yes");

        } catch (IllegalArgumentException e) {
            System.err.println("Configuration error: " + e.getMessage());
            System.out.println("Please check your input and try again.");
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());

            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }

            // Ask user if they want detailed error output
            System.out.print("Would you like to see the full error details? (y/n): ");
            try {
                String showDetails = scanner.nextLine().trim().toLowerCase();
                if (showDetails.equals("y") || showDetails.equals("yes")) {
                    e.printStackTrace();
                }

            } catch (Exception inputError) {
                // If input fails, just print stack trace
                e.printStackTrace();
            }

            System.out.println("Returning to main menu...");
        }

        return false; // On error or invalid input, return to main menu
    }

    /**
     * Provides access to the shared Scanner instance.
     * Useful for consistent input handling across the application.
     *
     * @return the shared Scanner instance.
     */
    public static Scanner getScanner() {
        return scanner;
    }
}
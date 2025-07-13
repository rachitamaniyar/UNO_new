import java.util.*;

/**
 * Main class that coordinates the entire UNO game, with database integration.
 * Entry point of the application.
 */
public class Main {

    // Shared Scanner instance for all input
    private static final Scanner scanner = new Scanner(System.in);

    // Database manager for score persistence
    private static ScoreDatabaseManager dbManager = null;
    private static int currentSessionId = 0;
    private static String currentGameVariant = "Standard";

    // Flag to control the main application loop
    private static boolean gameRunning = true;

    public static void main(String[] args) {
        System.out.println("🎮 Welcome to UNO!");
        System.out.println("=================");

        // Create a single Menu instance to be passed around
        Menu gameMenu = new Menu(scanner);

        // Initialize database connection
        try {
            dbManager = new ScoreDatabaseManager("uno_scores.sqlite");
            System.out.println("✅ Database initialized successfully.");
        } catch (Exception e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
            dbManager = null;
        }

        // Main loop continues until the player chooses to exit
        while (gameRunning) {
            try {
                showMainMenu(gameMenu);
            } catch (Exception e) {
                System.err.println("An unexpected error occurred: " + e.getMessage());
                System.out.println("The game will continue...\n");
            }
        }

        // Close scanner and menu before exiting to release resources
        scanner.close();
        gameMenu.close();

        // Close database connection if it exists
        if (dbManager != null) {
            try {
                dbManager.close();
                System.out.println("✅ Database connection closed.");
            } catch (Exception e) {
                System.err.println("⚠️ Error closing database: " + e.getMessage());
            }
        }

        System.out.println("Program ended. Goodbye!");
    }

    /**
     * Displays the main menu and handles user input.
     */
    private static void showMainMenu(Menu menu) {
        int choice = menu.showMainMenu();
        switch (choice) {
            case 1:
                runGameSessionLoop(menu);
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
                System.out.println("Invalid selection! Please choose between 1 and 3.");
                break;
        }
    }

    /**
     * Handles playing multiple rounds in one session if the user chooses to play again.
     * @param menu The Menu instance for game interactions.
     */
    private static void runGameSessionLoop(Menu menu) {
        boolean playAgain;
        do {
            playAgain = startNewGame(menu);
        } while (playAgain);
    }

    /**
     * Starts a new UNO game session, including database integration.
     * Initializes the game and manages error handling.
     * @param menu The Menu instance for game interactions.
     * @return true if the player wants to play again, false otherwise.
     */
    private static boolean startNewGame(Menu menu) {
        try {
            System.out.println("\n🚀 Starting a new game...");

            // Game initialization: players, deck, etc.
            Initialization initialization = new Initialization(scanner);
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

            // --- DATABASE INTEGRATION: Create a new session in the database ---
            currentGameVariant = gameSetup.specialRulesEnabled ? "Special Rules" : "Standard";
            if (dbManager != null) {
                String[] playerNames = gameSetup.players.stream()
                        .map(Player::getName)
                        .toArray(String[]::new);
                try {
                    currentSessionId = dbManager.createNewSession(playerNames, currentGameVariant);
                    System.out.println("📊 Session #" + currentSessionId + " created in database.");
                } catch (Exception e) {
                    System.err.println("❌ Could not create database session: " + e.getMessage());
                    dbManager = null;
                }
            }

            // Run the actual game
            Run gameRunner = new Run(
                    gameSetup,
                    scanner,
                    menu,
                    dbManager,
                    currentSessionId,
                    currentGameVariant
            );
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
                e.printStackTrace();
            }
            System.out.println("Returning to main menu...");
        }
        return false; // On error or invalid input, return to main menu
    }
}

import java.util.*;
import java.io.*;

/**
 * Handles all menu interactions and user interface
 * Manages game menus, rule display, and player input
 */
public class Menu implements AutoCloseable {
    private Scanner scanner;

    // Constants for better maintainability
    private static final String RULES_FILE = "uno_rules.txt";
    private static final int MIN_PLAYERS = 0;
    private static final int MAX_PLAYERS = 4;
    private static final int DEFAULT_PLAYERS = 1;
    private static final int MIN_DIFFICULTY = 1;
    private static final int MAX_DIFFICULTY = 3;
    private static final int DEFAULT_DIFFICULTY = 2;
    private static final int MIN_MENU_OPTION = 1;
    private static final int MAX_MENU_OPTION = 3;
    private static final int MIN_GAME_MENU_OPTION = 1;
    private static final int MAX_GAME_MENU_OPTION = 6;
    private static final double BOT_CHALLENGE_PROBABILITY = 0.3;

    private static final String SEPARATOR = "=".repeat(60);
    private static final String SHORT_SEPARATOR = "=".repeat(50);

    public Menu() {
        scanner = new Scanner(System.in);
    }

    public void displayWelcome() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║                UNO GAME              ║");
        System.out.println("║              Version 1.0             ║");
        System.out.println("║                                      ║");
        System.out.println("║       Welcome to the UNO Game!       ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();
    }

    public int showMainMenu() {
        System.out.println("\n=== MAIN MENU ===");
        System.out.println("1. Start New Game");
        System.out.println("2. View Game Rules");
        System.out.println("3. Exit Game");
        System.out.print("Choose an option (1-3): ");

        return getValidatedInput(MIN_MENU_OPTION, MAX_MENU_OPTION, 0);
    }

    public int selectDifficulty() {
        System.out.println("\n=== DIFFICULTY LEVEL ===");
        System.out.println("1. Easy   - Bots play randomly");
        System.out.println("2. Medium - Bots prefer action cards");
        System.out.println("3. Hard   - Bots play strategically");
        System.out.print("Choose difficulty level (1-3): ");

        int choice = getValidatedInput(MIN_DIFFICULTY, MAX_DIFFICULTY, DEFAULT_DIFFICULTY);
        if (choice == DEFAULT_DIFFICULTY && choice != getLastInputAttempt()) {
            System.out.println("Invalid input! Default: Medium difficulty selected");
        }
        return choice;
    }

    public int getNumberOfHumanPlayers() {
        System.out.println("\n=== PLAYER COUNT ===");
        System.out.println("There are always 4 players in total.");
        System.out.println("How many human players? (0-4)");
        System.out.print("Number: ");

        int humans = getValidatedInput(MIN_PLAYERS, MAX_PLAYERS, DEFAULT_PLAYERS);
        if (humans == DEFAULT_PLAYERS && humans != getLastInputAttempt()) {
            System.out.println("Invalid input! Default: 1 human player");
        }
        return humans;
    }

    public String[] getPlayerNames(int numberOfPlayers) {
        if (numberOfPlayers <= 0) {
            return new String[0];
        }

        String[] names = new String[numberOfPlayers];
        clearInputBuffer();

        System.out.println("\n=== PLAYER NAMES ===");
        for (int i = 0; i < numberOfPlayers; i++) {
            names[i] = getValidPlayerName(i + 1);
        }

        return names;
    }

    private String getValidPlayerName(int playerNumber) {
        String name;
        do {
            System.out.print("Name for player " + playerNumber + ": ");
            name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Name cannot be empty! Please try again.");
            } else if (name.length() > 20) {
                System.out.println("Name too long! Maximum 20 characters allowed.");
                name = "";
            }
        } while (name.isEmpty());

        return name;
    }

    public boolean askForSpecialRules() {
        System.out.println("\n=== SPECIAL RULES ===");
        System.out.println("Would you like to enable special rules?");
        System.out.println("Available: Stacking, Doubling, Jump-In");
        System.out.println("(These features are still in development)");
        System.out.print("Enable special rules? (y/n): ");

        return getYesNoInput();
    }

    public int showGameMenu() {
        System.out.println("\n=== UNO MENU ===");
        System.out.println("1. Call UNO");
        System.out.println("2. Challenge +4");
        System.out.println("3. Show Scores");
        System.out.println("4. View Rules");
        System.out.println("5. Return to Game");
        System.out.println("6. Quit Game");
        System.out.print("Choose an option (1-6): ");

        return getValidatedInput(MIN_GAME_MENU_OPTION, MAX_GAME_MENU_OPTION, 5);
    }

    public void displayRules() {
        System.out.println("\n" + SHORT_SEPARATOR);
        System.out.println("                UNO GAME RULES");
        System.out.println(SHORT_SEPARATOR);

        try {
            if (loadRulesFromFile()) {
                System.out.println("\n💡 Tip: Rules loaded from " + RULES_FILE + "!");
            } else {
                displayBasicRules();
                System.out.println("\n💡 Tip: Create a file named '" + RULES_FILE +
                        "' for detailed game rules!");
            }
        } catch (IOException e) {
            System.out.println("⚠️ Error reading rules file: " + e.getMessage());
            displayBasicRules();
        }

        System.out.println("\n" + SHORT_SEPARATOR);
        waitForUserInput("Press Enter to continue...");
    }

    private boolean loadRulesFromFile() throws IOException {
        File rulesFile = new File(RULES_FILE);
        if (!rulesFile.exists()) {
            return false;
        }

        try (Scanner fileScanner = new Scanner(rulesFile)) {
            while (fileScanner.hasNextLine()) {
                System.out.println(fileScanner.nextLine());
            }
        }
        return true;
    }

    private void displayBasicRules() {
        System.out.println("🎯 OBJECTIVE:");
        System.out.println("   Be the first to reach 500 points!");
        System.out.println();
        System.out.println("🃏 BASIC RULES:");
        System.out.println("   • Match cards by color or number");
        System.out.println("   • Call 'UNO' when you have one card left!");
        System.out.println("   • Draw a card if you can't play");
        System.out.println();
        System.out.println("🎴 ACTION CARDS:");
        System.out.println("   • +2: Next player draws 2 cards");
        System.out.println("   • ⏭️ Skip: Next player loses a turn");
        System.out.println("   • 🔄 Reverse: Change play direction");
        System.out.println("   • 🎨 Wild: Choose a new color");
        System.out.println("   • +4: Next player draws 4 (only play when you have no match!)");
        System.out.println();
        System.out.println("⚖️ PENALTIES:");
        System.out.println("   • Forgot UNO: Draw 2 penalty cards");
        System.out.println("   • Illegal play: Draw 1 penalty card");
        System.out.println("   • 3 penalties = disqualification");
    }

    public void displayGameState(List<Player> players, Player currentPlayer, Card topCard, int direction) {
        System.out.println("\n" + SEPARATOR);
        System.out.println("🎮 CURRENT GAME STATE");
        System.out.println(SEPARATOR);

        displayCurrentCard(topCard);
        displayDirection(direction);
        displayCurrentPlayer(currentPlayer);
        displayPlayerOverview(players, currentPlayer);

        System.out.println(SEPARATOR);
    }

    private void displayCurrentCard(Card topCard) {
        System.out.println("🃏 Top card: " + topCard);
    }

    private void displayDirection(int direction) {
        String directionArrow = (direction == 1) ? "➡️" : "⬅️";
        System.out.println("🔄 Play direction: " + directionArrow);
    }

    private void displayCurrentPlayer(Player currentPlayer) {
        System.out.println("👤 Current turn: " + currentPlayer.getName());
    }

    private void displayPlayerOverview(List<Player> players, Player currentPlayer) {
        System.out.println("\n📊 PLAYER OVERVIEW:");
        for (Player player : players) {
            displayPlayerInfo(player, player == currentPlayer);
        }
    }

    private void displayPlayerInfo(Player player, boolean isCurrent) {
        String indicator = isCurrent ? "👉 " : "   ";
        String botIndicator = (player instanceof BotPlayer) ? "🤖 " : "👤 ";
        System.out.printf("%s%s%s: %d cards",
                indicator, botIndicator, player.getName(), player.getHandSize());

        if (player.getHandSize() == 1) {
            System.out.print(" 🚨 UNO!");
        }
        System.out.println();
    }

    public boolean askForChallenge(Player challengingPlayer, Player playedByPlayer) {
        if (challengingPlayer instanceof BotPlayer) {
            boolean challenge = Math.random() < BOT_CHALLENGE_PROBABILITY;
            if (challenge) {
                System.out.println("🤖 " + challengingPlayer.getName() + " is challenging " +
                        playedByPlayer.getName() + "!");
                waitForUserInput("Press Enter to continue...");
            }
            return challenge;
        }

        System.out.println("\n⚠️ " + playedByPlayer.getName() + " played a +4 Wild Draw card!");
        System.out.println(challengingPlayer.getName() + ", do you want to challenge?");
        System.out.println("(Only if you think they had a playable card)");
        System.out.print("Challenge? (y/n): ");

        return getYesNoInput();
    }

    public boolean confirmQuit() {
        System.out.print("\n⚠️ Are you sure you want to quit the game? (y/n): ");
        return getYesNoInput();
    }

    public void displayGameResults(Player winner, List<Player> players) {
        System.out.println("\n" + SEPARATOR);
        System.out.println("🎉 GAME OVER - FINAL RESULTS 🎉");
        System.out.println(SEPARATOR);

        displayWinner(winner);
        displayFinalScores(players);

        System.out.println(SEPARATOR);
        System.out.println("Thanks for playing! 🎮");
        waitForUserInput("Press Enter to exit...");
    }

    private void displayWinner(Player winner) {
        System.out.println("🏆 WINNER: " + winner.getName() +
                " with " + winner.getTotalScore() + " points!");
    }

    private void displayFinalScores(List<Player> players) {
        System.out.println("\n📊 FINAL SCORES:");
        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort((p1, p2) -> Integer.compare(p2.getTotalScore(), p1.getTotalScore()));

        for (int i = 0; i < sortedPlayers.size(); i++) {
            Player player = sortedPlayers.get(i);
            String medal = getMedal(i);
            String botIndicator = (player instanceof BotPlayer) ? "🤖" : "👤";

            System.out.printf("%s %s %s: %d points\n",
                    medal, botIndicator, player.getName(), player.getTotalScore());
        }
    }

    private String getMedal(int position) {
        return switch (position) {
            case 0 -> "🥇";
            case 1 -> "🥈";
            case 2 -> "🥉";
            default -> "   ";
        };
    }

    private int getValidatedInput(int min, int max, int defaultValue) {
        try {
            int input = scanner.nextInt();
            return (input >= min && input <= max) ? input : defaultValue;
        } catch (InputMismatchException e) {
            clearInputBuffer();
            return defaultValue;
        }
    }

    private boolean getYesNoInput() {
        String input = scanner.next().toLowerCase().trim();
        return input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes")
                || input.equalsIgnoreCase("j") || input.equalsIgnoreCase("ja");
    }

    private void clearInputBuffer() {
        scanner.nextLine();
    }

    private void waitForUserInput(String message) {
        System.out.print(message);
        clearInputBuffer();
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }

    private int getLastInputAttempt() {
        return -1; // Placeholder
    }

    public void displayError(String message) {
        System.out.println("❌ Error: " + message);
    }

    public void displaySuccess(String message) {
        System.out.println("✅ " + message);
    }

    public void displayInfo(String message) {
        System.out.println("ℹ️ " + message);
    }

    @Override
    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }
}
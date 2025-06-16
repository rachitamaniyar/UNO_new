import java.util.*;
import java.io.*;

/**
 * Handles all menu interactions and user interface
 * Manages game menus, rule display, and player input
 */
public class Menu {
    private Scanner scanner;
    private static final String RULES_FILE = "uno_rules.txt";

    public Menu() {
        scanner = new Scanner(System.in);
    }

    /**
     * Displays the main welcome screen and ASCII art
     */
    public void displayWelcome() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║              UNO SPIEL               ║");
        System.out.println("║            Version 1.0               ║");
        System.out.println("║                                      ║");
        System.out.println("║    🎮 Willkommen beim UNO Spiel! 🎮   ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();
    }

    /**
     * Shows the main menu and gets user's choice
     * @return User's menu choice
     */
    public int showMainMenu() {
        System.out.println("\n=== HAUPTMENÜ ===");
        System.out.println("1. Neues Spiel starten");
        System.out.println("2. Spielregeln anzeigen");
        System.out.println("3. Spiel beenden");
        System.out.print("Wähle eine Option (1-3): ");

        try {
            return scanner.nextInt();
        } catch (InputMismatchException e) {
            scanner.nextLine(); // Clear invalid input
            return 0; // Invalid choice
        }
    }

    /**
     * Displays game setup menu for difficulty selection
     * @return Selected difficulty level (1-3)
     */
    public int selectDifficulty() {
        System.out.println("\n=== SCHWIERIGKEITSGRAD ===");
        System.out.println("1. Leicht   - Bots spielen zufällig");
        System.out.println("2. Mittel   - Bots bevorzugen Aktionskarten");
        System.out.println("3. Schwer   - Bots spielen strategisch");
        System.out.print("Wähle Schwierigkeitsgrad (1-3): ");

        try {
            int choice = scanner.nextInt();
            return (choice >= 1 && choice <= 3) ? choice : 2; // Default to medium
        } catch (InputMismatchException e) {
            scanner.nextLine();
            return 2; // Default to medium
        }
    }

    /**
     * Gets the number of human players
     * @return Number of human players (0-4)
     */
    public int getNumberOfHumanPlayers() {
        System.out.println("\n=== SPIELERANZAHL ===");
        System.out.println("Es spielen immer genau 4 Spieler.");
        System.out.println("Wie viele menschliche Spieler? (0-4)");
        System.out.print("Anzahl: ");

        try {
            int humans = scanner.nextInt();
            if (humans >= 0 && humans <= 4) {
                return humans;
            } else {
                System.out.println("Ungültige Anzahl! Standard: 1 menschlicher Spieler");
                return 1;
            }
        } catch (InputMismatchException e) {
            scanner.nextLine();
            System.out.println("Ungültige Eingabe! Standard: 1 menschlicher Spieler");
            return 1;
        }
    }

    /**
     * Gets player names from user input
     * @param numberOfPlayers Number of human players
     * @return Array of player names
     */
    public String[] getPlayerNames(int numberOfPlayers) {
        String[] names = new String[numberOfPlayers];
        scanner.nextLine(); // Clear the newline from previous input

        System.out.println("\n=== SPIELERNAMEN ===");
        for (int i = 0; i < numberOfPlayers; i++) {
            String name;
            do {
                System.out.print("Name für Spieler " + (i + 1) + ": ");
                name = scanner.nextLine().trim();
                if (name.isEmpty()) {
                    System.out.println("Name darf nicht leer sein! Bitte erneut eingeben.");
                }
            } while (name.isEmpty()); // Loop until valid name is entered

            names[i] = name;
        }

        return names;
    }

    /**
     * Asks if player wants to add special rules
     * @return true if special rules should be enabled
     */
    public boolean askForSpecialRules() {
        System.out.println("\n=== SPEZIALREGELN ===");
        System.out.println("Möchtest du Spezialregeln aktivieren?");
        System.out.println("Verfügbare Regeln: Doppeln, Kumulieren, Jump-In");
        System.out.println("(Diese Features sind noch in Entwicklung)");
        System.out.print("Spezialregeln aktivieren? (j/n): ");

        String input = scanner.next().toLowerCase();
        return input.equals("j") || input.equals("ja") || input.equals("y") || input.equals("yes");
    }

    /**
     * Shows the in-game menu during play
     * @return Selected menu option
     */
    public int showGameMenu() {
        System.out.println("\n=== SPIEL-MENÜ ===");
        System.out.println("1. UNO rufen");
        System.out.println("2. Spieler herausfordern (bei +4 Karte)");
        System.out.println("3. Punkte anzeigen");
        System.out.println("4. Spielregeln anzeigen");
        System.out.println("5. Zurück zum Spiel");
        System.out.println("6. Spiel beenden");
        System.out.print("Wähle eine Option: ");

        try {
            return scanner.nextInt();
        } catch (InputMismatchException e) {
            scanner.nextLine();
            return 5; // Return to game
        }
    }

    /**
     * Displays the game rules from external file
     * If the file doesn't exist, shows basic rules
     */
    public void displayRules() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("                UNO SPIELREGELN");
        System.out.println("=".repeat(50));

        try {
            // Try to read rules from external file
            File rulesFile = new File(RULES_FILE);
            if (rulesFile.exists()) {
                Scanner fileScanner = new Scanner(rulesFile);
                while (fileScanner.hasNextLine()) {
                    System.out.println(fileScanner.nextLine());
                }
                fileScanner.close();
            } else {
                // Display basic rules if file doesn't exist
                displayBasicRules();
                System.out.println("\n💡 Tipp: Erstelle eine Datei '" + RULES_FILE +
                        "' für ausführliche Spielregeln!");
            }
        } catch (IOException e) {
            System.out.println("Fehler beim Lesen der Regelndatei: " + e.getMessage());
            displayBasicRules();
        }

        System.out.println("\n" + "=".repeat(50));
        System.out.print("Drücke Enter zum Fortfahren...");
        scanner.nextLine();
        if (scanner.hasNextLine()) {
            scanner.nextLine(); // Wait for user input
        }
    }

    /**
     * Displays basic UNO rules when external file is not available
     */
    private void displayBasicRules() {
        System.out.println("🎯 SPIELZIEL:");
        System.out.println("   Erreiche als erster 500 Punkte!");
        System.out.println();
        System.out.println("🃏 GRUNDREGELN:");
        System.out.println("   • Lege Karten passender Farbe oder Zahl");
        System.out.println("   • Rufe 'UNO' bei der vorletzten Karte!");
        System.out.println("   • Ziehe eine Karte wenn du nicht legen kannst");
        System.out.println();
        System.out.println("🎴 AKTIONSKARTEN:");
        System.out.println("   • +2: Nächster Spieler zieht 2 Karten");
        System.out.println("   • ⏭️ Aussetzen: Nächster Spieler wird übersprungen");
        System.out.println("   • 🔄 Richtungswechsel: Spielrichtung umkehren");
        System.out.println("   • 🎨 Farbwahl: Wähle eine neue Farbe");
        System.out.println("   • +4: Nächster Spieler zieht 4, nur bei Notlage!");
        System.out.println();
        System.out.println("⚖️ STRAFEN:");
        System.out.println("   • UNO vergessen: 2 Strafkarten");
        System.out.println("   • Falsche Karte: 1 Strafkarte");
        System.out.println("   • 3 Strafen = Disqualifikation");
    }

    /**
     * Displays current game state
     * @param players All players
     * @param currentPlayer Current player
     * @param topCard Current top card
     * @param direction Game direction
     */
    public void displayGameState(List<Player> players, Player currentPlayer, Card topCard, int direction) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎮 AKTUELLER SPIELSTAND");
        System.out.println("=".repeat(60));

        // Show current card
        System.out.println("🃏 Aktuelle Karte: " + topCard);

        // Show direction
        String directionArrow = (direction == 1) ? "➡️" : "⬅️";
        System.out.println("🔄 Spielrichtung: " + directionArrow);

        // Show current player
        System.out.println("👤 Am Zug: " + currentPlayer.getName());

        // Show all players and their card counts
        System.out.println("\n📊 SPIELER-ÜBERSICHT:");
        for (Player player : players) {
            String indicator = (player == currentPlayer) ? "👉 " : "   ";
            String botIndicator = (player instanceof BotPlayer) ? "🤖 " : "👤 ";
            System.out.printf("%s%s%s: %d Karten",
                    indicator, botIndicator, player.getName(), player.getHandSize());

            if (player.getHandSize() == 1) {
                System.out.print(" 🚨 UNO!");
            }
            System.out.println();
        }
        System.out.println("=".repeat(60));
    }

    /**
     * Asks player if they want to challenge a Wild Draw Four
     * @param challengingPlayer The player who can challenge
     * @param playedByPlayer The player who played Wild Draw Four
     * @return true if player wants to challenge
     */
    public boolean askForChallenge(Player challengingPlayer, Player playedByPlayer) {
        if (challengingPlayer instanceof BotPlayer) {
            // Bots have a 30% chance to challenge
            boolean challenge = Math.random() < 0.3;
            if (challenge) {
                System.out.println(challengingPlayer.getName() + " zweifelt " +
                        playedByPlayer.getName() + " an!");
            }
            return challenge;
        }

        System.out.println("\n⚠️ " + playedByPlayer.getName() + " hat eine +4 Karte gespielt!");
        System.out.println(challengingPlayer.getName() + ", möchtest du ihn herausfordern?");
        System.out.println("(Nur wenn du glaubst, dass er eine passende Karte hatte)");
        System.out.print("Herausfordern? (j/n): ");

        String input = scanner.next().toLowerCase();
        return input.equals("j") || input.equals("ja");
    }

    /**
     * Confirms if player wants to quit the game
     * @return true if player wants to quit
     */
    public boolean confirmQuit() {
        System.out.print("\nMöchtest du das Spiel wirklich beenden? (j/n): ");
        String input = scanner.next().toLowerCase();
        return input.equals("j") || input.equals("ja");
    }

    /**
     * Shows final game results
     * @param winner The winning player
     * @param players All players
     */
    public void displayGameResults(Player winner, List<Player> players) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎉 SPIELENDE - ENDERGEBNIS 🎉");
        System.out.println("=".repeat(60));

        System.out.println("🏆 GEWINNER: " + winner.getName() +
                " mit " + winner.getTotalScore() + " Punkten!");

        System.out.println("\n📊 ENDSTAND:");
        // Sort players by score
        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort((p1, p2) -> Integer.compare(p2.getTotalScore(), p1.getTotalScore()));

        for (int i = 0; i < sortedPlayers.size(); i++) {
            Player player = sortedPlayers.get(i);
            String medal = "";
            switch (i) {
                case 0: medal = "🥇"; break;
                case 1: medal = "🥈"; break;
                case 2: medal = "🥉"; break;
                default: medal = "   "; break;
            }

            String botIndicator = (player instanceof BotPlayer) ? "🤖" : "👤";
            System.out.printf("%s %s %s: %d Punkte\n",
                    medal, botIndicator, player.getName(), player.getTotalScore());
        }

        System.out.println("=".repeat(60));
        System.out.println("Danke fürs Spielen! 🎮");
    }
}

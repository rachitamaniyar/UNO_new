import java.util.*;

/**
 * Handles game ending, final scoring, and cleanup
 */
public class End {
    private Menu menu;

    public End() {
        menu = new Menu();
    }

    /**
     * Handles the complete game ending process
     * @param winner The player who won the game
     * @param allPlayers All players in the game
     */
    public void handleGameEnd(Player winner, List<Player> allPlayers) {
        displayFinalResults(winner, allPlayers);
        displayStatistics(allPlayers);
        offerPlayAgain();
    }

    /**
     * Displays final game results and rankings
     * @param winner The winning player
     * @param players All players
     */
    private void displayFinalResults(Player winner, List<Player> players) {
        System.out.println("\n" + "★".repeat(60));
        System.out.println("                    SPIEL BEENDET");
        System.out.println("★".repeat(60));

        System.out.println("\n🏆 CHAMPION: " + winner.getName());
        System.out.println("   Endpunktzahl: " + winner.getTotalScore() + " Punkte");

        // Sort players by final score
        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort((p1, p2) -> Integer.compare(p2.getTotalScore(), p1.getTotalScore()));

        System.out.println("\n📊 FINAL RANKINGS:");
        System.out.println("-".repeat(40));

        for (int i = 0; i < sortedPlayers.size(); i++) {
            Player player = sortedPlayers.get(i);
            String position = getPositionString(i + 1);
            String playerType = (player instanceof BotPlayer) ? "🤖" : "👤";

            System.out.printf("%s %s %s: %d Punkte\n",
                    position, playerType, player.getName(), player.getTotalScore());
        }
    }

    /**
     * Gets position string with appropriate emoji
     * @param position Player's final position
     * @return Formatted position string
     */
    private String getPositionString(int position) {
        switch (position) {
            case 1: return "🥇 1st";
            case 2: return "🥈 2nd";
            case 3: return "🥉 3rd";
            case 4: return "🏅 4th";
            default: return "   " + position + ".";
        }
    }

    /**
     * Displays game statistics
     * @param players All players
     */
    private void displayStatistics(List<Player> players) {
        System.out.println("\n📈 SPIEL-STATISTIKEN:");
        System.out.println("-".repeat(40));

        // Calculate total rounds played (approximate)
        int totalScore = 0;
        for (Player player : players) {
            totalScore += player.getTotalScore();
        }
        int estimatedRounds = Math.max(1, totalScore / 200); // Rough estimate

        System.out.println("Geschätzte Rundenzahl: " + estimatedRounds);

        // Show penalty statistics
        System.out.println("\n⚠️ STRAF-STATISTIKEN:");
        for (Player player : players) {
            String playerType = (player instanceof BotPlayer) ? "🤖" : "👤";
            System.out.printf("   %s %s: %d Strafen\n",
                    playerType, player.getName(), player.getPenaltyCount());
        }

        // Find highest and lowest scores
        Player highestScorer = players.stream()
                .max((p1, p2) -> Integer.compare(p1.getTotalScore(), p2.getTotalScore()))
                .orElse(null);
        Player lowestScorer = players.stream()
                .min((p1, p2) -> Integer.compare(p1.getTotalScore(), p2.getTotalScore()))
                .orElse(null);

        if (highestScorer != null && lowestScorer != null) {
            System.out.println("\n🎯 SCORE-BEREICH:");
            System.out.println("   Höchste Punktzahl: " + highestScorer.getTotalScore() +
                    " (" + highestScorer.getName() + ")");
            System.out.println("   Niedrigste Punktzahl: " + lowestScorer.getTotalScore() +
                    " (" + lowestScorer.getName() + ")");
        }
    }

    /**
     * Offers player option to play again
     */
    private void offerPlayAgain() {
        System.out.println("\n" + "=".repeat(50));
        System.out.print("Möchtest du noch eine Runde spielen? (j/n): ");

        Scanner scanner = new Scanner(System.in);
        String input = scanner.next().toLowerCase();

        if (input.equals("j") || input.equals("ja") || input.equals("y") || input.equals("yes")) {
            System.out.println("Neues Spiel wird gestartet...\n");
            // This would trigger a new game in the main class
            restartGame();
        } else {
            displayFinalGoodbye();
        }
    }

    /**
     * Handles restarting the game
     */
    private void restartGame() {
        // In a complete implementation, this would communicate with Main
        // to restart the entire game process
        System.out.println("🔄 Spiel wird neu gestartet...");

        // For now, we'll just indicate that a restart was requested
        System.out.println("(Neustart-Funktionalität würde hier implementiert werden)");
        displayFinalGoodbye();
    }

    /**
     * Displays final goodbye message
     */
    private void displayFinalGoodbye() {
        System.out.println("\n" + "★".repeat(60));
        System.out.println("             Danke fürs UNO spielen!");
        System.out.println("                Bis zum nächsten Mal!");
        System.out.println("★".repeat(60));

        // Display credits
        System.out.println("\n💻 Entwickelt als Java-Lernprojekt");
        System.out.println("🎮 Basierend auf den offiziellen UNO-Regeln");
        System.out.println("📚 Version 1.0 - Konsolen-Edition");

        System.out.println("\n👋 Auf Wiedersehen!");
    }

    /**
     * Handles premature game ending (quit)
     * @param players Current players
     */
    public void handleEarlyExit(List<Player> players) {
        System.out.println("\n⏹️ Spiel wurde vorzeitig beendet.");

        if (!players.isEmpty()) {
            System.out.println("\n📊 Aktueller Stand beim Beenden:");
            displayCurrentScores(players);
        }

        displayFinalGoodbye();
    }

    /**
     * Displays current scores when game ends early
     * @param players All players
     */
    private void displayCurrentScores(List<Player> players) {
        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort((p1, p2) -> Integer.compare(p2.getTotalScore(), p1.getTotalScore()));

        for (Player player : sortedPlayers) {
            String playerType = (player instanceof BotPlayer) ? "🤖" : "👤";
            System.out.printf("   %s %s: %d Punkte\n",
                    playerType, player.getName(), player.getTotalScore());
        }
    }

    /**
     * Handles round ending (not game ending)
     * @param roundWinner Winner of the current round
     * @param players All players
     * @return true if game should continue, false if game should end
     */
    public boolean handleRoundEnd(Player roundWinner, List<Player> players) {
        System.out.println("\n🎊 Runde gewonnen von: " + roundWinner.getName());

        // Check if anyone has won the game
        Player gameWinner = null;
        for (Player player : players) {
            if (player.getTotalScore() >= 500) {
                gameWinner = player;
                break;
            }
        }

        if (gameWinner != null) {
            handleGameEnd(gameWinner, players);
            return false; // End the game
        }

        // Show current scores
        System.out.println("\n📊 Zwischenstand:");
        displayCurrentScores(players);

        System.out.println("\nNächste Runde beginnt...");
        return true; // Continue the game
    }
}

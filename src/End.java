import java.util.*;
import java.util.Scanner; // NEW - ADDED

/**
 * Handles game ending, final scoring, and cleanup
 */
public class End {

    private Menu menu; // the menu object
    private Scanner scanner; // NEW - ADDED

    public End(Scanner sharedScanner) {
        this.scanner = sharedScanner;
        this.menu = new Menu(this.scanner);
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
        System.out.println("                    GAME OVER");
        System.out.println("★".repeat(60));

        System.out.println("\n🏆 CHAMPION: " + winner.getName());
        System.out.println("   Final score: " + winner.getTotalScore() + " points");

        // Sort players by final score
        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort((p1, p2) -> Integer.compare(p2.getTotalScore(), p1.getTotalScore()));

        System.out.println("\n📊 FINAL RANKINGS:");
        System.out.println("-".repeat(40));

        for (int i = 0; i < sortedPlayers.size(); i++) {
            Player player = sortedPlayers.get(i);
            String position = getPositionString(i + 1);
            String playerType = (player instanceof BotPlayer) ? "🤖" : "👤";

            System.out.printf("%s %s %s: %d Points\n",
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
        System.out.println("\n📈 GAME STATISTICS:");
        System.out.println("-".repeat(40));

        // Calculate total rounds played (approximate)
        int totalScore = 0;
        for (Player player : players) {
            totalScore += player.getTotalScore();
        }
        int estimatedRounds = Math.max(1, totalScore / 200); // Rough estimate

        System.out.println("Estimated number of rounds: " + estimatedRounds);

        // Show penalty statistics
        System.out.println("\n⚠️ PENALTY STATISTICS:");
        for (Player player : players) {
            String playerType = (player instanceof BotPlayer) ? "🤖" : "👤";
            System.out.printf("   %s %s: %d Penalties:\n",
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
            System.out.println("\n🎯 SCORE RANGE:");
            System.out.println("   Highest score: " + highestScorer.getTotalScore() +
                    " (" + highestScorer.getName() + ")");
            System.out.println("   Lowest score: " + lowestScorer.getTotalScore() +
                    " (" + lowestScorer.getName() + ")");
        }
    }

    /**
     * Offers player option to play again
     */
    private void offerPlayAgain() {
        System.out.println("\n" + "=".repeat(50));
        System.out.print("Do you want to play another round? (y/n): ");

        Scanner scanner = new Scanner(System.in);
        String input = scanner.next().toLowerCase();

        if (input.equalsIgnoreCase("j") || input.equalsIgnoreCase("ja") ||
                input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes")) {
            System.out.println("Starting a new game...\n");
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
        System.out.println("🔄 Restarting the game...");

        // For now, we'll just indicate that a restart was requested
        System.out.println("(Restart functionality would be implemented here)");
        displayFinalGoodbye();
    }

    /**
     * Displays final goodbye message
     */
    private void displayFinalGoodbye() {
        System.out.println("\n" + "★".repeat(60));
        System.out.println("             Thanks for playing UNO!");
        System.out.println("                See you next time");
        System.out.println("★".repeat(60));

        // Display credits
        System.out.println("\n💻 Developed as a Java learning project");
        System.out.println("🎮 Based on the official UNO rules");
        System.out.println("📚 Version 1.0 - Console Edition");

        System.out.println("\n👋 Goodbye!");
    }

    /**
     * Handles premature game ending (quit)
     * @param players Current players
     */
    public void handleEarlyExit(List<Player> players) {
        System.out.println("\n⏹️ Game was ended early.");

        if (!players.isEmpty()) {
            System.out.println("\n📊 Current standings at exit:");
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
            System.out.printf("   %s %s: %d points\n",
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
        System.out.println("\n🎊 Round won by: " + roundWinner.getName());

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
        System.out.println("\n📊 Current scores:");
        displayCurrentScores(players);

        System.out.println("\nNext round starts...");
        return true; // Continue the game
    }
}

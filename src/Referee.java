import java.util.*;

/**
 * Enforces game rules, handles penalties, and manages scoring
 * Acts as the rule enforcer and game state validator
 */
public class Referee {
    private List<Player> players;
    private Deck deck;
    private Scanner scanner;

    public Referee(List<Player> players, Deck deck) {
        this.players = players;
        this.deck = deck;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Validates if a card play is legal
     * @param card The card being played
     * @param topCard The current top card
     * @param player The player making the move
     * @return true if the move is valid
     */
    public boolean validateCardPlay(Card card, Card topCard, Player player) {
        // Basic rule check - can the card be played on the top card?
        if (!card.canPlayOn(topCard)) {
            System.out.println("Ungültige Karte! " + card + " kann nicht auf " + topCard + " gelegt werden.");
            penalizeFalseCardPlay(player);
            return false;
        }

        // Special validation for Wild Draw Four
        if (card.getType() == CardType.WILD_DRAW_FOUR) {
            return validateWildDrawFour(card, topCard, player);
        }

        return true;
    }

    /**
     * Validates Wild Draw Four card play - can only be played if no matching color cards
     * @param card The Wild Draw Four card
     * @param topCard Current top card
     * @param player Player attempting to play the card
     * @return true if valid play
     */
    private boolean validateWildDrawFour(Card card, Card topCard, Player player) {
        // Check if player has any cards matching the current color
        boolean hasMatchingColor = false;

        for (Card handCard : player.getHand()) {
            // Player can play Wild Draw Four if they don't have matching color
            // But they CAN have matching numbers or other action cards
            if (handCard.getColor() == topCard.getColor() &&
                    handCard != card) { // Don't count the Wild Draw Four itself
                hasMatchingColor = true;
                break;
            }
        }

        if (hasMatchingColor) {
            // This is potentially a bluff - store this information
            System.out.println(player.name + " spielt eine Zieh-Vier-Karte...");
            return true; // Allow the play, but it might be challenged
        }

        return true; // Legal play
    }

    /**
     * Handles challenge of Wild Draw Four card
     * @param challenger The player challenging
     * @param challengedPlayer The player who played Wild Draw Four
     * @param topCard The card that was on top before Wild Draw Four
     */
    public void handleWildDrawFourChallenge(Player challenger, Player challengedPlayer, Card topCard) {
        System.out.println(challenger.getName() + " zweifelt " + challengedPlayer.getName() + " an!");

        // Check if the challenged player was bluffing
        boolean wasBluffing = false;
        for (Card card : challengedPlayer.getHand()) {
            if (card.getColor() == topCard.getColor()) {
                wasBluffing = true;
                break;
            }
        }

        if (wasBluffing) {
            System.out.println(challengedPlayer.getName() + " hat geblufft!");
            // Challenged player draws 4 cards instead of challenger
            for (int i = 0; i < 4; i++) {
                Card drawnCard = deck.drawCard();
                if (drawnCard != null) {
                    challengedPlayer.addCard(drawnCard);
                }
            }
            challengedPlayer.addPenalty();
        } else {
            System.out.println(challengedPlayer.getName() + " hat nicht geblufft!");
            // Challenger draws 6 cards (4 + 2 penalty)
            for (int i = 0; i < 6; i++) {
                Card drawnCard = deck.drawCard();
                if (drawnCard != null) {
                    challenger.addCard(drawnCard);
                }
            }
            challenger.addPenalty();
        }
    }

    /**
     * Checks if player forgot to call UNO
     * @param player The player to check
     * @return true if player should be penalized for forgetting UNO
     */
    public boolean checkUnoViolation(Player player) {
        if (player.getHandSize() == 1 && !player.hasSaidUno()) {
            System.out.println(player.getName() + " hat vergessen UNO zu rufen!");

            // Give other players a chance to catch this
            System.out.println("Hat jemand bemerkt, dass " + player.getName() + " UNO vergessen hat?");
            System.out.println("Drücke 'j' wenn ja, beliebige andere Taste wenn nein:");

            // In a real game, other players would notice
            // For simulation, we'll have a random chance
            if (Math.random() < 0.7) { // 70% chance someone notices
                penalizeUnoViolation(player);
                return true;
            }
        }
        return false;
    }

    /**
     * Applies penalty for UNO violation
     */
    private void penalizeUnoViolation(Player player) {
        System.out.println(player.getName() + " muss 2 Strafkarten ziehen!");
        for (int i = 0; i < 2; i++) {
            Card card = deck.drawCard();
            if (card != null) {
                player.addCard(card);
            }
        }
        player.addPenalty();
    }

    /**
     * Applies penalty for playing wrong card
     */
    private void penalizeFalseCardPlay(Player player) {
        System.out.println(player.getName() + " muss 1 Strafkarte ziehen!");
        Card card = deck.drawCard();
        if (card != null) {
            player.addCard(card);
        }
        player.addPenalty();
    }

    /**
     * Applies penalty for playing out of turn
     */
    public void penalizeOutOfTurn(Player player) {
        System.out.println(player.getName() + " war nicht dran! 1 Strafkarte!");
        Card card = deck.drawCard();
        if (card != null) {
            player.addCard(card);
        }
        player.addPenalty();
    }

    /**
     * Calculates and awards points at the end of a round
     * @param winner The player who won the round
     */
    public void calculateRoundScore(Player winner) {
        int totalPoints = 0;

        System.out.println("\n=== RUNDENERGEBNIS ===");
        System.out.println(winner.getName() + " hat die Runde gewonnen!");

        // Calculate points from all other players' hands
        for (Player player : players) {
            if (player != winner) {
                int handPoints = player.calculateHandPoints();
                totalPoints += handPoints;
                System.out.println(player.getName() + " hat " + handPoints + " Punkte auf der Hand.");
            }
        }

        winner.addScore(totalPoints);
        System.out.println(winner.getName() + " erhält " + totalPoints + " Punkte!");
        System.out.println("Gesamtpunkte von " + winner.getName() + ": " + winner.getTotalScore());
    }

    /**
     * Checks if any player has reached the winning score
     * @return The winning player, or null if no winner yet
     */
    public Player checkGameWinner() {
        for (Player player : players) {
            if (player.getTotalScore() >= 500) {
                return player;
            }
        }
        return null;
    }

    /**
     * Checks for disqualified players
     * @return List of players to be disqualified
     */
    public List<Player> checkDisqualifications() {
        List<Player> disqualified = new ArrayList<>();
        for (Player player : players) {
            if (player.shouldBeDisqualified()) {
                disqualified.add(player);
                System.out.println(player.getName() + " wird wegen zu vieler Strafen disqualifiziert!");
            }
        }
        return disqualified;
    }

    /**
     * Displays current scores of all players
     */
    public void displayScores() {
        System.out.println("\n=== AKTUELLE PUNKTE ===");
        // Sort players by score for better display
        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort((p1, p2) -> Integer.compare(p2.getTotalScore(), p1.getTotalScore()));

        for (Player player : sortedPlayers) {
            System.out.printf("%s: %d Punkte\n", player.getName(), player.getTotalScore());
        }
    }

    /**
     * Handles the effects of special cards
     * @param card The special card that was played
     * @param currentPlayerIndex Index of current player
     * @param direction Current game direction
     * @return New direction after card effect
     */
    public int handleSpecialCardEffects(Card card, int currentPlayerIndex, int direction) {
        switch (card.getType()) {
            case DRAW_TWO:
                // Next player draws 2 cards and loses turn
                int nextPlayer = getNextPlayerIndex(currentPlayerIndex, direction);
                Player victim = players.get(nextPlayer);
                System.out.println(victim.getName() + " muss 2 Karten ziehen und aussetzten!");

                for (int i = 0; i < 2; i++) {
                    Card drawnCard = deck.drawCard();
                    if (drawnCard != null) {
                        victim.addCard(drawnCard);
                    }
                }
                break;

            case REVERSE:
                direction *= -1; // Reverse direction
                System.out.println("Spielrichtung wird umgekehrt!");
                break;

            case SKIP:
                // Next player loses their turn
                nextPlayer = getNextPlayerIndex(currentPlayerIndex, direction);
                System.out.println(players.get(nextPlayer).getName() + " muss aussetzen!");
                break;

            case WILD_DRAW_FOUR:
                // Next player draws 4 cards and loses turn
                nextPlayer = getNextPlayerIndex(currentPlayerIndex, direction);
                victim = players.get(nextPlayer);
                System.out.println(victim.getName() + " muss 4 Karten ziehen und aussetzen!");

                for (int i = 0; i < 4; i++) {
                    Card drawnCard = deck.drawCard();
                    if (drawnCard != null) {
                        victim.addCard(drawnCard);
                    }
                }
                break;
        }

        return direction;
    }

    /**
     * Helper method to get next player index
     */
    private int getNextPlayerIndex(int currentIndex, int direction) {
        int nextIndex = currentIndex + direction;
        if (nextIndex >= players.size()) {
            nextIndex = 0;
        } else if (nextIndex < 0) {
            nextIndex = players.size() - 1;
        }
        return nextIndex;
    }
}

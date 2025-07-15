import java.util.*;

/**
 * Represents a player in the UNO game
 * Contains player's hand, score, and basic player operations
 */
public class Player {
    protected String name;              // Player's name
    protected List<Card> hand;          // Player's cards (using List interface)
    protected int totalScore;           // Total score across all rounds
    protected int penaltyCount;         // Number of penalties received
    protected boolean saidUno;          // Whether player said UNO

    /**
     * Constructor for creating a new player
     * @param name The user should enter the player's name, other variables are automatically created.
     */
    public Player(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be null or empty");
        }
        this.name = name.trim(); //to remove any spaces
        this.hand = new ArrayList<>();  // ArrayList implements List interface
        this.totalScore = 0;
        this.penaltyCount = 0;
        this.saidUno = false;
    }

    /**
     * Adds a card to the player's hand
     * @param card The card to add
     */
    public void addCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Cannot add null card to hand");
        }
        hand.add(card);
        // Reset UNO flag when getting a new card
        if (hand.size() > 1) {
            saidUno = false;
        }
    }

    /**
     * Removes a card from the player's hand
     * @param index The index of the card to remove
     * @return The removed card, or null if index is invalid
     */
    public Card playCard(int index) {
        //Exception-Handling (instead of returning null)
        if (index < 0 || index >= hand.size()) {
            throw new IndexOutOfBoundsException("Invalid card index: " + index);
        }
        return hand.remove(index);
    }

    /**
     * Displays the player's hand with numbered options
     * Only shows the actual cards to the current player
     */
    public void displayHand() {
        System.out.println("\n" + name + "'s cards:");
        for (int i = 0; i < hand.size(); i++) {
            // Using printf for formatted output: %d = integer, %s = string
            System.out.printf("%d. %s\n", i + 1, hand.get(i));
        }
        System.out.println("0. Draw a card"); // Option to draw is for ONLY human players
    }

    /**
     * Handles the UNO call when player has one card left
     * This method simply updates the internal state.
     * The actual "call" output and penalty check happen in the game logic (Run/Referee).
     */
    public void callUno() {
        if (hand.size() == 1) {
            saidUno = true;
            System.out.println(name + " calls: UNO!");
        }
    }

    /**
     * Checks if player forgot to call UNO and applies penalty if needed
     * @return true if penalty was applied
     */
    //UNUSED
    public boolean checkUnoViolation() {
        return hand.size() == 1 && !saidUno;
    }

    /**
     * Calculates the total points in the player's hand
     * Used for scoring at the end of rounds
     * @return Total point value of all cards in hand
     */
    public int calculateHandPoints() {
        int total = 0;
        // Enhanced for loop (for-each loop) - iterates through all cards
        for (Card card : hand) {
            total += card.getPoints();
        }
        return total;
    }

    /**
     * Checks if player has a playable card
     * @param topCard The current top card
     * @return true if player has a playable card
     */
    //UNUSED
    public boolean hasPlayableCard(Card topCard) {
        if (topCard == null) return false;

        for (Card card : hand) {
            if (card.canPlayOn(topCard)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets all playable cards from hand
     * @param topCard The current top card
     * @return List of indices of playable cards
     */
    //UNUSED
    public List<Integer> getPlayableCardIndices(Card topCard) {
        List<Integer> playableIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).canPlayOn(topCard)) {
                playableIndices.add(i);
            }
        }
        return playableIndices;
    }

    /**
     * Adds penalty points and increments penalty counter
     */
    public void addPenalty() {
        penaltyCount++;
        System.out.println(name + "  receives a penalty! (Total: " + penaltyCount + ")");
    }

    /**
     * Checks if player should be disqualified (3 penalties)
     * @return true if player should be disqualified
     */
    public boolean shouldBeDisqualified() {
        return penaltyCount >= 3;
    }

    /**
     * Checks if player's hand is empty (won the round)
     * @return true if hand is empty
     */
    //UNUSED
    public boolean hasWon() {
        return hand.isEmpty();
    }

    public void addScore(int points) {
        if (points >= 0) {
            totalScore += points;
        }
    }
    /**
     * Clears the player's hand for a new round
     */
    public void clearHand() {
        hand.clear();
        saidUno = false;
    }
    // Getter and setter methods
    public String getName() { return name; }
    public List<Card> getHand() { return new ArrayList<>(hand); } // Return copy to prevent external modification
    public int getHandSize() { return hand.size(); }
    public int getTotalScore() { return totalScore; }

    public boolean hasSaidUno() { return saidUno; }

    public int getPenaltyCount() { return penaltyCount; }
    public void setSaidUno(boolean saidUno) { this.saidUno = saidUno; }

    public void resetPenalties() { penaltyCount = 0; }

    @Override
    public String toString() {
        return name + " (Cards: " + hand.size() + ", Points: " + totalScore + ")";
    }
}
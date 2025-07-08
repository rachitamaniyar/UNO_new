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
    protected static Scanner scanner = new Scanner(System.in); // Shared scanner to avoid resource conflicts

    /**
     * Constructor for creating a new player
     * @param name The player's name
     */
    public Player(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be null or empty");
        }
        this.name = name.trim();
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
            throw new IllegalArgumentException("Cannot add 'null' card to hand");
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
        if (index >= 0 && index < hand.size()) {
            Card playedCard = hand.remove(index);

            // Check if player should call UNO after playing
            if (hand.size() == 1 && !saidUno) {
                System.out.println(name + " forgot to call UNO!");
                // Could add penalty here if needed
            }

            return playedCard;
        }
        return null;
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
        System.out.println("0. Draw a card");
    }

    /**
     * Gets the player's choice for which card to play
     * @param topCard The current top card for reference
     * @return The index of the chosen card, or -1 to draw a card
     */
    public int getCardChoice(Card topCard) {
        int maxAttempts = 3;
        int attempts = 0;

        while (attempts < maxAttempts) {
            try {
                System.out.println("\nCurrent card: " + topCard);
                displayHand();
                System.out.print("Choose a card (enter number): ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Clear the newline character

                // Validate choice range
                if (choice == 0) {
                    return -1; // Draw card
                } else if (choice >= 1 && choice <= hand.size()) {
                    return choice - 1; // Convert to 0-based index
                } else {
                    System.out.println("Invalid choice! Please enter a number between 0 and " + hand.size() + " eingeben.");
                    attempts++;
                }

            } catch (InputMismatchException e) {
                // Handle invalid input (non-integer)
                scanner.nextLine(); // Clear the invalid input
                System.out.println("Invalid input! Please enter a number.");
                attempts++;
            }
        }

        System.out.println("Too many invalid attempts. A penalty card will be drawn automatically.");
        return -1; // Default to drawing a card
    }

    /**
     * Asks player to choose a color for wild cards
     * @return The chosen color
     */
    public CardColor chooseColor() {
        int maxAttempts = 3;
        int attempts = 0;

        while (attempts < maxAttempts) {
            try {
                System.out.println("\nChoose a color: ");
                System.out.println("1. Red");
                System.out.println("2. Yellow");
                System.out.println("3. Green");
                System.out.println("4. Blue");
                System.out.print("Your choice: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Clear the newline character

                switch (choice) {
                    case 1: return CardColor.RED;
                    case 2: return CardColor.YELLOW;
                    case 3: return CardColor.GREEN;
                    case 4: return CardColor.BLUE;
                    default:
                        System.out.println("Invalid input! Please enter a number between 1 and 4.");
                        attempts++;
                }
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("Invalid input! Please enter a number.");
                attempts++;
            }
        }

        System.out.println("Too many invalid attempts. Red will be selected automatically.");
        return CardColor.RED;
    }

    /**
     * Handles the UNO call when player has one card left
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
    public boolean checkUnoViolation() {
        if (hand.size() == 1 && !saidUno) {
            System.out.println(name + " forgot to call UNO and must draw 2 cards!");
            return true;
        }
        return false;
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
    public boolean hasWon() {
        return hand.isEmpty();
    }

    // Getter and setter methods
    public String getName() { return name; }
    public List<Card> getHand() { return new ArrayList<>(hand); } // Return copy to prevent external modification
    public int getHandSize() { return hand.size(); }
    public int getTotalScore() { return totalScore; }
    public boolean hasSaidUno() { return saidUno; }
    public int getPenaltyCount() { return penaltyCount; }

    public void addScore(int points) {
        if (points >= 0) {
            totalScore += points;
        }
    }

    public void setSaidUno(boolean saidUno) { this.saidUno = saidUno; }
    public void resetPenalties() { penaltyCount = 0; }

    /**
     * Clears the player's hand for a new round
     */
    public void clearHand() {
        hand.clear();
        saidUno = false;
    }

    @Override
    public String toString() {
        return name + " (Cards: " + hand.size() + ", Points: " + totalScore + ")";
    }
}
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
    protected Scanner scanner;          // For input (only used by human players)

    /**
     * Constructor for creating a new player
     * @param name The player's name
     */
    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();  // ArrayList implements List interface
        this.totalScore = 0;
        this.penaltyCount = 0;
        this.saidUno = false;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Adds a card to the player's hand
     * @param card The card to add
     */
    public void addCard(Card card) {
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
            return hand.remove(index); // remove(index) returns the removed element
        }
        return null;
    }

    /**
     * Displays the player's hand with numbered options
     * Only shows the actual cards to the current player
     */
    public void displayHand() {
        System.out.println("\n" + name + "'s Karten:");
        for (int i = 0; i < hand.size(); i++) {
            // Using printf for formatted output: %d = integer, %s = string
            System.out.printf("%d. %s\n", i + 1, hand.get(i));
        }
        System.out.println("0. Karte ziehen");
    }

    /**
     * Gets the player's choice for which card to play
     * @param topCard The current top card for reference
     * @return The index of the chosen card, or -1 to draw a card
     */
    public int getCardChoice(Card topCard) {
        System.out.println("\nAktuelle Karte: " + topCard);
        displayHand();
        System.out.print("Wähle eine Karte (Nummer eingeben): ");

        try {
            int choice = scanner.nextInt();
            return choice - 1; // Convert to 0-based index (-1 for draw card option)
        } catch (InputMismatchException e) {
            // Handle invalid input (non-integer)
            scanner.nextLine(); // Clear the invalid input
            System.out.println("Ungültige Eingabe! Bitte eine Nummer eingeben.");
            return getCardChoice(topCard); // Recursive call to try again
        }
    }

    /**
     * Asks player to choose a color for wild cards
     * @return The chosen color
     */
    public CardColor chooseColor() {
        System.out.println("\nWähle eine Farbe:");
        System.out.println("1. Rot");
        System.out.println("2. Gelb");
        System.out.println("3. Grün");
        System.out.println("4. Blau");
        System.out.print("Deine Wahl: ");

        try {
            int choice = scanner.nextInt();
            switch (choice) {
                case 1: return CardColor.RED;
                case 2: return CardColor.YELLOW;
                case 3: return CardColor.GREEN;
                case 4: return CardColor.BLUE;
                default:
                    System.out.println("Ungültige Wahl! Rot wird automatisch gewählt.");
                    return CardColor.RED;
            }
        } catch (InputMismatchException e) {
            scanner.nextLine();
            System.out.println("Ungültige Eingabe! Rot wird automatisch gewählt.");
            return CardColor.RED;
        }
    }

    /**
     * Handles the UNO call when player has one card left
     */
    public void callUno() {
        if (hand.size() == 1) {
            saidUno = true;
            System.out.println(name + " ruft: UNO!");
        }
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
        for (Card card : hand) {
            if (card.canPlayOn(topCard)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds penalty points and increments penalty counter
     */
    public void addPenalty() {
        penaltyCount++;
        System.out.println(name + " erhält eine Strafe! (Gesamt: " + penaltyCount + ")");
    }

    /**
     * Checks if player should be disqualified (3 penalties)
     * @return true if player should be disqualified
     */
    public boolean shouldBeDisqualified() {
        return penaltyCount >= 3;
    }

    // Getter and setter methods
    public String getName() { return name; }
    public List<Card> getHand() { return hand; }
    public int getHandSize() { return hand.size(); }
    public int getTotalScore() { return totalScore; }
    public boolean hasSaidUno() { return saidUno; }
    public int getPenaltyCount() { return penaltyCount; }

    public void addScore(int points) { totalScore += points; }
    public void setSaidUno(boolean saidUno) { this.saidUno = saidUno; }
    public void resetPenalties() { penaltyCount = 0; }

    /**
     * Clears the player's hand for a new round
     */
    public void clearHand() {
        hand.clear();
        saidUno = false;
    }
}

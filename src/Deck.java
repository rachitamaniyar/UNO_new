import java.util.*;

/**
 * Manages the UNO deck including draw pile and discard pile
 * Uses Stack data structure for LIFO (Last In, First Out) behavior
 */
public class Deck {
    private Stack<Card> drawPile;    // Nachziehstapel
    private Stack<Card> discardPile; // Ablegestapel
    private Random random;           // For shuffling cards

    /**
     * Constructor initializes the deck and creates all 108 UNO cards
     */
    public Deck() {
        drawPile = new Stack<>();    // Stack is a class that extends Vector
        discardPile = new Stack<>(); // LIFO - Last card added is first card removed
        random = new Random();
        initializeDeck();
        shuffleDeck();
    }

    /**
     * Creates all 108 UNO cards according to official rules
     * 76 number cards + 24 colored action cards + 8 black special cards = 108 total
     */
    private void initializeDeck() {
        // Create number cards for each color
        for (CardColor color : Arrays.asList(CardColor.RED, CardColor.YELLOW,
                CardColor.GREEN, CardColor.BLUE)) {

            // Add one zero card per color (4 total)
            drawPile.push(new Card(color, CardType.ZERO));

            // Add two of each number 1-9 per color (72 total)
            for (CardType type : Arrays.asList(CardType.ONE, CardType.TWO, CardType.THREE,
                    CardType.FOUR, CardType.FIVE, CardType.SIX,
                    CardType.SEVEN, CardType.EIGHT, CardType.NINE)) {
                drawPile.push(new Card(color, type)); // First copy
                drawPile.push(new Card(color, type)); // Second copy
            }

            // Add two of each colored action card per color (24 total)
            drawPile.push(new Card(color, CardType.DRAW_TWO));
            drawPile.push(new Card(color, CardType.DRAW_TWO));
            drawPile.push(new Card(color, CardType.REVERSE));
            drawPile.push(new Card(color, CardType.REVERSE));
            drawPile.push(new Card(color, CardType.SKIP));
            drawPile.push(new Card(color, CardType.SKIP));
        }

        // Add black special cards (8 total)
        for (int i = 0; i < 4; i++) {
            drawPile.push(new Card(CardColor.BLACK, CardType.WILD));
            drawPile.push(new Card(CardColor.BLACK, CardType.WILD_DRAW_FOUR));
        }
    }

    /**
     * Shuffles the draw pile using Collections.shuffle()
     * Collections.shuffle() randomly permutes the specified list
     */
    public void shuffleDeck() {
        List<Card> tempList = new ArrayList<>(drawPile); // Convert Stack to List
        Collections.shuffle(tempList, random);           // Shuffle the list
        drawPile.clear();                               // Clear the original stack

        // Add shuffled cards back to stack
        // Using addAll() method to add all elements from collection
        drawPile.addAll(tempList);
    }

    /**
     * Draws a card from the draw pile
     * If draw pile is empty, reshuffles discard pile (except top card)
     * @return The drawn card
     */
    public Card drawCard() {
        // Check if draw pile is empty
        if (drawPile.isEmpty()) {
            reshuffleDiscardPile(); // Reshuffle discard pile into draw pile
        }

        // Return the top card from draw pile, or null if completely empty
        return drawPile.isEmpty() ? null : drawPile.pop();
    }

    /**
     * Zieht eine bestimmte Anzahl von Karten und fügt sie der Hand eines Spielers hinzu.
     * @param player Der Spieler, der die Karten erhält.
     * @param numCards Die Anzahl der zu ziehenden Karten.
     */
    public void drawCards(Player player, int numCards) {
        for (int i = 0; i < numCards; i++) {
            Card drawnCard = drawCard(); // Ruft die bestehende drawCard() Methode auf
            if (drawnCard != null) {
                player.addCard(drawnCard);
            } else {
                System.out.println("No more cards to draw from the deck.");
                break; // Deck ist leer, keine weiteren Karten ziehen
            }
        }
    }

    /**
     * Reshuffles the discard pile back into the draw pile
     * Keeps the top card of discard pile as the current card
     */
    private void reshuffleDiscardPile() {
        if (discardPile.size() <= 1) {
            return; // Can't reshuffle if only one or no cards in discard pile
        }

        Card topCard = discardPile.pop(); // Remove and save the top card

        // Move all remaining discard cards to draw pile
        while (!discardPile.isEmpty()) {
            Card card = discardPile.pop();
            // Reset wild card colors to black for reshuffling
            if (card.getType() == CardType.WILD || card.getType() == CardType.WILD_DRAW_FOUR) {
                card.setColor(CardColor.BLACK);
            }
            drawPile.push(card);
        }

        shuffleDeck();              // Shuffle the new draw pile
        discardPile.push(topCard);  // Put the top card back on discard pile

        System.out.println("No more cards to draw - so the discard pile has been reshuffled!");
    }

    /**
     * Plays a card to the discard pile
     * @param card The card to be played
     */
    public void playCard(Card card) {
        discardPile.push(card);
    }

    /**
     * Gets the top card of the discard pile without removing it
     * peek() method returns the top element without removing it
     * @return The top card of discard pile, or null if empty
     */
    public Card getTopCard() {
        return discardPile.isEmpty() ? null : discardPile.peek();
    }

    /**
     * Sets up the initial game by placing the first card on discard pile
     * Ensures the first card is not a Wild Draw Four card
     */
    public void setupInitialCard() {
        Card firstCard;
        do {
            firstCard = drawCard();
            // If it's a Wild Draw Four, put it back and draw another
            if (firstCard != null && firstCard.getType() == CardType.WILD_DRAW_FOUR) {
                drawPile.add(0, firstCard); // Add to bottom of deck using add(index, element)
                shuffleDeck();
            }
        } while (firstCard != null && firstCard.getType() == CardType.WILD_DRAW_FOUR);

        if (firstCard != null) {
            playCard(firstCard);
            System.out.println("Starting card: " + firstCard);
        }
    }

    // Getter methods
    public int getDrawPileSize() { return drawPile.size(); }
    public int getDiscardPileSize() { return discardPile.size(); }
}

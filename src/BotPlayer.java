import java.util.*;

/**
 * Represents an AI/Bot player that extends the Player class
 * Implements automatic decision making for card selection
 */
public class BotPlayer extends Player {
    private Random random;
    private int difficulty; // 1 = Easy, 2 = Medium, 3 = Hard

    /**
     * Constructor for bot player with difficulty level
     * @param name Bot's name
     * @param difficulty Difficulty level (1-3)
     */
    public BotPlayer(String name, int difficulty) {
        super(name); // Call parent constructor using super keyword
        this.random = new Random();
        this.difficulty = difficulty;
    }

    /**
     * Automatically generates bot names
     * @param index Bot number for unique naming
     * @return Generated bot name
     */
    public static String generateBotName(int index) {
        String[] botNames = {"Bot-Alpha", "Bot-Beta", "Bot-Gamma", "Bot-Delta"};
        return botNames[index % botNames.length];
    }

    /**
     * Bot's automatic card selection logic
     * @param topCard Current top card on discard pile
     * @return Index of card to play, or -1 to draw
     */
    @Override
    public int getCardChoice(Card topCard) {
        System.out.println("\n" + name + " ist am Zug...");

        // Simulate thinking time
        try {
            Thread.sleep(1000 + random.nextInt(1000)); // Sleep 1-2 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
        }

        List<Integer> playableCards = new ArrayList<>();

        // Find all playable cards
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).canPlayOn(topCard)) {
                playableCards.add(i);
            }
        }

        if (playableCards.isEmpty()) {
            System.out.println(name + " zieht eine Karte.");
            return -1; // Draw a card
        }

        // Different strategies based on difficulty
        int chosenIndex = selectCardByDifficulty(playableCards, topCard);

        // Auto-call UNO if down to one card
        if (hand.size() == 2) { // Will be 1 after playing this card
            callUno();
        }

        System.out.println(name + " spielt: " + hand.get(chosenIndex));
        return chosenIndex;
    }

    /**
     * Selects card based on bot difficulty level
     * @param playableCards List of indices of playable cards
     * @param topCard Current top card
     * @return Index of selected card
     */
    private int selectCardByDifficulty(List<Integer> playableCards, Card topCard) {
        switch (difficulty) {
            case 1: // Easy - Random selection
                return playableCards.get(random.nextInt(playableCards.size()));

            case 2: // Medium - Prefer action cards
                return selectMediumStrategy(playableCards);

            case 3: // Hard - Strategic play
                return selectHardStrategy(playableCards, topCard);

            default:
                return playableCards.get(0); // Fallback
        }
    }

    /**
     * Medium difficulty strategy - prefers action cards
     */
    private int selectMediumStrategy(List<Integer> playableCards) {
        // First, look for action cards
        for (int index : playableCards) {
            if (hand.get(index).isActionCard()) {
                return index;
            }
        }
        // If no action cards, pick random
        return playableCards.get(random.nextInt(playableCards.size()));
    }

    /**
     * Hard difficulty strategy - prioritizes high-value cards and strategic plays
     */
    private int selectHardStrategy(List<Integer> playableCards, Card topCard) {
        int bestIndex = playableCards.get(0);
        int highestPoints = hand.get(bestIndex).getPoints();

        // Find the highest point card
        for (int index : playableCards) {
            Card card = hand.get(index);
            if (card.getPoints() > highestPoints) {
                highestPoints = card.getPoints();
                bestIndex = index;
            }
        }

        return bestIndex;
    }

    /**
     * Bot's automatic color selection for wild cards
     * Chooses based on the most common color in hand
     */
    @Override
    public CardColor chooseColor() {
        // Count colors in hand
        Map<CardColor, Integer> colorCount = new HashMap<>();
        colorCount.put(CardColor.RED, 0);
        colorCount.put(CardColor.YELLOW, 0);
        colorCount.put(CardColor.GREEN, 0);
        colorCount.put(CardColor.BLUE, 0);

        for (Card card : hand) {
            if (card.getColor() != CardColor.BLACK) {
                colorCount.put(card.getColor(), colorCount.get(card.getColor()) + 1);
            }
        }

        // Find the most common color
        CardColor mostCommon = CardColor.RED;
        int maxCount = 0;

        for (Map.Entry<CardColor, Integer> entry : colorCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostCommon = entry.getKey();
            }
        }

        System.out.println(name + " wählt die Farbe: " + mostCommon);
        return mostCommon;
    }

    /**
     * Bot automatically calls UNO when appropriate
     */
    @Override
    public void callUno() {
        super.callUno();
        // Bots have a small chance to forget UNO call based on difficulty
        if (difficulty == 1 && random.nextInt(10) == 0) { // 10% chance for easy bots
            saidUno = false; // "Forget" to call UNO
        }
    }
}

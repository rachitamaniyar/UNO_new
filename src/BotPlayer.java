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

    // only left in BotPlayer
    /**
     * Bot's automatic card selection logic
     * @param topCard Current top card on discard pile
     * @return Index of card to play, or -1 to draw
     */
    public int getCardChoice(Card topCard) {
        System.out.println("\n It's " + name + "'s turn.");

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
            System.out.println(name + " draws a card.");
            return -1; // Draw a card
        }

        // Different strategies based on difficulty
        int chosenIndex = selectCardByDifficulty(playableCards, topCard);

        // Auto-call UNO if down to one card
        // Check hand.size() BEFORE playing the card. If it's 2, it will be 1 after playing.
        if (hand.size() == 2) { // Will be 1 after playing this card
            // the Bot-specific Logic "Forget" is in the overwritten callUNO()
            // the callUNO() sets the flag according to the bot-logic
            callUno();
        }

        // (REMOVED) the print of the turn - should be in RUN after the actual card has been playes
        // System.out.println(name + " plays: " + hand.get(chosenIndex));
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
    public CardColor chooseColor() {
        // Count colors in hand
        Map<CardColor, Integer> colorCount = new HashMap<>();
        colorCount.put(CardColor.RED, 0);
        colorCount.put(CardColor.YELLOW, 0);
        colorCount.put(CardColor.GREEN, 0);
        colorCount.put(CardColor.BLUE, 0);

        for (Card card : hand) {
            // (MODIFIED)
            if (card.getColor() != CardColor.BLACK) {
                colorCount.put(card.getColor(), colorCount.get(card.getColor()) + 1);
            }
        }

        // (MODIFIED)
        // Find the most common color
        CardColor mostCommon = CardColor.RED;
        int maxCount = -1; // starting with -1, to also process 0-count colours

        // if the hand is empty or holds only wild-cards, chose a colour randomly
        if (hand.isEmpty() || colorCount.values().stream().allMatch(count -> count == 0)) {
            CardColor[] colors = {CardColor.RED, CardColor.YELLOW, CardColor.GREEN, CardColor.BLUE};
            mostCommon = colors[random.nextInt(colors.length)];
        } else {
            for (Map.Entry<CardColor, Integer> entry : colorCount.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    mostCommon = entry.getKey();
                }
            }
        }
        System.out.println(name + " chooses a color: " + mostCommon);
        return mostCommon;
    }

        /**
         * Bot automatically calls UNO when appropriate
         * Includes a chance for easy bots to forget
         */
        @Override
        public void callUno() {
            // super.callUno();
            // Bots have a small chance to forget UNO call based on difficulty
            if (hand.size() == 1) { // only apply if just 1 card is left
                boolean shouldForget = false;
                if (difficulty == 1 && random.nextInt(10) == 0) { // 10% chance for easy bots
                    shouldForget = true;
                }
                super.setSaidUno(!shouldForget); // sets the flag according to the Forget-Logic
            }
        }
    }

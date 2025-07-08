import java.util.*;

/**
 * Handles game initialization and setup
 * Sets up players, deck, and initial game state
 */
public class Initialization {
    private Menu menu;
    private List<Player> players;
    private Deck deck;
    private int difficulty;

    public Initialization() {
        menu = new Menu();
        players = new ArrayList<>();
    }

    /**
     * Initializes the complete game setup
     * @return GameSetup object with all initialized components
     */
    public GameSetup initializeGame() {
        menu.displayWelcome();

        // Get game settings from user
        difficulty = menu.selectDifficulty();
        int humanPlayers = menu.getNumberOfHumanPlayers();
        boolean specialRules = menu.askForSpecialRules();

        // Create players
        createPlayers(humanPlayers);

        // Initialize deck and deal cards
        deck = new Deck();
        dealInitialCards();

        // Set up first card
        deck.setupInitialCard();

        // Randomly select starting player
        int startingPlayer = selectStartingPlayer();

        System.out.println("\n🎮 The game starts!");
        System.out.println("Starting player: " + players.get(startingPlayer).getName());

        return new GameSetup(players, deck, startingPlayer, difficulty, specialRules);
    }

    /**
     * Creates human and bot players based on user input
     * @param numberOfHumans Number of human players to create
     */
    private void createPlayers(int numberOfHumans) {
        // Get names for human players
        String[] humanNames = new String[0];
        if (numberOfHumans > 0) {
            humanNames = menu.getPlayerNames(numberOfHumans);
        }

        // Create human players
        for (int i = 0; i < numberOfHumans; i++) {
            players.add(new Player(humanNames[i]));
        }

        // Create bot players to fill up to 4 total players
        int numberOfBots = 4 - numberOfHumans;
        for (int i = 0; i < numberOfBots; i++) {
            String botName = BotPlayer.generateBotName(i);
            players.add(new BotPlayer(botName, difficulty));
        }

        System.out.println("\n👥 Players created:");
        for (Player player : players) {
            String type = (player instanceof BotPlayer) ? "🤖 Bot" : "👤 Human";
            System.out.println("   " + type + ": " + player.getName());
        }
    }

    /**
     * Deals 7 cards to each player
     */
    private void dealInitialCards() {
        System.out.println("\n🃏 Dealing the cards...");

        // Deal 7 cards to each player
        for (int cardNum = 0; cardNum < 7; cardNum++) {
            for (Player player : players) {
                Card card = deck.drawCard();
                if (card != null) {
                    player.addCard(card);
                }
            }
        }

        System.out.println("Each player has received 7 cards.");
    }

    /**
     * Randomly selects the starting player
     * @return Index of the starting player
     */
    private int selectStartingPlayer() {
        Random random = new Random();
        return random.nextInt(players.size());
    }

    /**
     * Prepares a new round (clears hands, redeals cards)
     */
    public void prepareNewRound() {
        System.out.println("\n🔄 Preparing a new round...");

        // Clear all players' hands
        for (Player player : players) {
            player.clearHand();
        }

        // Create new deck and deal cards
        deck = new Deck();
        dealInitialCards();
        deck.setupInitialCard();

        System.out.println("New round is ready!");
    }

    /**
     * Helper class to return all game setup information
     */
    public static class GameSetup {
        public final List<Player> players;
        public final Deck deck;
        public final int startingPlayerIndex;
        public final int difficulty;
        public final boolean specialRulesEnabled;

        public GameSetup(List<Player> players, Deck deck, int startingPlayerIndex,
                         int difficulty, boolean specialRulesEnabled) {
            this.players = players;
            this.deck = deck;
            this.startingPlayerIndex = startingPlayerIndex;
            this.difficulty = difficulty;
            this.specialRulesEnabled = specialRulesEnabled;
        }
    }

    // Getter methods
    public Menu getMenu() { return menu; }
    public List<Player> getPlayers() { return players; }
    public Deck getDeck() { return deck; }
}

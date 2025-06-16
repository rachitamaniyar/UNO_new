import java.util.*;

/**
 * Main game loop and gameplay logic
 * Handles turn management and game flow
 */
public class Run {
    private List<Player> players;
    private Deck deck;
    private Referee referee;
    private Menu menu;
    private int currentPlayerIndex;
    private int direction; // 1 for clockwise, -1 for counter-clockwise
    private boolean gameRunning;
    private boolean specialRulesEnabled;
Scanner scanner = new Scanner(System.in);
    /**
     * Constructor for the game runner
     * @param gameSetup Initial game setup
     */
    public Run(Initialization.GameSetup gameSetup) {
        this.players = gameSetup.players;
        this.deck = gameSetup.deck;
        this.currentPlayerIndex = gameSetup.startingPlayerIndex;
        this.specialRulesEnabled = gameSetup.specialRulesEnabled;
        this.direction = 1; // Start clockwise
        this.gameRunning = true;
        this.referee = new Referee(players, deck);
        this.menu = new Menu();
    }

    /**
     * Main game loop - continues until someone wins or quits
     */
    public void runGame() {
        System.out.println("\n🎮 Das Spiel beginnt!");

        // Check if first card is special and handle it
        handleStartingSpecialCard();

        while (gameRunning) {
            // Check for game end conditions
            if (checkGameEndConditions()) {
                break;
            }

            // Play one turn
            playTurn();

            // Move to next player
            moveToNextPlayer();
        }
    }

    /**
     * Handles special cards that might be revealed at game start
     */
    private void handleStartingSpecialCard() {
        Card topCard = deck.getTopCard();
        if (topCard != null && SpecialCards.isSpecialCard(topCard)) {
            Player[] playerArray = players.toArray(new Player[0]);
            SpecialCards.GameStartInfo info = SpecialCards.handleStartingSpecialCard(
                    topCard, currentPlayerIndex, playerArray, deck);

            direction = info.direction;
            if (info.skipFirstPlayer) {
                moveToNextPlayer(); // Skip the first player
            }
        }
    }

    /**
     * Plays one complete turn for the current player
     */
    private void playTurn() {
        Player currentPlayer = players.get(currentPlayerIndex);
        Card topCard = deck.getTopCard();

        // Display game state
        menu.displayGameState(players, currentPlayer, topCard, direction);

        // Check if player needs to draw cards due to previous action
        if (shouldDrawCards(currentPlayer)) {
            return; // Player's turn is over after drawing penalty cards
        }

        // Check if player has playable cards
        if (!currentPlayer.hasPlayableCard(topCard)) {
            handleNoPlayableCards(currentPlayer);
            return;
        }

        // Get player's card choice
        int cardChoice = currentPlayer.getCardChoice(topCard);

        if (cardChoice == -1) {
            // Player chooses to draw a card
            handleDrawCard(currentPlayer);
        } else {
            // Player chooses to play a card
            handlePlayCard(currentPlayer, cardChoice);
        }
    }

    /**
     * Checks if current player should draw cards due to previous special cards
     * @param player Current player
     * @return true if player drew cards and turn is over
     */
    private boolean shouldDrawCards(Player player) {
        // This is handled by special card effects in previous turns
        // For now, we'll implement basic logic
        return false;
    }

    /**
     * Handles when player has no playable cards
     * @param player Current player
     */
    private void handleNoPlayableCards(Player player) {
        System.out.println(player.getName() + " hat keine spielbare Karte und muss ziehen.");
        Card drawnCard = deck.drawCard();

        if (drawnCard != null) {
            player.addCard(drawnCard);
            System.out.println(player.getName() + " zieht: " + drawnCard);

            // Check if drawn card can be played immediately
            Card topCard = deck.getTopCard();
            if (drawnCard.canPlayOn(topCard)) {
                System.out.println("Die gezogene Karte kann gespielt werden!");

                if (player instanceof BotPlayer) {
                    // Bots automatically play if they can
                    playDrawnCard(player, drawnCard);
                } else {
                    // Ask human player
                    drawnCard.toString();
                    System.out.print("Möchtest du die gezogene Karte spielen? (j/n): ");
                    String choice = scanner.next().toLowerCase();

                    if (choice.equals("j") || choice.equals("ja")) {
                        playDrawnCard(player, drawnCard);
                    }
                }
            }
        }
    }

    /**
     * Handles when player chooses to draw a card
     * @param player Current player
     */
    private void handleDrawCard(Player player) {
        Card drawnCard = deck.drawCard();
        if (drawnCard != null) {
            player.addCard(drawnCard);
            System.out.println(player.getName() + " zieht eine Karte.");

            // Optionally allow immediate play of drawn card
            Card topCard = deck.getTopCard();
            if (drawnCard.canPlayOn(topCard)) {
                if (player instanceof BotPlayer) {
                    // Bots automatically decide
                    if (Math.random() < 0.7) { // 70% chance to play
                        playDrawnCard(player, drawnCard);
                    }
                } else {
                    System.out.println("Die gezogene Karte kann gespielt werden!");
                    System.out.print("Möchtest du sie spielen? (j/n): ");
                    String choice = scanner.next().toLowerCase();

                    if (choice.equals("j") || choice.equals("ja")) {
                        playDrawnCard(player, drawnCard);
                    }
                }
            }
        }
    }

    /**
     * Plays a card that was just drawn
     * @param player The player
     * @param drawnCard The card that was drawn
     */
    private void playDrawnCard(Player player, Card drawnCard) {
        player.getHand().remove(drawnCard); // Remove from hand
        playCard(player, drawnCard); // Play the card
    }

    /**
     * Handles when player chooses to play a card
     * @param player Current player
     * @param cardChoice Index of chosen card
     */
    private void handlePlayCard(Player player, int cardChoice) {
        if (cardChoice < 0 || cardChoice >= player.getHandSize()) {
            System.out.println("Ungültige Kartenwahl!");
            return;
        }

        Card chosenCard = player.getHand().get(cardChoice);
        Card topCard = deck.getTopCard();

        // Validate the card play
        if (!referee.validateCardPlay(chosenCard, topCard, player)) {
            return; // Invalid play, penalties already applied
        }

        // Remove card from player's hand and play it
        Card playedCard = player.playCard(cardChoice);
        playCard(player, playedCard);
    }

    /**
     * Plays a card to the discard pile and handles its effects
     * @param player Player who played the card
     * @param card Card being played
     */
    private void playCard(Player player, Card card) {
        // Play card to discard pile
        deck.playCard(card);
        System.out.println(player.getName() + " spielt: " + card);
        System.out.println("Do you want to end your turn? Y/N");
        scanner.nextLine();
        if(scanner.next().equalsIgnoreCase("N")) {
                        menu.showGameMenu();
                    } else if (scanner.next().equalsIgnoreCase("Y")) {



        // Check for UNO call
        if (player.getHandSize() == 1) {
            System.out.println("Would you like to call UNO? Y/N");
            scanner.nextLine();
            if (scanner.nextLine().toLowerCase().equals("y")) {
                player.callUno();
            }
               else {
                    // Check if other players catch the UNO violation
                    referee.checkUnoViolation(player);
                }

        }

        // Handle special card effects
        if (SpecialCards.isSpecialCard(card)) {
            handleSpecialCardEffects(player, card);
        }

        // Check if player won the round
        if (player.getHandSize() == 0) {
            handleRoundWin(player);
        }
        }
    }

    /**
     * Handles the effects of special cards
     * @param player Player who played the card
     * @param card The special card
     */
    private void handleSpecialCardEffects(Player player, Card card) {
        int nextPlayerIndex = getNextPlayerIndex();
        Player nextPlayer = players.get(nextPlayerIndex);

        switch (card.getType()) {
            case DRAW_TWO:
                SpecialCards.processDrawTwo(nextPlayer, deck);
                skipNextPlayer();
                break;

            case REVERSE:
                direction = SpecialCards.processReverse(direction);
                break;

            case SKIP:
                SpecialCards.processSkip(nextPlayer);
                skipNextPlayer();
                break;

            case WILD:
                SpecialCards.processWild(player, card);
                break;

            case WILD_DRAW_FOUR:
                // Check for challenge
                if (menu.askForChallenge(nextPlayer, player)) {
                    referee.handleWildDrawFourChallenge(nextPlayer, player,
                            deck.getTopCard());
                } else {
                    SpecialCards.processWildDrawFour(player, nextPlayer, card, deck);
                    skipNextPlayer();
                }
                break;
        }
    }

    /**
     * Handles when a player wins a round
     * @param winner The winning player
     */
    private void handleRoundWin(Player winner) {
        System.out.println("\n🎉 " + winner.getName() + " hat die Runde gewonnen!");

        // Calculate and award points
        referee.calculateRoundScore(winner);

        // Check if someone won the game
        Player gameWinner = referee.checkGameWinner();
        if (gameWinner != null) {
            handleGameWin(gameWinner);
        } else {
            // Start new round
            System.out.println("\nNeue Runde beginnt...");
            prepareNewRound();
        }
    }

    /**
     * Handles when someone wins the entire game
     * @param winner The game winner
     */
    private void handleGameWin(Player winner) {
        menu.displayGameResults(winner, players);
        gameRunning = false;
    }

    /**
     * Prepares a new round
     */
    private void prepareNewRound() {
        // Clear hands and reset penalties
        for (Player player : players) {
            player.clearHand();
            player.resetPenalties();
        }

        // Reset deck and deal new cards
        deck = new Deck();

        // Deal 7 cards to each player
        for (int i = 0; i < 7; i++) {
            for (Player player : players) {
                Card card = deck.drawCard();
                if (card != null) {
                    player.addCard(card);
                }
            }
        }

        deck.setupInitialCard();

        // Reset game state
        direction = 1;
        currentPlayerIndex = 0;
    }

    /**
     * Moves to the next player
     */
    private void moveToNextPlayer() {
        currentPlayerIndex = getNextPlayerIndex();
    }

    /**
     * Gets the index of the next player
     * @return Next player index
     */
    private int getNextPlayerIndex() {
        int nextIndex = currentPlayerIndex + direction;

        if (nextIndex >= players.size()) {
            nextIndex = 0;
        } else if (nextIndex < 0) {
            nextIndex = players.size() - 1;
        }

        return nextIndex;
    }

    /**
     * Skips the next player's turn
     */
    private void skipNextPlayer() {
        moveToNextPlayer(); // Skip the next player
    }

    /**
     * Checks for game end conditions
     * @return true if game should end
     */
    private boolean checkGameEndConditions() {
        // Check for disqualifications
        List<Player> disqualified = referee.checkDisqualifications();
        players.removeAll(disqualified);

        // If too few players remain, end game
        if (players.size() < 2) {
            System.out.println("Nicht genug Spieler übrig! Spiel beendet.");
            return true;
        }

        // Adjust current player index if needed
        if (currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }

        return false;
    }
}

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
    private Scanner scanner; // Use shared scanner
    private int roundNumber = 1;

    /**
     * Constructor for the game runner
     * @param gameSetup Initial game setup
     */
    public Run(Initialization.GameSetup gameSetup) {
        if (gameSetup == null) {
            throw new IllegalArgumentException("Game setup cannot be null");
        }

        this.players = gameSetup.players;
        this.deck = gameSetup.deck;
        this.currentPlayerIndex = gameSetup.startingPlayerIndex;
        this.specialRulesEnabled = gameSetup.specialRulesEnabled;
        this.direction = 1; // Start clockwise
        this.gameRunning = true;
        this.referee = new Referee(players, deck);
        this.menu = new Menu();
        this.scanner = Main.getScanner(); // Use shared scanner from Main

        if (currentPlayerIndex < 0 || currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }
    }

    /**
     * Main game loop - continues until someone wins or quits
     */
    public void runGame() {
        System.out.println("\n🎮 The game begins!");
        System.out.println("Players: " + players.size());
        System.out.println("Starting player: " + players.get(currentPlayerIndex).getName());

        handleStartingSpecialCard();

        while (gameRunning) {
            if (checkGameEndConditions()) break;
            if (isDeckCompletelyEmpty()) {
                handleEmptyDeck();
                break;
            }

            try {
                playTurn();
                moveToNextPlayer();
            } catch (Exception e) {
                System.err.println("Error during turn: " + e.getMessage());
                System.out.println("Ending game...");
                break;
            }
        }

        System.out.println("Game over.");
    }

    private boolean isDeckCompletelyEmpty() {
        return deck.getDrawPileSize() == 0 && deck.getDiscardPileSize() <= 1;
    }

    private void handleEmptyDeck() {
        System.out.println("\n⚠️ Both card piles are empty! The game ends in a draw.");

        System.out.println("\nCurrent player scores:");
        for (Player player : players) {
            int handPoints = player.calculateHandPoints();
            System.out.println(player.getName() + ": " + handPoints + " points in hand");
        }

        gameRunning = false;
    }

    private void handleStartingSpecialCard() {
        Card topCard = deck.getTopCard();
        if (topCard != null && SpecialCards.isSpecialCard(topCard)) {
            System.out.println("Starting card is a special card: " + topCard);

            Player[] playerArray = players.toArray(new Player[0]);
            SpecialCards.GameStartInfo info = SpecialCards.handleStartingSpecialCard(
                    topCard, currentPlayerIndex, playerArray, deck);

            direction = info.direction;
            if (info.skipFirstPlayer) {
                System.out.println("The first player is skipped!");
                moveToNextPlayer();
            }
        }
    }

    private void playTurn() {
        Player currentPlayer = players.get(currentPlayerIndex);
        Card topCard = deck.getTopCard();

        if (currentPlayer == null || topCard == null) {
            System.err.println("Critical error: Player or card is null");
            gameRunning = false;
            return;
        }

        menu.displayGameState(players, currentPlayer, topCard, direction);

        if (shouldDrawCards(currentPlayer)) return;

        if (!currentPlayer.hasPlayableCard(topCard)) {
            handleNoPlayableCards(currentPlayer);
            return;
        }

        int cardChoice = currentPlayer.getCardChoice(topCard);

        if (cardChoice == -1) {
            handleDrawCard(currentPlayer);
        } else {
            handlePlayCard(currentPlayer, cardChoice);
        }
    }

    private boolean shouldDrawCards(Player player) {
        return false;
    }

    private void handleNoPlayableCards(Player player) {
        System.out.println(player.getName() + " has no playable cards and must draw.");
        Card drawnCard = deck.drawCard();

        if (drawnCard != null) {
            player.addCard(drawnCard);
            System.out.println(player.getName() + " draws: " + drawnCard);

            Card topCard = deck.getTopCard();
            if (drawnCard.canPlayOn(topCard)) {
                System.out.println("The drawn card can be played!");

                if (player instanceof BotPlayer) {
                    playDrawnCard(player, drawnCard);
                } else {
                    System.out.print("Do you want to play the drawn card (" + drawnCard + ")? (y/n): ");
                    String choice = scanner.nextLine().toLowerCase().trim();

                    if (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("yes")) {
                        playDrawnCard(player, drawnCard);
                    }
                }
            }
        } else {
            System.out.println("No more cards in the draw pile!");
        }
    }

    // hier müsste noch die Variante mit (no) ergänzt werden, falls nicht schon geschehen
    private void handleDrawCard(Player player) {
        Card drawnCard = deck.drawCard();
        if (drawnCard != null) {
            player.addCard(drawnCard);
            System.out.println(player.getName() + " draws a card.");

            Card topCard = deck.getTopCard();
            if (drawnCard.canPlayOn(topCard)) {
                if (player instanceof BotPlayer) {
                    if (Math.random() < 0.7) {
                        playDrawnCard(player, drawnCard);
                    }
                } else {
                    System.out.println("The drawn card (" + drawnCard + ") can be played!");
                    System.out.print("Do you want to play it? (y/n): ");
                    String choice = scanner.nextLine().toLowerCase().trim();

                    if (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("yes")) {
                        playDrawnCard(player, drawnCard);
                    }
                }
            }
        } else {
            System.out.println("No cards available to draw!");
        }
    }

    private void playDrawnCard(Player player, Card drawnCard) {
        List<Card> hand = player.getHand();
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).equals(drawnCard)) {
                Card removedCard = player.playCard(i);
                playCard(player, removedCard);
                break;
            }
        }
    }

    private void handlePlayCard(Player player, int cardChoice) {
        if (cardChoice < 0 || cardChoice >= player.getHandSize()) {
            System.out.println("Invalid card choice!");
            return;
        }

        List<Card> hand = player.getHand();
        Card chosenCard = hand.get(cardChoice);
        Card topCard = deck.getTopCard();

        if (!referee.validateCardPlay(chosenCard, topCard, player)) {
            return;
        }

        Card playedCard = player.playCard(cardChoice);
        if (playedCard != null) {
            playCard(player, playedCard);
        }
    }

    // yes / no varianten vollständig?
    private void playCard(Player player, Card card) {
        deck.playCard(card);
        System.out.println(player.getName() + " plays: " + card);

        // Prompt to end turn early
        System.out.println("Do you want to end your turn? (y/n)");
        scanner.nextLine(); // Consume newline
        String input = scanner.nextLine().trim().toLowerCase();

        if (input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no")) {
            int menuChoice = menu.showGameMenu();
            switch (menuChoice) {
                case 1:
                    player.callUno();
                    break;
                // case 2: referee.handleWildDrawFourChallenge(player, player);
            }
        }

        // UNO check
        if (player.getHandSize() == 1) {
            if (!(player instanceof BotPlayer)) {
                System.out.print("Do you want to call UNO? (y/n): ");
                String unoChoice = scanner.nextLine().trim().toLowerCase();
                if (unoChoice.equalsIgnoreCase("y") || unoChoice.equalsIgnoreCase("yes")) {
                    player.callUno();
                } else {
                    referee.checkUnoViolation(player);
                }
            } else {
                player.callUno();
            }
        }

        // Special card logic
        if (SpecialCards.isSpecialCard(card)) {
            handleSpecialCardEffects(player, card);
        }

        // Win check
        if (player.getHandSize() == 0) {
            handleRoundWin(player);
            return;
        }

        // Prompt to end turn again (if not already)
        if (!(player instanceof BotPlayer)) {
            System.out.print("End turn? (y/n): ");
            String endTurn = scanner.nextLine().trim().toLowerCase();
            if (endTurn.equalsIgnoreCase("n") || endTurn.equalsIgnoreCase("no")) {
                menu.showGameMenu();
            }
        }
    }


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
                System.out.println("Play direction reversed!");
                break;

            case SKIP:
                SpecialCards.processSkip(nextPlayer);
                skipNextPlayer();
                break;

            case WILD:
                SpecialCards.processWild(player, card);
                break;

            case WILD_DRAW_FOUR:
                if (!(nextPlayer instanceof BotPlayer) && menu.askForChallenge(nextPlayer, player)) {
                    referee.handleWildDrawFourChallenge(nextPlayer, player, deck.getTopCard());
                } else {
                    SpecialCards.processWildDrawFour(player, nextPlayer, card, deck);
                    skipNextPlayer();
                }
                break;
        }
    }

    private void handleRoundWin(Player winner) {
        System.out.println("\n🎉 " + winner.getName() + " has won round " + roundNumber + "!");

        referee.calculateRoundScore(winner);

        Player gameWinner = referee.checkGameWinner();
        if (gameWinner != null) {
            handleGameWin(gameWinner);
        } else {
            roundNumber++;
            System.out.println("\nStarting round " + roundNumber + "...");
            prepareNewRound();
        }
    }

    private void handleGameWin(Player winner) {
        System.out.println("\n🏆 " + winner.getName() + " has won the entire game!");
        menu.displayGameResults(winner, players);
        gameRunning = false;
    }

    private void prepareNewRound() {
        for (Player player : players) {
            player.clearHand();
            player.resetPenalties();
        }

        deck = new Deck();

        for (int i = 0; i < 7; i++) {
            for (Player player : players) {
                Card card = deck.drawCard();
                if (card != null) {
                    player.addCard(card);
                }
            }
        }

        deck.setupInitialCard();

        direction = 1;
        currentPlayerIndex = 0;
    }

    private void moveToNextPlayer() {
        currentPlayerIndex = getNextPlayerIndex();
    }

    private int getNextPlayerIndex() {
        int nextIndex = currentPlayerIndex + direction;

        if (nextIndex >= players.size()) {
            nextIndex = 0;
        } else if (nextIndex < 0) {
            nextIndex = players.size() - 1;
        }

        return nextIndex;
    }

    private void skipNextPlayer() {
        moveToNextPlayer();
        System.out.println("Next player is skipped!");
    }

    private boolean checkGameEndConditions() {
        List<Player> disqualified = referee.checkDisqualifications();
        players.removeAll(disqualified);

        if (players.size() < 2) {
            System.out.println("Not enough players remaining! Game ends.");
            return true;
        }

        if (currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }

        return false;
    }
}
import java.util.*;

/**
 * Main game loop and gameplay logic for UNO.
 * Handles turn management, database integration, and game flow.
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
    private Scanner scanner;
    private int roundNumber = 1;

    // Database integration
    private ScoreDatabaseManager dbManager;
    private int sessionId;
    private String gameVariant;

    /**
     * Constructor for the game runner with database support.
     * @param gameSetup Initial game setup
     * @param scanner The shared Scanner instance
     * @param menu The shared Menu instance
     * @param dbManager The ScoreDatabaseManager instance (nullable)
     * @param sessionId The current session ID (0 if not using DB)
     * @param gameVariant The game variant string ("Standard", "Special Rules", etc.)
     */
    public Run(Initialization.GameSetup gameSetup, Scanner scanner, Menu menu,
               ScoreDatabaseManager dbManager, int sessionId, String gameVariant) {
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
        this.menu = menu;
        this.scanner = scanner;
        this.dbManager = dbManager;
        this.sessionId = sessionId;
        this.gameVariant = gameVariant;
        if (currentPlayerIndex < 0 || currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }
    }

    /**
     * Main game loop - continues until someone wins or quits.
     */
    public void runGame() {
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
                if (!gameRunning) break;
                moveToNextPlayer();
            } catch (Exception e) {
                System.err.println("Error during turn: " + e.getMessage());
                e.printStackTrace();
                break;
            }
        }
        if (dbManager != null && sessionId > 0) {
            try {
                String winnerName = getWinnerNameOrDraw();
                dbManager.finalizeSession(sessionId, winnerName, roundNumber);
            } catch (Exception e) {
                System.err.println("❌ Could not finalize session in database: " + e.getMessage());
            }
        }
        System.out.println("Game over.");
    }
    private String getWinnerNameOrDraw() {
        Player winner = referee.checkGameWinner();
        if (winner != null) return winner.getName();
        return "DRAW";
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
        if (dbManager != null && sessionId > 0) {
            try {
                dbManager.finalizeSession(sessionId, "DRAW", roundNumber);
            } catch (Exception e) {
                System.err.println("❌ Could not save final scores: " + e.getMessage());
            }
        }
        gameRunning = false;
    }

    private void handleStartingSpecialCard() {
        Card topCard = deck.getTopCard();
        if (topCard != null && SpecialCards.isSpecialCard(topCard)) {
            System.out.println("Starting card is a special card: " + topCard);
            Player[] playerArray = players.toArray(new Player[0]);
            SpecialCards.GameStartInfo info = SpecialCards.handleStartingSpecialCard(
                    topCard, currentPlayerIndex, playerArray, deck, menu, scanner);
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

        if (currentPlayer instanceof BotPlayer) {
            handleBotTurn((BotPlayer) currentPlayer, topCard);
        } else {
            handleHumanTurn(currentPlayer, topCard);
        }
    }

    private void handleHumanTurn(Player player, Card topCard) {
        if (player.getHandSize() == 2 && !player.hasSaidUno()) {
            System.out.print(player.getName() + ", you have 2 cards. Do you want to call UNO? (y/n): ");
            if (menu.getYesNoInput()) {
                player.callUno();
                System.out.println(player.getName() + " calls: UNO!");
            }
        }
        boolean cardPlayedOrDrawn = false;
        while (!cardPlayedOrDrawn) {
            player.displayHand();
            System.out.println("\nCurrent card: " + topCard);
            System.out.print("Choose a card (enter number 1-" + player.getHandSize() + ") or 0 to draw, or -1 for game menu: ");
            String inputLine = scanner.nextLine().trim();
            int cardChoice;
            try {
                cardChoice = Integer.parseInt(inputLine);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }
            if (cardChoice == -1) {
                int menuChoice = menu.showGameMenu();
                switch (menuChoice) {
                    case 1:
                        player.callUno();
                        System.out.println(player.getName() + " calls: UNO!");
                        break;
                    case 2:
                        System.out.println("You can only challenge a Wild Draw Four after it has been played.");
                        break;
                    case 3:
                        displayCurrentScores();
                        break;
                    case 4:
                        menu.displayRules();
                        break;
                    case 5:
                        break;
                    case 6:
                        if (menu.confirmQuit()) {
                            gameRunning = false;
                            return;
                        }
                        break;
                    default:
                        System.out.println("Invalid menu choice.");
                        break;
                }
                continue;
            } else if (cardChoice == 0) {
                handleDrawCard(player);
                cardPlayedOrDrawn = true;
            } else if (cardChoice >= 1 && cardChoice <= player.getHandSize()) {
                int actualIndex = cardChoice - 1;
                Card chosenCard = player.getHand().get(actualIndex);
                if (referee.validateCardPlay(chosenCard, topCard, player)) {
                    player.playCard(actualIndex);
                    playCard(player, chosenCard);
                    cardPlayedOrDrawn = true;
                } else {
                    System.out.println("You cannot play " + chosenCard + " on " + topCard + ". Please choose another card or draw.");
                }
            } else {
                System.out.println("Invalid card number. Please try again.");
            }
        }
    }

    private void handleBotTurn(BotPlayer bot, Card topCard) {
        int cardChoiceIndex = bot.getCardChoice(topCard);
        if (cardChoiceIndex == -1) {
            handleDrawCard(bot);
        } else {
            Card chosenCard = bot.getHand().get(cardChoiceIndex);
            if (referee.validateCardPlay(chosenCard, topCard, bot)) {
                bot.playCard(cardChoiceIndex);
                playCard(bot, chosenCard);
            } else {
                System.out.println("Bot " + bot.getName() + " tried to play an invalid card. Drawing instead.");
                handleDrawCard(bot);
            }
        }
    }

    private void handleDrawCard(Player player) {
        Card drawnCard = deck.drawCard();
        if (drawnCard != null) {
            player.addCard(drawnCard);
            System.out.println(player.getName() + " draws a card: " + drawnCard);
            Card topCard = deck.getTopCard();
            if (drawnCard.canPlayOn(topCard)) {
                if (player instanceof BotPlayer) {
                    Card playedBotCard = player.playCard(player.getHand().indexOf(drawnCard));
                    playCard(player, playedBotCard);
                } else {
                    System.out.println("The drawn card (" + drawnCard + ") can be played!");
                    System.out.print("Do you want to play it? (y/n): ");
                    if (menu.getYesNoInput()) {
                        Card playedHumanCard = player.playCard(player.getHand().indexOf(drawnCard));
                        playCard(player, playedHumanCard);
                    } else {
                        System.out.println(player.getName() + " decides not to play the drawn card.");
                    }
                }
            } else {
                System.out.println(player.getName() + " cannot play the drawn card.");
            }
        } else {
            System.out.println("No cards available to draw!");
        }
    }

    private void playCard(Player player, Card card) {
        deck.playCard(card);
        System.out.println(player.getName() + " plays: " + card);
        if (player.getHandSize() == 1) {
            if (player instanceof BotPlayer) {
                ((BotPlayer) player).callUno();
                if (player.hasSaidUno()) {
                    System.out.println("🤖 " + player.getName() + " calls: UNO!");
                }
            } else {
                System.out.print(player.getName() + ", you have 1 card left! Call UNO? (y/n): ");
                if (menu.getYesNoInput()) {
                    player.callUno();
                    System.out.println(player.getName() + " calls: UNO!");
                } else {
                    if (referee.checkUnoViolation(player)) {
                        System.out.println(player.getName() + " forgot to call UNO and draws 2 penalty cards!");
                        deck.drawCards(player, 2);
                        player.addPenalty();
                    }
                }
            }
        } else if (player.getHandSize() == 0) {
            // Player has won, no UNO message needed
        } else {
            player.setSaidUno(false);
        }
        if (SpecialCards.isSpecialCard(card)) {
            handleSpecialCardEffects(player, card);
        }
        if (player.getHandSize() == 0) {
            handleRoundWin(player);
            gameRunning = false;
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
                CardColor chosenColor;
                if (player instanceof BotPlayer) {
                    chosenColor = ((BotPlayer) player).chooseColor();
                } else {
                    chosenColor = menu.chooseColor(scanner);
                }
                SpecialCards.processWild(player, card, chosenColor);
                break;
            case WILD_DRAW_FOUR:
                CardColor chosenColorDrawFour;
                if (player instanceof BotPlayer) {
                    chosenColorDrawFour = ((BotPlayer) player).chooseColor();
                } else {
                    chosenColorDrawFour = menu.chooseColor(scanner);
                }
                SpecialCards.processWildDrawFour(player, nextPlayer, card, deck, chosenColorDrawFour);
                skipNextPlayer();
                break;
        }
    }

    private void handleRoundWin(Player winner) {
        System.out.println("\n🎉 " + winner.getName() + " has won round " + roundNumber + "!");
        referee.calculateRoundScore(winner);
        // --- DATABASE INTEGRATION: Save/display round scores ---
        if (dbManager != null && sessionId > 0) {
            try {
                Player[] playerArray = players.toArray(new Player[0]);
                int[] roundScores = new int[playerArray.length];
                for (int i = 0; i < playerArray.length; i++) {
                    roundScores[i] = playerArray[i].calculateHandPoints();
                }
                dbManager.addRoundScores(sessionId, playerArray, roundNumber, roundScores, gameVariant);
                dbManager.displayRoundScoresFromDatabase(sessionId, roundNumber);
            } catch (Exception e) {
                System.err.println("❌ Could not save/display round scores: " + e.getMessage());
            }
        }
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
        // --- DATABASE INTEGRATION: Finalize session ---
        if (dbManager != null && sessionId > 0) {
            try {
                dbManager.finalizeSession(sessionId, winner.getName(), roundNumber);
            } catch (Exception e) {
                System.err.println("❌ Could not finalize session in database: " + e.getMessage());
            }
        }
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
        if (!disqualified.isEmpty()) {
            System.out.println("\n❌ Players disqualified:");
            for (Player p : disqualified) {
                System.out.println(" - " + p.getName());
            }
            players.removeAll(disqualified);
        }
        if (players.size() < 2) {
            System.out.println("Not enough players remaining! Game ends.");
            gameRunning = false;
            return true;
        }
        if (currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }
        return false;
    }

    /**
     * Displays current scores, using the database if available.
     */
    private void displayCurrentScores() {
        if (dbManager != null && sessionId > 0) {
            try {
                dbManager.displaySessionStandings(sessionId);
                menu.waitForUserInput("Press Enter to continue...");
                return;
            } catch (Exception e) {
                System.err.println("❌ Could not fetch scores from database: " + e.getMessage());
            }
        }
        System.out.println("\n--- Current Scores ---");
        for (Player player : players) {
            System.out.println(player.getName() + ": " + player.getTotalScore() + " points");
        }
        System.out.println("----------------------");
        menu.waitForUserInput("Press Enter to continue...");
    }
}

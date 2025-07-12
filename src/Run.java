import java.util.*;
import java.util.Scanner;

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
     * @param scanner The shared Scanner instance.
     * @param menu The shared Menu instance.
     */
    // (MODIFIED) the constructor now receives the scanner and menu object
    public Run(Initialization.GameSetup gameSetup, Scanner scanner, Menu menu) {
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
        // (MODIFIED)
        this.menu = menu; // (NEW) using the received menu object
        // (MODIFIED)
        // this.scanner = Main.getScanner(); // Use shared scanner from Main
        this.scanner = scanner; // (NEW) using the received scanner

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
                // [NEW] Check if the game has ended after the player's turn (e.g. by winning)
                if (!gameRunning) break;
                moveToNextPlayer();
            } catch (Exception e) {
                System.err.println("Error during turn: " + e.getMessage());
                System.out.println("Ending game...");
                e.printStackTrace();
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

        // [NEW / MODIFIED ] If the current player is a bot, its logic is executed.
        // If it is a human player, the UI interaction is triggered.
//        if (shouldDrawCards(currentPlayer)) return;
//
//        if (!currentPlayer.hasPlayableCard(topCard)) {
//            handleNoPlayableCards(currentPlayer);
//            return;
//        int cardChoice = currentPlayer.getCardChoice(topCard);
//
//        if (cardChoice == -1) {
//            handleDrawCard(currentPlayer);
//        } else {
//            handlePlayCard(currentPlayer, cardChoice);
//        }
//    }

        if (currentPlayer instanceof BotPlayer) {
            handleBotTurn((BotPlayer) currentPlayer, topCard);
        } else {
            handleHumanTurn(currentPlayer, topCard);
        }
    }

    // [NEW] Method for handling a human player's turn
    private void handleHumanTurn(Player player, Card topCard) {
        // [REMOVED] shouldDrawCards was unused
        // if (shouldDrawCards(player)) return; // This method currently always returns false

        // Ask the player whether they want to call UNO (directly)
        // This can also be done in the main menu under Option 1: Call UNO.
        // Here we directly ask the player if they want to call UNO when they have only 2 cards
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

            // [NEW] Robust input handling of player selection
            String inputLine = scanner.nextLine().trim();
            int cardChoice;
            try {
                cardChoice = Integer.parseInt(inputLine);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue; // Repeat the loop
            }

            if (cardChoice == -1) { // Player wants to access GAME MENU
                int menuChoice = menu.showGameMenu();
                switch (menuChoice) {
                    case 1: // Call UNO
                        player.callUno();
                        System.out.println(player.getName() + " calls: UNO!");
                        break;
                    case 2: // Challenge +4
                        // Currently, the challenge logic is only invoked when processing a +4.
                        System.out.println("You can only challenge a Wild Draw Four after it has been played.");
                        break;
                    case 3: // Show Scores
                        displayCurrentScores(); // [NEW] Helper method
                        break;
                    case 4: // View Rules
                        menu.displayRules();
                        break;
                    case 5: // Return to Game - nothing to do, loop continues
                        break;
                    case 6: // Quit Game
                        if (menu.confirmQuit()) {
                            gameRunning = false;
                            return; // End the game
                        }
                        break;
                    default:
                        System.out.println("Invalid menu choice.");
                        break;
                }
                // After the menu, prompt again for card selection
                continue;
            } else if (cardChoice == 0) { // Player wants to draw
                handleDrawCard(player);
                cardPlayedOrDrawn = true; // Turn ends after drawing and possibly playing
            } else if (cardChoice >= 1 && cardChoice <= player.getHandSize()) {
                int actualIndex = cardChoice - 1; // Zero-based index
                Card chosenCard = player.getHand().get(actualIndex);

                if (referee.validateCardPlay(chosenCard, topCard, player)) {
                    // Remove card from player
                    player.playCard(actualIndex);
                    playCard(player, chosenCard); // Play the card and process effects
                    cardPlayedOrDrawn = true;
                } else {
                    System.out.println("You cannot play " + chosenCard + " on " + topCard + ". Please choose another card or draw.");
                }
            } else {
                System.out.println("Invalid card number. Please try again.");
            }
        }
    }

    // [NEW] Method for handling a bot player's turn
    private void handleBotTurn(BotPlayer bot, Card topCard) {
        int cardChoiceIndex = bot.getCardChoice(topCard); // Bot decides on card or draw

        if (cardChoiceIndex == -1) { // Bot wants to draw
            handleDrawCard(bot);
        } else { // Bot wants to play
            Card chosenCard = bot.getHand().get(cardChoiceIndex);
            // Validation should already happen inside the bot, but double-check here for safety
            if (referee.validateCardPlay(chosenCard, topCard, bot)) {
                bot.playCard(cardChoiceIndex);
                playCard(bot, chosenCard);
            } else {
                // This should rarely happen with a well-programmed bot
                System.out.println("Bot " + bot.getName() + " tried to play an invalid card. Drawing instead.");
                handleDrawCard(bot);
            }
        }
    }

// (REMOVED) was not used and is not of relevance anymore
//    private boolean shouldDrawCards(Player player) {
//        return false;
//    }


    // (REMOVED) not needed anymore . the logic if a player need to dray a card or not is
    // handled in handleHumanTurn & handleBotTurn
//    private void handleNoPlayableCards(Player player) {
//        System.out.println(player.getName() + " has no playable cards and must draw.");
//        Card drawnCard = deck.drawCard();
//
//        if (drawnCard != null) {
//            player.addCard(drawnCard);
//            System.out.println(player.getName() + " draws: " + drawnCard);
//
//            Card topCard = deck.getTopCard();
//            if (drawnCard.canPlayOn(topCard)) {
//                System.out.println("The drawn card can be played!");
//
//                // (MODIFIED):
//                // Better: BotPlayer should make the decision and play the card directly
//                // For bots: If the drawn card is playable, the bot should play it immediately.
//                // A second "choice" is redundant, since the bot already chose to draw because there were no other options.
//                if (player instanceof BotPlayer) {
//                    Card playedBotCard = player.playCard(player.getHand().indexOf(drawnCard); // the drwan card will be played
//                    //playDrawnCard(player, drawnCard);
//                    playCard(player, playedBotCard);
//
//                    // now for the HUMAN player
//                } else {
//                    System.out.print("Do you want to play the drawn card (" + drawnCard + ")? (y/n): ");
//                    // (MODIFIED) Uses now the menus getYESNOInput
//                    //String choice = scanner.nextLine().toLowerCase().trim();
//                    // if (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("yes")) {
//                    //    playDrawnCard(player, drawnCard);
//                    if (menu.getYesNoInput()) {
//                        Card playedHumanCard = player.playCard(player.getHand().indexOf(drawnCard)); // playing drawn card
//                        playCard(player, playedHumanCard);
//                    }
//                }
//            }
//        } else {
//            System.out.println("No more cards in the draw pile!");
//        }
//    }


    private void handleDrawCard(Player player) {
        Card drawnCard = deck.drawCard();
        if (drawnCard != null) {
            player.addCard(drawnCard);
            // (MODIFIED)
            System.out.println(player.getName() + " draws a card: " + drawnCard);

            Card topCard = deck.getTopCard();
            if (drawnCard.canPlayOn(topCard)) {
                // [OPTIMIZATION] If the drawn card is playable, the bot plays it immediately.
                // Since the bot already chose to draw, it's reasonable to assume it wants to play the card if possible.
                if (player instanceof BotPlayer) {
//                    if (Math.random() < 0.7) {
//                        playDrawnCard(player, drawnCard);
                    Card playedBotCard = player.playCard(player.getHand().indexOf(drawnCard));
                    playCard(player, playedBotCard);
            } else { // now for the human
                System.out.println("The drawn card (" + drawnCard + ") can be played!");
                System.out.print("Do you want to play it? (y/n): ");
                // (MODIFIED) Using Menus getYesNoInput
                // String choice = scanner.nextLine().toLowerCase().trim();
//                    if (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("yes")) {
//                        playDrawnCard(player, drawnCard);
                if (menu.getYesNoInput()) {
                    Card playedHumanCard = player.playCard(player.getHand().indexOf(drawnCard));
                    playCard(player, playedHumanCard);
                } else {
                    // [NEW] In case the player doesnt want to play the drawn card
                    System.out.println(player.getName() + " decides not to play the drawn card.");
                }
            }
        } else {
                // (NEW) if the drawn card is not playabe
                System.out.println(player.getName() + " cannot play the drawn card.");
            }
        } else {
            System.out.println("No cards available to draw!");
        }
    }

        // [REMOVED] The playDrawnCard() method has been merged into handleNoPlayableCards and handleDrawCard
//    private void playDrawnCard(Player player, Card drawnCard) {
//        List<Card> hand = player.getHand();
//        for (int i = 0; i < hand.size(); i++) {
//            if (hand.get(i).equals(drawnCard)) {
//                Card removedCard = player.playCard(i);
//                playCard(player, removedCard);
//                break;
//            }
//        }
//    }


    // [REMOVED] handlePlayCard() is now integrated into handleHumanTurn and handleBotTurn
//    private void handlePlayCard(Player player, int cardChoice) {
//        if (cardChoice < 0 || cardChoice >= player.getHandSize()) {
//            System.out.println("Invalid card choice!");
//            return;
//        }
//
//        List<Card> hand = player.getHand();
//        Card chosenCard = hand.get(cardChoice);
//        Card topCard = deck.getTopCard();
//
//        if (!referee.validateCardPlay(chosenCard, topCard, player)) {
//            return;
//        }
//
//        Card playedCard = player.playCard(cardChoice);
//        if (playedCard != null) {
//            playCard(player, playedCard);
//        }
//    }

    // [CHANGED] playCard is now primarily used for handling effects and validations
    // after a card has actually been played (i.e., removed from the hand).
    private void playCard(Player player, Card card) {
        deck.playCard(card); //put card on discard pile
        System.out.println(player.getName() + " plays: " + card);

        // (REMOVED) The "End trun early" option and the "In Game Menu" during the turn
        // are better integrated in handleHumandTurn(), where the actual human Player interacts.
//        // Prompt to end turn early
//        System.out.println("Do you want to end your turn? (y/n)");
//        scanner.nextLine(); // Consume newline
//        String input = scanner.nextLine().trim().toLowerCase();
//
//        if (input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no")) {
//            int menuChoice = menu.showGameMenu();
//            switch (menuChoice) {
//                case 1:
//                    player.callUno();
//                    break;
//                // case 2: referee.handleWildDrawFourChallenge(player, player);
//            }
//        }

        // UNO check (happens after playing a card)
        // (MODIFIED) first in case of BotPlayer then Human Player logic
//        if (player.getHandSize() == 1) {
//            if ((player instanceof BotPlayer)) {
//                System.out.print("Do you want to call UNO? (y/n): ");
//                String unoChoice = scanner.nextLine().trim().toLowerCase();
//                if (unoChoice.equalsIgnoreCase("y") || unoChoice.equalsIgnoreCase("yes")) {
//                    player.callUno();
//                } else {
//                    referee.checkUnoViolation(player);
//                }
//            } else {
//                player.callUno();
//            }
//        }
        // UNO check
        if (player.getHandSize() == 1) {
            if (player instanceof BotPlayer) {
                ((BotPlayer)player).callUno(); // Bot calls UNO based on its internal logic
                if (player.hasSaidUno()) { // Confirm if bot successfully called UNO
                    System.out.println("🤖 " + player.getName() + " calls: UNO!");
                }
            } else { // Human Player
                // Ask if UNO shall be called
                // It's also available in the Menu
                // And now here directly:
                System.out.print(player.getName() + ", you have 1 card left! Call UNO? (y/n): ");
                // [MODIFIED] Uses menus getYesNoInout
                if (menu.getYesNoInput()) {
                    player.callUno();
                    System.out.println(player.getName() + " calls: UNO!");
                } else {
                    // if the Human forgets or says "no"
                    if (referee.checkUnoViolation(player)) {
                        System.out.println(player.getName() + " forgot to call UNO and draws 2 penalty cards!");
                        deck.drawCards(player, 2);
                        player.addPenalty();
                    }
                }
            }
        } else if (player.getHandSize() == 0) {
            // if the hand is empty - the player won, no UNO-Message is necessary
        } else {
            // if the player has more than 1 card left, the saidUNO-flag will be resetted
            // if it was before set to true (e.g. because of a UNO-message)
            player.setSaidUno(false);
        }


        // Special card logic
        if (SpecialCards.isSpecialCard(card)) {
            handleSpecialCardEffects(player, card);
        }

        // Win check (after all card-effects)
        if (player.getHandSize() == 0) {
            handleRoundWin(player);
            // (NEW) set gameRunning to false, if the game has been won
            // that ends the outer loop in runGame()
            gameRunning = false;
        }

        // (REMOVED)
//        // Prompt to end turn again (if not already)
//        if (!(player instanceof BotPlayer)) {
//            System.out.print("End turn? (y/n): ");
//            String endTurn = scanner.nextLine().trim().toLowerCase();
//            if (endTurn.equalsIgnoreCase("n") || endTurn.equalsIgnoreCase("no")) {
//                menu.showGameMenu();
//            }
//        }
    }


    private void handleSpecialCardEffects(Player player, Card card) {
        int nextPlayerIndex = getNextPlayerIndex(); // its a temp index
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
                // (MODIFIED) Bots choose immediatle a colour in their chooseColor()
                // Humans get asked in thei menu.chooseColor() since the menu has the scanner
                CardColor chosenColor;
                if (player instanceof BotPlayer) {
                    chosenColor = ((BotPlayer) player).chooseColor();
                } else {
                    chosenColor = menu.chooseColor(scanner); // (NEW) call menu.chooseColor and commit / transfer scanner
                }
                SpecialCards.processWild(player, card, chosenColor); // (NEW) commit ChosenColor as argument
                break;

                // (MODIFIED)
                // aks next Player if they want to challenge
            case WILD_DRAW_FOUR:
//                if (!(nextPlayer instanceof BotPlayer) && menu.askForChallenge(nextPlayer, player)) {
//                    referee.handleWildDrawFourChallenge(nextPlayer, player, deck.getTopCard());
//                } else {
//                    SpecialCards.processWildDrawFour(player, nextPlayer, card, deck);
//                    skipNextPlayer();
                // First, determine the chosen color (similar to the WILD case)
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
        gameRunning = false; //Ends game
    }

    private void prepareNewRound() {
        for (Player player : players) {
            player.clearHand();
            player.resetPenalties();
        }

        // create new Deck and initialize
        deck = new Deck();
        for (int i = 0; i < 7; i++) {
            for (Player player : players) {
                Card card = deck.drawCard();
                if (card != null) {
                    player.addCard(card);
                }
            }
        }

        deck.setupInitialCard(); // put first card

        direction = 1; // reset direction
        currentPlayerIndex = 0; // reset starting player
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
        // (MODIFIED)
        if (!disqualified.isEmpty()) {
            System.out.println("\n❌ Players disqualified:");
            for (Player p : disqualified) {
                System.out.println("   - " + p.getName());
            }
            players.removeAll(disqualified); // removes disqualified players from index
        }

        if (players.size() < 2) {
            System.out.println("Not enough players remaining! Game ends.");
            // (MODIFIED)
            gameRunning = false; // Game ends
            return true;
        }

        // Ensure currentPlayerIndex is still valid after removals
        if (currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }

        return false;
    }

    // [NEW] Method to show current Scores
    private void displayCurrentScores() {
        System.out.println("\n--- Current Scores ---");
        for (Player player : players) {
            System.out.println(player.getName() + ": " + player.getTotalScore() + " points");
        }
        System.out.println("----------------------");
        menu.waitForUserInput("Press Enter to continue...");
    }
}
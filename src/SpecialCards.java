import java.util.Scanner;

/**
 * Handles the logic for special action cards
 * Contains methods for each special card type's behavior
 */
public class SpecialCards {

    /**
     * Processes Draw Two card effects
     * @param targetPlayer The player who must draw cards
     * @param deck The game deck
     */
    public static void processDrawTwo(Player targetPlayer, Deck deck) {
        System.out.println(targetPlayer.getName() + " must draw 2 cards!");

        for (int i = 0; i < 2; i++) {
            Card drawnCard = deck.drawCard();
            if (drawnCard != null) {
                targetPlayer.addCard(drawnCard);
                System.out.println(targetPlayer.getName() + " draws: " + drawnCard);
            }
        }

        System.out.println(targetPlayer.getName() + " is skipped this round.");
    }

    /**
     * Processes Reverse card effects
     * @param currentDirection Current game direction (1 or -1)
     * @return New direction
     */
    public static int processReverse(int currentDirection) {
        int newDirection = currentDirection * -1;
        System.out.println("🔄 Play direction is reversed!");

        if (newDirection == 1) {
            System.out.println("The game now proceeds clockwise.");
        } else {
            System.out.println("The game now proceeds counterclockwise.");
        }

        return newDirection;
    }

    /**
     * Processes Skip card effects
     * @param skippedPlayer The player who must skip their turn
     */
    public static void processSkip(Player skippedPlayer) {
        System.out.println("⏭️ " + skippedPlayer.getName() + " must skip their turn!!");
    }

    /**
     * Processes Wild card effects
     * @param player The player who played the wild card
     * @param wildCard The wild card (to set its color)
     * @param chosenColor The color chosen by the player
     */
    public static void processWild(Player player, Card wildCard, CardColor chosenColor) {
        // Die Farbe wurde bereits in Run ermittelt und übergeben.
        wildCard.setColor(chosenColor);
        System.out.println("🎨 " + player.getName() + " chooses " + chosenColor + " as new color!");
    }

    /**
     * Processes Wild Draw Four card effects
     * @param player The player who played the card
     * @param targetPlayer The next player who must draw
     * @param wildDrawFour The card (to set its color)
     * @param deck The game deck
     * @param chosenColor The color chosen by the player
     */
    public static void processWildDrawFour(Player player, Player targetPlayer, Card wildDrawFour, Deck deck, CardColor chosenColor) {
        // (REMOVED) First, let player choose color
        // CardColor chosenColor = player.chooseColor();
        wildDrawFour.setColor(chosenColor);
        System.out.println("🎨 " + player.getName() + " chooses " + chosenColor + " as new color!");

        // Then make target player draw 4 cards
        System.out.println("📚 " + targetPlayer.getName() + " must draw 4 cards!");

        for (int i = 0; i < 4; i++) {
            Card drawnCard = deck.drawCard();
            if (drawnCard != null) {
                targetPlayer.addCard(drawnCard);
                System.out.println(targetPlayer.getName() + " draws: " + drawnCard);
            }
        }

        System.out.println(targetPlayer.getName() + " skipps this round.");
    }

    /**
     * Checks if a card is a special action card
     * @param card The card to check
     * @return true if it's a special card
     */
    public static boolean isSpecialCard(Card card) {
        CardType type = card.getType();
        return type == CardType.DRAW_TWO ||
                type == CardType.REVERSE ||
                type == CardType.SKIP ||
                type == CardType.WILD ||
                type == CardType.WILD_DRAW_FOUR;
    }

    /**
     * Handles special card played at game start
     * @param firstCard The first card revealed
     * @param startingPlayerIndex The player who would go first
     * @param players All players in the game
     * @param deck The game deck
     * @param menu The shared Menu instance (for human color choice) // NEUER PARAMETER
     * @param scanner The shared Scanner instance (for human color choice) // NEUER PARAMETER
     * @return Adjusted starting direction and player info
     */
    public static GameStartInfo handleStartingSpecialCard(Card firstCard, int startingPlayerIndex,
                                                          Player[] players, Deck deck, Menu menu, Scanner scanner) {
        GameStartInfo info = new GameStartInfo();
        info.direction = 1; // Default direction
        info.currentPlayerIndex = startingPlayerIndex;

        switch (firstCard.getType()) {
            case DRAW_TWO:
                System.out.println("⚠️ Starting card is a Draw Two! The first player must draw 2 cards!");
                // (MODIFIED) processDrawTwo(players[startingPlayerIndex], deck);
                deck.drawCards(players[startingPlayerIndex], 2);
                info.skipFirstPlayer = true;
                break;

            case REVERSE:
                info.direction = processReverse(1);
                break;

            case SKIP:
                System.out.println("⚠️ Starting card is a Skip! The first player is skipped!");
                processSkip(players[startingPlayerIndex]);
                info.skipFirstPlayer = true;
                break;

            case WILD:
//                // First player chooses color
//                CardColor color = players[startingPlayerIndex].chooseColor();
//                firstCard.setColor(color);
//                System.out.println("🎨 " + players[startingPlayerIndex].getName() +
//                        " chooses " + color + " as the starting color!");
                // GEÄNDERT: Ermittelt Farbe abhängig vom Spielertyp
                Player affectedPlayer = players[startingPlayerIndex];
                CardColor chosenColor;
                if (affectedPlayer instanceof BotPlayer) {
                    chosenColor = ((BotPlayer) affectedPlayer).chooseColor();
                } else {
                    chosenColor = menu.chooseColor(scanner);
                }
                firstCard.setColor(chosenColor);
                System.out.println("🎨 " + affectedPlayer.getName() +
                        " chooses " + chosenColor + " as the starting color!");
                break;

            case WILD_DRAW_FOUR:
                // This should not happen as we prevent it in Deck.setupInitialCard()
                System.out.println("❌ Error: A Wild Draw Four card is not allowed as a starting card!\"");
                break;
        }

        return info;
    }

    /**
     * Helper class to return multiple values from handleStartingSpecialCard
     */
    public static class GameStartInfo {
        public int direction = 1;
        public int currentPlayerIndex;
        public boolean skipFirstPlayer = false;
    }
}

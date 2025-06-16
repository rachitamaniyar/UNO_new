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
        System.out.println(targetPlayer.getName() + " muss 2 Karten ziehen!");

        for (int i = 0; i < 2; i++) {
            Card drawnCard = deck.drawCard();
            if (drawnCard != null) {
                targetPlayer.addCard(drawnCard);
                System.out.println(targetPlayer.getName() + " zieht: " + drawnCard);
            }
        }

        System.out.println(targetPlayer.getName() + " setzt diese Runde aus.");
    }

    /**
     * Processes Reverse card effects
     * @param currentDirection Current game direction (1 or -1)
     * @return New direction
     */
    public static int processReverse(int currentDirection) {
        int newDirection = currentDirection * -1;
        System.out.println("🔄 Spielrichtung wird umgekehrt!");

        if (newDirection == 1) {
            System.out.println("Spiel läuft jetzt im Uhrzeigersinn.");
        } else {
            System.out.println("Spiel läuft jetzt gegen den Uhrzeigersinn.");
        }

        return newDirection;
    }

    /**
     * Processes Skip card effects
     * @param skippedPlayer The player who must skip their turn
     */
    public static void processSkip(Player skippedPlayer) {
        System.out.println("⏭️ " + skippedPlayer.getName() + " muss aussetzen!");
    }

    /**
     * Processes Wild card effects
     * @param player The player who played the wild card
     * @param wildCard The wild card (to set its color)
     */
    public static void processWild(Player player, Card wildCard) {
        CardColor chosenColor = player.chooseColor();
        wildCard.setColor(chosenColor);
        System.out.println("🎨 " + player.getName() + " wählt " + chosenColor + " als neue Farbe!");
    }

    /**
     * Processes Wild Draw Four card effects
     * @param player The player who played the card
     * @param targetPlayer The next player who must draw
     * @param wildDrawFour The card (to set its color)
     * @param deck The game deck
     */
    public static void processWildDrawFour(Player player, Player targetPlayer, Card wildDrawFour, Deck deck) {
        // First, let player choose color
        CardColor chosenColor = player.chooseColor();
        wildDrawFour.setColor(chosenColor);
        System.out.println("🎨 " + player.getName() + " wählt " + chosenColor + " als neue Farbe!");

        // Then make target player draw 4 cards
        System.out.println("📚 " + targetPlayer.getName() + " muss 4 Karten ziehen!");

        for (int i = 0; i < 4; i++) {
            Card drawnCard = deck.drawCard();
            if (drawnCard != null) {
                targetPlayer.addCard(drawnCard);
                System.out.println(targetPlayer.getName() + " zieht: " + drawnCard);
            }
        }

        System.out.println(targetPlayer.getName() + " setzt diese Runde aus.");
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
     * @return Adjusted starting direction and player info
     */
    public static GameStartInfo handleStartingSpecialCard(Card firstCard, int startingPlayerIndex,
                                                          Player[] players, Deck deck) {
        GameStartInfo info = new GameStartInfo();
        info.direction = 1; // Default direction
        info.currentPlayerIndex = startingPlayerIndex;

        switch (firstCard.getType()) {
            case DRAW_TWO:
                System.out.println("⚠️ Startkarte ist Zieh-2! Der erste Spieler muss 2 Karten ziehen!");
                processDrawTwo(players[startingPlayerIndex], deck);
                info.skipFirstPlayer = true;
                break;

            case REVERSE:
                info.direction = processReverse(1);
                break;

            case SKIP:
                System.out.println("⚠️ Startkarte ist Aussetzen! Der erste Spieler wird übersprungen!");
                processSkip(players[startingPlayerIndex]);
                info.skipFirstPlayer = true;
                break;

            case WILD:
                // First player chooses color
                CardColor color = players[startingPlayerIndex].chooseColor();
                firstCard.setColor(color);
                System.out.println("🎨 " + players[startingPlayerIndex].getName() +
                        " wählt " + color + " als Startfarbe!");
                break;

            case WILD_DRAW_FOUR:
                // This should not happen as we prevent it in Deck.setupInitialCard()
                System.out.println("❌ Fehler: Zieh-4-Karte als Startkarte ist nicht erlaubt!");
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

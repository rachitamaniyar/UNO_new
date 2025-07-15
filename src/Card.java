
public class Card {
    /**
     * Represents a single UNO card with color, type, and point value
     * This class encapsulates all card-related data and behavior
     */
        private CardColor color;
        private final CardType type;
        private final int points; // Punktwert für Punkteberechnung

        /**
         * Constructor for creating a card
         * @param color The color of the card
         * @param type The type/value of the card
         */
        public Card(CardColor color, CardType type) {
            this.color = color;
            this.type = type;
            this.points = calculatePoints(); // Automatically calculate points based on the card type
        }


    /**
         * Calculates the point value of this card based on UNO rules
         * Number cards = face value, Special colored cards = 20 points, Wild cards = 50 points
         * @return The point value of this card
         */
        private int calculatePoints() {
            // Switch statement to determine points based on card type
            switch (type) {
                case ZERO: return 0;
                case ONE: return 1;
                case TWO: return 2;
                case THREE: return 3;
                case FOUR: return 4;
                case FIVE: return 5;
                case SIX: return 6;
                case SEVEN: return 7;
                case EIGHT: return 8;
                case NINE: return 9;
                case DRAW_TWO:
                case REVERSE:
                case SKIP:
                    return 20; // Colored Action Cards = 20 Punkte
                case WILD:
                case WILD_DRAW_FOUR:
                    return 50; // Black Action Cards = 50 Punkte
                default:
                    return 0;
            }
        }

        /**
         * Checks if this card can be played on top of another card
         * @param topCard The card currently on top of the discard pile
         * @return true if this card can be played, false otherwise
         */
        public boolean canPlayOn(Card topCard) {
            // Wild cards can always be played
            if (this.type == CardType.WILD || this.type == CardType.WILD_DRAW_FOUR) {
                return true;
            }

            // Same color or same type can be played
            return this.color == topCard.color || this.type == topCard.type;
        }

        /**
         * Checks if this card is a special action card
         * @return true if it's an action card, false if it's a number card
         */
        public boolean isActionCard() {
            // Using ordinal() method - returns the position of enum constant (0-based index)
            // Number cards are positions 0-9, so anything above 9 is an action card
            return type.ordinal() > 9;
        }

        /**
         * Returns a string representation of the card for display
         * @return Formatted string showing the card
         */
        @Override
        public String toString() {
            if (color == CardColor.BLACK) {
                // For black cards, only show the type
                return type.toString().replace("_", " ");
            } else {
                // For colored cards, show both color and type
                return color + " " + type.toString().replace("_", " ");
            }
        }

        // Getter methods (accessor methods) to access private fields
        public CardColor getColor() { return color; }
        public CardType getType() { return type; }
        public int getPoints() { return points; }

        // Setter for color (needed for wild cards when player chooses color)
        public void setColor(CardColor color) { this.color = color; }
    }

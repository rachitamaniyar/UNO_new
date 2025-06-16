/**
 * Enum representing all possible card types in UNO
 * This includes number cards (0-9) and all special action cards
 */
public enum CardType {
    // Zahlenkarten (Number cards 0-9)
    ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE,

    // Farbige Aktionskarten (Colored action cards)
    DRAW_TWO,      // Zieh 2 Karte
    REVERSE,       // Retour/Richtungswechsel Karte
    SKIP,          // Aussetzen Karte

    // Schwarze Spezialkarten (Black special cards)
    WILD,          // Farbauswahlkarte
    WILD_DRAW_FOUR // Zieh Vier Farbauswahlkarte
}

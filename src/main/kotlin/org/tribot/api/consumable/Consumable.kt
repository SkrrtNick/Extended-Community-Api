package org.tribot.api.consumable

/**
 * Represents a consumable item (food, potion, brew, etc.) with its effects.
 *
 * @param itemId the in-game item ID
 * @param name display name of the item
 * @param effects list of effects applied when consumed
 * @param type classification of the consumable
 */
data class Consumable(
    val itemId: Int,
    val name: String,
    val effects: List<Effect>,
    val type: ConsumableType
)

/**
 * Classification of consumable types.
 */
enum class ConsumableType {
    /** Standard food items consumed with "Eat" */
    FOOD,
    /** Potions consumed with "Drink", typically have dose variants */
    POTION,
    /** Brews that boost/drain multiple stats (e.g. Saradomin brew) */
    BREW,
    /** Pie foods that have 2 halves */
    PIE,
    /** Combo foods that can be eaten on the same tick as regular food (e.g. Karambwan) */
    COMBO,
    /** Special consumables with unique mechanics */
    SPECIAL
}

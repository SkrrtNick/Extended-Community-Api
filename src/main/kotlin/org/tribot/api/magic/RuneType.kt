package org.tribot.api.magic

/**
 * All rune types in Old School RuneScape with their item IDs.
 *
 * Item IDs validated against the OSRS Wiki:
 * https://oldschool.runescape.wiki/w/Runes
 */
enum class RuneType(val itemId: Int) {
    AIR(556),
    WATER(555),
    EARTH(557),
    FIRE(554),
    MIND(558),
    BODY(559),
    COSMIC(564),
    CHAOS(562),
    NATURE(561),
    LAW(563),
    DEATH(560),
    ASTRAL(9075),
    BLOOD(565),
    SOUL(566),
    WRATH(21880);

    companion object {
        /**
         * Finds a [RuneType] by its item ID, or null if no match.
         */
        fun fromItemId(itemId: Int): RuneType? = entries.find { it.itemId == itemId }
    }
}

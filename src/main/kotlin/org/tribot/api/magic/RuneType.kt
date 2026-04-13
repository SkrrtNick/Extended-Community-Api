package org.tribot.api.magic

import org.tribot.automation.script.ScriptContext

/**
 * All rune types in Old School RuneScape with their item IDs and combo rune substitutes.
 *
 * Item IDs validated against the OSRS Wiki and local Inferno project data.
 *
 * @property itemId The primary rune item ID
 * @property comboRuneIds Item IDs of combination runes that can substitute for this rune
 */
enum class RuneType(
    val itemId: Int,
    val comboRuneIds: List<Int> = emptyList()
) {
    // Elemental runes (with combo rune substitutes)
    AIR(556, listOf(4695, 4696, 4697)),       // mist, dust, smoke
    WATER(555, listOf(4695, 4698, 4694)),      // mist, mud, steam
    EARTH(557, listOf(4696, 4698, 4699)),      // dust, mud, lava
    FIRE(554, listOf(4697, 4694, 4699, 28929)), // smoke, steam, lava, sunfire

    // Catalytic runes
    MIND(558),
    BODY(559),
    COSMIC(564, listOf(30843)),  // aether rune
    CHAOS(562),
    NATURE(561),
    LAW(563),
    DEATH(560),
    ASTRAL(9075),
    BLOOD(565),
    SOUL(566, listOf(30843)),    // aether rune
    WRATH(21880);

    /**
     * All item IDs that count as this rune type (primary + combo runes).
     */
    val allItemIds: IntArray by lazy {
        (listOf(itemId) + comboRuneIds).toIntArray()
    }

    /**
     * Returns the total count of this rune type available to the player,
     * considering combo runes in inventory and unlimited runes from equipped staffs.
     *
     * If a staff provides this rune type, returns [Int.MAX_VALUE].
     */
    fun getAvailableCount(ctx: ScriptContext): Int {
        if (this in Staff.getProvidedRunes(ctx)) return Int.MAX_VALUE
        return allItemIds.sumOf { ctx.inventory.getCount(it) }
    }

    companion object {
        fun fromItemId(itemId: Int): RuneType? = entries.find { it.itemId == itemId }
    }
}

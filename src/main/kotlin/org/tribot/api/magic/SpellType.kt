package org.tribot.api.magic

/**
 * Broad categories of spells.
 */
enum class SpellType {
    /** Offensive damage-dealing spells. */
    COMBAT,
    /** Teleportation spells. */
    TELEPORT,
    /** General utility spells (alchemy, superheat, orb charging, etc.). */
    UTILITY,
    /** Jewellery and crossbow bolt enchantment spells. */
    ENCHANT,
    /** Alchemy spells (Low/High Level Alchemy, Bones to Bananas/Peaches). */
    ALCHEMY,
    /** Curse / debuff spells (Confuse, Weaken, Vulnerability, binds, etc.). */
    CURSE
}

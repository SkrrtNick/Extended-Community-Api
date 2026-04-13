package org.tribot.api.magic

/**
 * A single rune requirement for casting a spell.
 *
 * @property rune the type of rune required
 * @property amount the number of that rune consumed per cast
 */
data class RuneRequirement(val rune: RuneType, val amount: Int)

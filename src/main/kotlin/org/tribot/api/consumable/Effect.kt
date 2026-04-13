package org.tribot.api.consumable

import net.runelite.api.Skill

/**
 * Sealed hierarchy representing possible effects a consumable can have.
 */
sealed interface Effect {
    val description: String
}

/**
 * Heals a flat amount of HP.
 * @param amount HP healed
 * @param overheal true if the food can boost above max HP (e.g. Anglerfish)
 */
data class HealEffect(
    val amount: Int,
    val overheal: Boolean = false
) : Effect {
    override val description: String
        get() = "${if (overheal) "Boost" else "Heal"} $amount HP"
}

/**
 * Heals a percentage of max HP plus a flat bonus.
 * Used by foods like Saradomin brew whose healing scales with HP level.
 * @param percent multiplier applied to max HP (e.g. 0.15 = 15%)
 * @param flatBonus added after the percentage calculation
 * @param overheal true if healing can exceed max HP
 */
data class PercentHealEffect(
    val percent: Double,
    val flatBonus: Int = 0,
    val overheal: Boolean = false
) : Effect {
    fun calculateHeal(maxHp: Int): Int = (maxHp * percent).toInt() + flatBonus
    override val description: String
        get() = "Heal ${(percent * 100).toInt()}% + $flatBonus HP"
}

/**
 * Boosts a skill level above (or up to) its base.
 * @param skill the skill affected
 * @param percent multiplier applied to base level (e.g. 0.15 = 15%)
 * @param flatBonus added after the percentage calculation
 * @param exceedsBase whether the boost can push the level above base
 */
data class StatBoostEffect(
    val skill: Skill,
    val percent: Double = 0.0,
    val flatBonus: Int = 0,
    val exceedsBase: Boolean = true
) : Effect {
    fun calculateBoost(level: Int): Int = (level * percent).toInt() + flatBonus
    override val description: String
        get() = "${skill.getName()} +${(percent * 100).toInt()}% + $flatBonus"
}

/**
 * Drains a skill level.
 * @param skill the skill drained
 * @param percent multiplier applied to current level
 * @param flatDrain subtracted after the percentage calculation
 */
data class StatDrainEffect(
    val skill: Skill,
    val percent: Double = 0.0,
    val flatDrain: Int = 0
) : Effect {
    fun calculateDrain(level: Int): Int = (level * percent).toInt() + flatDrain
    override val description: String
        get() = "${skill.getName()} -${(percent * 100).toInt()}% - $flatDrain"
}

/**
 * Restores prayer points.
 * @param percent multiplier applied to base prayer level
 * @param flatBonus added after the percentage calculation
 */
data class PrayerRestoreEffect(
    val percent: Double,
    val flatBonus: Int
) : Effect {
    fun calculateRestore(prayerLevel: Int): Int = (prayerLevel * percent).toInt() + flatBonus
    override val description: String
        get() = "Prayer +${(percent * 100).toInt()}% + $flatBonus"
}

/**
 * Restores run energy.
 * @param amount run energy percentage points restored
 */
data class RunEnergyEffect(val amount: Int) : Effect {
    override val description: String
        get() = "Run energy +$amount"
}

/**
 * Heals based on a custom formula dependent on the player's HP level.
 * Used by Anglerfish whose healing scales with max HP in a piecewise fashion.
 * @param formula function that takes max HP and returns the heal amount
 * @param overheal whether healing can exceed max HP
 * @param formulaDescription human-readable description of the formula
 */
data class ScalingHealEffect(
    val formula: (Int) -> Int,
    val overheal: Boolean = false,
    val formulaDescription: String = "Scaling heal"
) : Effect {
    fun calculateHeal(maxHp: Int): Int = formula(maxHp)
    override val description: String get() = formulaDescription
}

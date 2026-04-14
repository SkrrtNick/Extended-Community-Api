package org.tribot.api.requirements

import org.tribot.api.ApiContext

/**
 * Requires the player's combat level to satisfy a comparison.
 */
class CombatLevelRequirement(
    val level: Int,
    val operation: Operation = Operation.GREATER_EQUAL
) : Requirement {

    override fun check(): Boolean {
        val actual = ApiContext.get().worldViews.getLocalPlayer()?.combatLevel ?: return false
        return operation.check(actual, level)
    }

    override val displayText: String get() = "Combat level ${operation.symbol} $level"
}

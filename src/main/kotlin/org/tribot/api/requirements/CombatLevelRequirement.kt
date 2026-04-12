package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext

/**
 * Requires the player's combat level to satisfy a comparison.
 */
class CombatLevelRequirement(
    val level: Int,
    val operation: Operation = Operation.GREATER_EQUAL
) : Requirement {

    override fun check(ctx: ScriptContext): Boolean {
        val actual = ctx.worldViews.getLocalPlayer()?.combatLevel ?: return false
        return operation.check(actual, level)
    }

    override val displayText: String get() = "Combat level ${operation.symbol} $level"
}

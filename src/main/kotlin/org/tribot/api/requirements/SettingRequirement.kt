package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext

/**
 * Requires a setting (varplayer/varp) to satisfy a comparison against an expected value.
 *
 * In OSRS, "settings" and "varps" (VarPlayers) are the same thing — full 32-bit
 * integers stored server-side per player.
 */
class SettingRequirement(
    val settingId: Int,
    val value: Int,
    val operation: Operation = Operation.EQUAL,
    private val name: String = "Setting $settingId ${operation.symbol} $value"
) : Requirement {

    override fun check(ctx: ScriptContext): Boolean {
        val actual = ctx.client.getVarpValue(settingId)
        return operation.check(actual, value)
    }

    override val displayText: String get() = name
}

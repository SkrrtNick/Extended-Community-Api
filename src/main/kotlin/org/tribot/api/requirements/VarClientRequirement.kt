package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext

/**
 * Requires a VarClient integer to satisfy a comparison against an expected value.
 *
 * VarClients are client-side only variables (not server-synced). Used for things
 * like chatbox transparency, camera settings, and other client preferences.
 */
class VarClientRequirement(
    val varcId: Int,
    val value: Int,
    val operation: Operation = Operation.EQUAL,
    private val name: String = "VarClient $varcId ${operation.symbol} $value"
) : Requirement {

    override fun check(ctx: ScriptContext): Boolean {
        val actual = ctx.client.getVarcIntValue(varcId)
        return operation.check(actual, value)
    }

    override val displayText: String get() = name
}

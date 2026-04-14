package org.tribot.api.requirements

import org.tribot.api.ApiContext

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

    override fun check(): Boolean {
        val actual = ApiContext.get().client.getVarcIntValue(varcId)
        return operation.check(actual, value)
    }

    override val displayText: String get() = name
}

package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext

/**
 * Requires a varplayer (varp) to satisfy a comparison against an expected value.
 */
class VarplayerRequirement(
    val varpId: Int,
    val value: Int,
    val operation: Operation = Operation.EQUAL,
    private val name: String = "Varp $varpId ${operation.symbol} $value"
) : Requirement {

    override fun check(ctx: ScriptContext): Boolean {
        val actual = ctx.client.getVarpValue(varpId)
        return operation.check(actual, value)
    }

    override val displayText: String get() = name
}

package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext

/**
 * Requires a varbit to satisfy a comparison against an expected value.
 */
class VarbitRequirement(
    val varbitId: Int,
    val value: Int,
    val operation: Operation = Operation.EQUAL,
    private val name: String = "Varbit $varbitId ${operation.symbol} $value"
) : Requirement {

    override fun check(ctx: ScriptContext): Boolean {
        val actual = ctx.client.getVarbitValue(varbitId)
        return operation.check(actual, value)
    }

    override val displayText: String get() = name
}

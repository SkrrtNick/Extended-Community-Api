package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext

/**
 * Requires a quest to have reached a particular progress value (tracked via varbit).
 */
class QuestRequirement(
    val questName: String,
    val varbitId: Int,
    val requiredValue: Int,
    val operation: Operation = Operation.GREATER_EQUAL
) : Requirement {

    override fun check(ctx: ScriptContext): Boolean {
        val actual = ctx.client.getVarbitValue(varbitId)
        return operation.check(actual, requiredValue)
    }

    override val displayText: String get() = questName
}

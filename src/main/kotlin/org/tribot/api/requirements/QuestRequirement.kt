package org.tribot.api.requirements

import org.tribot.api.ApiContext

/**
 * Requires a quest to have reached a particular progress value (tracked via varbit).
 */
class QuestRequirement(
    val questName: String,
    val varbitId: Int,
    val requiredValue: Int,
    val operation: Operation = Operation.GREATER_EQUAL
) : Requirement {

    override fun check(): Boolean {
        val actual = ApiContext.get().client.getVarbitValue(varbitId)
        return operation.check(actual, requiredValue)
    }

    override val displayText: String get() = questName
}

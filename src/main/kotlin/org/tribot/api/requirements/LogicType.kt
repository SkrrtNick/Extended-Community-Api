package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext

/**
 * Boolean logic modes for combining multiple [Requirement] instances.
 */
enum class LogicType {
    /** All requirements must pass. */
    AND,
    /** At least one requirement must pass. */
    OR,
    /** No requirements may pass. */
    NOR,
    /** At least one requirement must fail. */
    NAND,
    /** Exactly one requirement must pass. */
    XOR;

    fun test(requirements: List<Requirement>, ctx: ScriptContext): Boolean {
        val passed = requirements.count { it.check(ctx) }
        return when (this) {
            AND -> passed == requirements.size
            OR -> passed > 0
            NOR -> passed == 0
            NAND -> passed < requirements.size
            XOR -> passed == 1
        }
    }
}

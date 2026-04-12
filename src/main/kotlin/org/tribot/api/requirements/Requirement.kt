package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext

/**
 * A condition that can be checked against the current [ScriptContext].
 *
 * Requirements are composable — combine them with [CompositeRequirement]
 * or the helpers in [Requirements] to build complex preconditions.
 */
interface Requirement {
    /** Returns `true` when the requirement is currently satisfied. */
    fun check(ctx: ScriptContext): Boolean

    /** Human-readable description of what this requirement expects. */
    val displayText: String
}

/**
 * Comparison operations for integer values (varbits, varps, levels, etc.).
 */
enum class Operation(val symbol: String) {
    EQUAL("=="),
    NOT_EQUAL("!="),
    GREATER(">"),
    LESS("<"),
    GREATER_EQUAL(">="),
    LESS_EQUAL("<=");

    fun check(actual: Int, expected: Int): Boolean = when (this) {
        EQUAL -> actual == expected
        NOT_EQUAL -> actual != expected
        GREATER -> actual > expected
        LESS -> actual < expected
        GREATER_EQUAL -> actual >= expected
        LESS_EQUAL -> actual <= expected
    }
}

package org.tribot.api.requirements

/**
 * A [Requirement] that delegates to a list of child requirements,
 * combining their results with the given [LogicType].
 */
class CompositeRequirement(
    private val logicType: LogicType,
    private val requirements: List<Requirement>,
    private val name: String? = null
) : Requirement {

    constructor(logicType: LogicType, vararg requirements: Requirement) :
        this(logicType, requirements.toList())

    override fun check(): Boolean = logicType.test(requirements)

    override val displayText: String
        get() = name ?: requirements.joinToString(" ${logicType.name} ") { it.displayText }
}

/**
 * Convenience factory methods for common composite patterns.
 */
object Requirements {
    /** All requirements must be satisfied. */
    fun all(vararg requirements: Requirement) = CompositeRequirement(LogicType.AND, *requirements)

    /** At least one requirement must be satisfied. */
    fun any(vararg requirements: Requirement) = CompositeRequirement(LogicType.OR, *requirements)

    /** No requirements may be satisfied. */
    fun none(vararg requirements: Requirement) = CompositeRequirement(LogicType.NOR, *requirements)

    /** The single requirement must NOT be satisfied. */
    fun not(requirement: Requirement) = CompositeRequirement(LogicType.NOR, requirement)
}

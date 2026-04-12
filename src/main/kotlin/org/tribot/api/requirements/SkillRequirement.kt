package org.tribot.api.requirements

import net.runelite.api.Skill
import org.tribot.automation.script.ScriptContext

/**
 * Requires the player to have a minimum level in a [Skill].
 * When [boostable] is true the boosted (current) level is used instead of the base level.
 */
class SkillRequirement(
    val skill: Skill,
    val level: Int,
    val boostable: Boolean = false
) : Requirement {

    override fun check(ctx: ScriptContext): Boolean {
        val actual = if (boostable) ctx.skills.getBoostedLevel(skill) else ctx.skills.getLevel(skill)
        return actual >= level
    }

    override val displayText: String
        get() = "$level ${skill.getName()}${if (boostable) " (boostable)" else ""}"
}

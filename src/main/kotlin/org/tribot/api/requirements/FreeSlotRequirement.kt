package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext

/**
 * Requires a minimum number of free inventory slots.
 */
class FreeSlotRequirement(val slots: Int) : Requirement {

    override fun check(ctx: ScriptContext): Boolean = ctx.inventory.emptySlots() >= slots

    override val displayText: String get() = "$slots free inventory slots"
}

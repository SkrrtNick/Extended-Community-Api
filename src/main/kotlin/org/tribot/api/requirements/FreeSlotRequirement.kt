package org.tribot.api.requirements

import org.tribot.api.ApiContext

/**
 * Requires a minimum number of free inventory slots.
 */
class FreeSlotRequirement(val slots: Int) : Requirement {

    override fun check(): Boolean = ApiContext.get().inventory.emptySlots() >= slots

    override val displayText: String get() = "$slots free inventory slots"
}

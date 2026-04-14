package org.tribot.api.requirements

import org.tribot.api.ApiContext
import org.tribot.automation.script.core.tabs.EquipmentSlot

/**
 * Requires a specific item to be equipped in a given [EquipmentSlot].
 */
class EquipmentRequirement(
    val itemId: Int,
    val slot: EquipmentSlot,
    val alternateIds: List<Int> = emptyList(),
    val displayName: String = "Item #$itemId"
) : Requirement {

    private val allIds: Set<Int> get() = setOf(itemId) + alternateIds

    override fun check(): Boolean {
        val equipped = ApiContext.get().equipment.getItemIn(slot) ?: return false
        return equipped.id in allIds
    }

    override val displayText: String get() = "$displayName in ${slot.name}"
}

package org.tribot.api.loadout

import org.tribot.automation.script.core.tabs.EquipmentSlot

data class LoadoutItem(
    val itemId: Int,
    val quantity: Int = 1,
    val slot: EquipmentSlot? = null,
    val alternateIds: List<Int> = emptyList()
) {
    val allIds: Set<Int> get() = setOf(itemId) + alternateIds
}

data class Loadout(
    val inventory: List<LoadoutItem> = emptyList(),
    val equipment: List<LoadoutItem> = emptyList()
)

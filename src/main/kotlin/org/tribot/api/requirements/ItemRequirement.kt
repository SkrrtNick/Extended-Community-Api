package org.tribot.api.requirements

import org.tribot.api.ApiContext

/**
 * Requires a certain quantity of an item in inventory (or equipment if [equipped] is true).
 * Supports alternate item IDs (e.g. noted/un-noted variants).
 */
class ItemRequirement(
    val itemId: Int,
    val quantity: Int = 1,
    val equipped: Boolean = false,
    val alternateIds: List<Int> = emptyList(),
    val displayName: String = "Item #$itemId"
) : Requirement {

    private val allIds: Set<Int> get() = setOf(itemId) + alternateIds

    override fun check(): Boolean {
        val ctx = ApiContext.get()
        if (equipped) {
            return ctx.equipment.getItems().any { it.id in allIds && it.quantity >= quantity }
        }
        val total = ctx.inventory.getItems()
            .filter { it.id in allIds }
            .sumOf { it.quantity }
        return total >= quantity
    }

    override val displayText: String
        get() = "$quantity x $displayName${if (equipped) " (equipped)" else ""}"
}

package org.tribot.api.query

import org.tribot.automation.script.ScriptContext
import org.tribot.automation.script.core.tabs.EquipmentSlot
import org.tribot.automation.script.core.tabs.EquippedItem

/**
 * Fluent query builder for items in the player's equipment.
 */
class EquipmentQueryBuilder(private val ctx: ScriptContext) : QueryBuilder<EquippedItem, EquipmentQueryBuilder>() {

    fun names(vararg names: String): EquipmentQueryBuilder = filter { item ->
        val def = ctx.definitions.getItem(item.id)
        def != null && def.name in names
    }

    fun ids(vararg ids: Int): EquipmentQueryBuilder = filter { item ->
        item.id in ids.toSet()
    }

    fun slots(vararg slots: EquipmentSlot): EquipmentQueryBuilder = filter { item ->
        item.slot in slots.toSet()
    }

    fun actions(vararg actions: String): EquipmentQueryBuilder = filter { item ->
        val def = ctx.definitions.getItem(item.id)
        def != null && def.inventoryActions.filterNotNull().any { it in actions.toSet() }
    }

    fun minQuantity(min: Int): EquipmentQueryBuilder = filter { it.quantity >= min }

    fun maxQuantity(max: Int): EquipmentQueryBuilder = filter { it.quantity <= max }

    override fun fetchEntities(): List<EquippedItem> = ctx.equipment.getItems()

    override fun results(): QueryResults<EquippedItem> {
        val filtered = applyFilters(fetchEntities())
        return QueryResults(filtered)
    }
}

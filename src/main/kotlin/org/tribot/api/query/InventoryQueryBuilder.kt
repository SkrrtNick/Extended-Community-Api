package org.tribot.api.query

import org.tribot.automation.script.ScriptContext
import org.tribot.automation.script.core.tabs.InventoryItem

/**
 * Fluent query builder for items in the player's inventory.
 */
class InventoryQueryBuilder(private val ctx: ScriptContext) : QueryBuilder<InventoryItem, InventoryQueryBuilder>() {

    fun names(vararg names: String): InventoryQueryBuilder = filter { item ->
        val def = ctx.definitions.getItem(item.id)
        def != null && def.name in names
    }

    fun ids(vararg ids: Int): InventoryQueryBuilder = filter { item ->
        item.id in ids.toSet()
    }

    fun actions(vararg actions: String): InventoryQueryBuilder = filter { item ->
        val def = ctx.definitions.getItem(item.id)
        def != null && def.inventoryActions.filterNotNull().any { it in actions.toSet() }
    }

    fun minQuantity(min: Int): InventoryQueryBuilder = filter { item ->
        item.quantity >= min
    }

    fun maxQuantity(max: Int): InventoryQueryBuilder = filter { item ->
        item.quantity <= max
    }

    override fun results(): QueryResults<InventoryItem> {
        val filtered = applyFilters(fetchEntities())
        return QueryResults(filtered)
    }

    override fun fetchEntities(): List<InventoryItem> =
        ctx.inventory.getItems()
}

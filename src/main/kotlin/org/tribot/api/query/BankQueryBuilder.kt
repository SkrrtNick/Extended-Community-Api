package org.tribot.api.query

import org.tribot.api.ApiContext
import org.tribot.automation.script.core.widgets.BankItem

/**
 * Fluent query builder for items in the player's bank.
 */
class BankQueryBuilder : QueryBuilder<BankItem, BankQueryBuilder>() {

    fun names(vararg names: String): BankQueryBuilder = filter { item ->
        val def = ApiContext.get().definitions.getItem(item.id)
        def != null && def.name in names
    }

    fun ids(vararg ids: Int): BankQueryBuilder = filter { item ->
        item.id in ids.toSet()
    }

    fun actions(vararg actions: String): BankQueryBuilder = filter { item ->
        val def = ApiContext.get().definitions.getItem(item.id)
        def != null && def.inventoryActions.filterNotNull().any { it in actions.toSet() }
    }

    fun minQuantity(min: Int): BankQueryBuilder = filter { item ->
        item.quantity >= min
    }

    fun maxQuantity(max: Int): BankQueryBuilder = filter { item ->
        item.quantity <= max
    }

    override fun results(): QueryResults<BankItem> {
        val filtered = applyFilters(fetchEntities())
        return QueryResults(filtered)
    }

    override fun fetchEntities(): List<BankItem> =
        ApiContext.get().banking.getItems()
}

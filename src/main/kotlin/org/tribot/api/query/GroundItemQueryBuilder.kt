package org.tribot.api.query

import org.tribot.api.ApiContext
import org.tribot.automation.script.core.GroundItem

/**
 * Fluent query builder for ground items in the game world.
 */
class GroundItemQueryBuilder : QueryBuilder<GroundItem, GroundItemQueryBuilder>() {

    fun names(vararg names: String): GroundItemQueryBuilder = filter { item ->
        val def = ApiContext.get().definitions.getItem(item.id)
        def != null && def.name in names
    }

    fun ids(vararg ids: Int): GroundItemQueryBuilder = filter { item ->
        item.id in ids.toSet()
    }

    fun minQuantity(min: Int): GroundItemQueryBuilder = filter { item ->
        item.quantity >= min
    }

    fun maxQuantity(max: Int): GroundItemQueryBuilder = filter { item ->
        item.quantity <= max
    }

    fun withinDistance(maxDistance: Int): GroundItemQueryBuilder = filter { item ->
        val playerLocation = ApiContext.get().worldViews.getLocalPlayer()?.worldLocation ?: return@filter false
        item.position.distanceTo(playerLocation) <= maxDistance
    }

    override fun results(): LocatableQueryResults<GroundItem> {
        val filtered = applyFilters(fetchEntities())
        return LocatableQueryResults(filtered) { it.position }
    }

    override fun fetchEntities(): List<GroundItem> =
        ApiContext.get().worldViews.getTopLevelGroundItems()
}

package org.tribot.api.query

import net.runelite.api.Player
import org.tribot.api.ApiContext

/**
 * Fluent query builder for players in the game world.
 * By default, the local player is excluded from results.
 */
class PlayerQueryBuilder : QueryBuilder<Player, PlayerQueryBuilder>() {

    private var includeLocal: Boolean = false

    fun names(vararg names: String): PlayerQueryBuilder = filter { player ->
        player.name in names
    }

    fun minCombatLevel(min: Int): PlayerQueryBuilder = filter { it.combatLevel >= min }

    fun maxCombatLevel(max: Int): PlayerQueryBuilder = filter { it.combatLevel <= max }

    fun animating(): PlayerQueryBuilder = filter { it.animation != -1 }

    fun notAnimating(): PlayerQueryBuilder = filter { it.animation == -1 }

    fun withinDistance(maxDistance: Int): PlayerQueryBuilder = filter { player ->
        val playerLocation = ApiContext.get().worldViews.getLocalPlayer()?.worldLocation ?: return@filter false
        player.worldLocation.distanceTo(playerLocation) <= maxDistance
    }

    @Suppress("UNCHECKED_CAST")
    fun includeLocalPlayer(): PlayerQueryBuilder {
        includeLocal = true
        return this
    }

    override fun results(): LocatableQueryResults<Player> {
        var entities = fetchEntities()
        if (!includeLocal) {
            val localPlayer = ApiContext.get().worldViews.getLocalPlayer()
            if (localPlayer != null) {
                entities = entities.filter { it != localPlayer }
            }
        }
        val filtered = applyFilters(entities)
        return LocatableQueryResults(filtered) { it.worldLocation }
    }

    override fun fetchEntities(): List<Player> =
        ApiContext.get().worldViews.getTopLevelPlayers()
}

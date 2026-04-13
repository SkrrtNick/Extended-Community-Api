package org.tribot.api.query

import org.tribot.automation.script.ScriptContext
import org.tribot.automation.script.core.world.World
import org.tribot.automation.script.core.world.WorldRegion

/**
 * Fluent query builder for OSRS worlds.
 */
class WorldQueryBuilder(private val ctx: ScriptContext) : QueryBuilder<World, WorldQueryBuilder>() {

    fun members(): WorldQueryBuilder = filter { it.isMembers }

    fun free(): WorldQueryBuilder = filter { !it.isMembers }

    fun pvp(): WorldQueryBuilder = filter { it.isPvp }

    fun notPvp(): WorldQueryBuilder = filter { !it.isPvp }

    fun notDeadman(): WorldQueryBuilder = filter { !it.isDeadman }

    fun notSkillTotal(): WorldQueryBuilder = filter { !it.isSkillTotalWorld }

    fun notSeasonal(): WorldQueryBuilder = filter { !it.isSeasonal }

    fun notFreshStart(): WorldQueryBuilder = filter { !it.isFreshStartWorld }

    /**
     * Filters to worlds that are safe for general botting: excludes PvP, deadman,
     * fresh-start, skill-total, seasonal, speed-running, and grid-master worlds.
     */
    fun safe(): WorldQueryBuilder = filter { world ->
        !world.isPvp && !world.isDeadman && !world.isFreshStartWorld &&
        !world.isSkillTotalWorld && !world.isSeasonal && !world.isSpeedRunning && !world.isGridMaster
    }

    fun maxPopulation(max: Int): WorldQueryBuilder = filter { it.population <= max }

    fun minPopulation(min: Int): WorldQueryBuilder = filter { it.population >= min }

    fun region(vararg regions: WorldRegion): WorldQueryBuilder = filter { it.region in regions.toSet() }

    fun numbers(vararg numbers: Int): WorldQueryBuilder = filter { it.number in numbers.toSet() }

    /**
     * Excludes the world the player is currently logged into.
     * Uses the RuneLite Client.getWorld() value.
     */
    fun notCurrent(): WorldQueryBuilder = filter { world ->
        world.number != ctx.client.world
    }

    fun activityContains(text: String): WorldQueryBuilder = filter { world ->
        world.activity?.contains(text, ignoreCase = true) == true
    }

    override fun fetchEntities(): List<World> = ctx.worldCache.getAll()

    override fun results(): QueryResults<World> {
        val filtered = applyFilters(fetchEntities())
        return QueryResults(filtered)
    }
}

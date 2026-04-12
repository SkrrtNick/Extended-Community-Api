package org.tribot.api.query

import net.runelite.api.NPC
import org.tribot.automation.script.ScriptContext

/**
 * Fluent query builder for NPCs in the game world.
 */
class NpcQueryBuilder(private val ctx: ScriptContext) : QueryBuilder<NPC, NpcQueryBuilder>() {

    fun names(vararg names: String): NpcQueryBuilder = filter { npc ->
        val def = ctx.definitions.getNpc(npc.id)
        def != null && def.name in names
    }

    fun ids(vararg ids: Int): NpcQueryBuilder = filter { npc ->
        npc.id in ids.toSet()
    }

    fun actions(vararg actions: String): NpcQueryBuilder = filter { npc ->
        val def = ctx.definitions.getNpc(npc.id)
        def != null && def.actions.filterNotNull().any { it in actions.toSet() }
    }

    fun withinDistance(maxDistance: Int): NpcQueryBuilder = filter { npc ->
        val playerLocation = ctx.worldViews.getLocalPlayer()?.worldLocation ?: return@filter false
        npc.worldLocation.distanceTo(playerLocation) <= maxDistance
    }

    fun animating(): NpcQueryBuilder = filter { it.animation != -1 }

    fun notAnimating(): NpcQueryBuilder = filter { it.animation == -1 }

    fun inCombat(): NpcQueryBuilder = filter { npc ->
        npc.interacting != null || npc.healthRatio != -1
    }

    fun notInCombat(): NpcQueryBuilder = filter { npc ->
        npc.interacting == null && npc.healthRatio == -1
    }

    fun interactingWithMe(): NpcQueryBuilder = filter { npc ->
        val localPlayer = ctx.worldViews.getLocalPlayer()
        localPlayer != null && npc.interacting == localPlayer
    }

    fun minLevel(min: Int): NpcQueryBuilder = filter { it.combatLevel >= min }

    fun maxLevel(max: Int): NpcQueryBuilder = filter { it.combatLevel <= max }

    override fun results(): LocatableQueryResults<NPC> {
        val filtered = applyFilters(fetchEntities())
        return LocatableQueryResults(filtered) { it.worldLocation }
    }

    override fun fetchEntities(): List<NPC> =
        ctx.worldViews.getTopLevelNpcs()
}

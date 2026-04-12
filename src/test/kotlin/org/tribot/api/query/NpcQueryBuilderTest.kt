package org.tribot.api.query

import io.mockk.every
import net.runelite.api.coords.WorldPoint
import org.tribot.api.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NpcQueryBuilderTest {

    private val playerLocation = WorldPoint(3200, 3200, 0)

    private fun buildContext(
        npcs: List<net.runelite.api.NPC> = emptyList(),
        npcDefs: Map<Int, org.tribot.automation.script.core.definition.NpcDefinition> = emptyMap()
    ): org.tribot.automation.script.ScriptContext {
        val localPlayer = fakePlayer(worldLocation = playerLocation)
        return fakeContext {
            every { worldViews.getTopLevelNpcs() } returns npcs
            every { worldViews.getLocalPlayer() } returns localPlayer
            for ((id, def) in npcDefs) {
                every { definitions.getNpc(id) } returns def
            }
        }
    }

    @Test
    fun `no filters returns all npcs`() {
        val npcs = listOf(
            fakeNpc(id = 1, name = "Goblin"),
            fakeNpc(id = 2, name = "Guard")
        )
        val ctx = buildContext(npcs = npcs, npcDefs = mapOf(
            1 to fakeNpcDef(id = 1, name = "Goblin"),
            2 to fakeNpcDef(id = 2, name = "Guard")
        ))

        val results = NpcQueryBuilder(ctx).results()
        assertEquals(2, results.size)
    }

    @Test
    fun `names filter matches by name`() {
        val npcs = listOf(
            fakeNpc(id = 1, name = "Goblin"),
            fakeNpc(id = 2, name = "Guard")
        )
        val ctx = buildContext(npcs = npcs, npcDefs = mapOf(
            1 to fakeNpcDef(id = 1, name = "Goblin"),
            2 to fakeNpcDef(id = 2, name = "Guard")
        ))

        val results = NpcQueryBuilder(ctx).names("Goblin").results()
        assertEquals(1, results.size)
        assertEquals("Goblin", results.first()?.name)
    }

    @Test
    fun `multiple names filter matches any`() {
        val npcs = listOf(
            fakeNpc(id = 1, name = "Goblin"),
            fakeNpc(id = 2, name = "Guard"),
            fakeNpc(id = 3, name = "Cow")
        )
        val ctx = buildContext(npcs = npcs, npcDefs = mapOf(
            1 to fakeNpcDef(id = 1, name = "Goblin"),
            2 to fakeNpcDef(id = 2, name = "Guard"),
            3 to fakeNpcDef(id = 3, name = "Cow")
        ))

        val results = NpcQueryBuilder(ctx).names("Goblin", "Guard").results()
        assertEquals(2, results.size)
    }

    @Test
    fun `ids filter matches by id`() {
        val npcs = listOf(
            fakeNpc(id = 1, name = "Goblin"),
            fakeNpc(id = 2, name = "Guard")
        )
        val ctx = buildContext(npcs = npcs, npcDefs = mapOf(
            1 to fakeNpcDef(id = 1, name = "Goblin"),
            2 to fakeNpcDef(id = 2, name = "Guard")
        ))

        val results = NpcQueryBuilder(ctx).ids(2).results()
        assertEquals(1, results.size)
        assertEquals(2, results.first()?.id)
    }

    @Test
    fun `actions filter matches by action`() {
        val npcs = listOf(
            fakeNpc(id = 1, name = "Goblin"),
            fakeNpc(id = 2, name = "Banker")
        )
        val ctx = buildContext(npcs = npcs, npcDefs = mapOf(
            1 to fakeNpcDef(id = 1, name = "Goblin", actions = listOf("Attack", null, null, null, null)),
            2 to fakeNpcDef(id = 2, name = "Banker", actions = listOf("Talk-to", "Bank", null, null, null))
        ))

        val results = NpcQueryBuilder(ctx).actions("Bank").results()
        assertEquals(1, results.size)
        assertEquals(2, results.first()?.id)
    }

    @Test
    fun `withinDistance filters by distance`() {
        val npcs = listOf(
            fakeNpc(id = 1, name = "Close", worldLocation = WorldPoint(3202, 3200, 0)),   // distance 2
            fakeNpc(id = 2, name = "Far", worldLocation = WorldPoint(3220, 3200, 0))       // distance 20
        )
        val ctx = buildContext(npcs = npcs, npcDefs = mapOf(
            1 to fakeNpcDef(id = 1, name = "Close"),
            2 to fakeNpcDef(id = 2, name = "Far")
        ))

        val results = NpcQueryBuilder(ctx).withinDistance(10).results()
        assertEquals(1, results.size)
        assertEquals("Close", results.first()?.name)
    }

    @Test
    fun `custom filter works`() {
        val npcs = listOf(
            fakeNpc(id = 1, name = "Goblin", combatLevel = 5),
            fakeNpc(id = 2, name = "Guard", combatLevel = 21)
        )
        val ctx = buildContext(npcs = npcs, npcDefs = mapOf(
            1 to fakeNpcDef(id = 1, name = "Goblin"),
            2 to fakeNpcDef(id = 2, name = "Guard")
        ))

        val results = NpcQueryBuilder(ctx).filter { it.combatLevel > 10 }.results()
        assertEquals(1, results.size)
        assertEquals("Guard", results.first()?.name)
    }

    @Test
    fun `chaining multiple filters`() {
        val npcs = listOf(
            fakeNpc(id = 1, name = "Goblin", worldLocation = WorldPoint(3202, 3200, 0)),
            fakeNpc(id = 2, name = "Goblin", worldLocation = WorldPoint(3220, 3200, 0)),
            fakeNpc(id = 3, name = "Guard", worldLocation = WorldPoint(3201, 3200, 0))
        )
        val ctx = buildContext(npcs = npcs, npcDefs = mapOf(
            1 to fakeNpcDef(id = 1, name = "Goblin"),
            2 to fakeNpcDef(id = 2, name = "Goblin"),
            3 to fakeNpcDef(id = 3, name = "Guard")
        ))

        val results = NpcQueryBuilder(ctx).names("Goblin").withinDistance(10).results()
        assertEquals(1, results.size)
        assertEquals(1, results.first()?.id)
    }

    @Test
    fun `animating filters for animation not equal to -1`() {
        val npcs = listOf(
            fakeNpc(id = 1, name = "Idle", animation = -1),
            fakeNpc(id = 2, name = "Active", animation = 808)
        )
        val ctx = buildContext(npcs = npcs, npcDefs = mapOf(
            1 to fakeNpcDef(id = 1, name = "Idle"),
            2 to fakeNpcDef(id = 2, name = "Active")
        ))

        val results = NpcQueryBuilder(ctx).animating().results()
        assertEquals(1, results.size)
        assertEquals("Active", results.first()?.name)
    }

    @Test
    fun `notAnimating filters for animation equal to -1`() {
        val npcs = listOf(
            fakeNpc(id = 1, name = "Idle", animation = -1),
            fakeNpc(id = 2, name = "Active", animation = 808)
        )
        val ctx = buildContext(npcs = npcs, npcDefs = mapOf(
            1 to fakeNpcDef(id = 1, name = "Idle"),
            2 to fakeNpcDef(id = 2, name = "Active")
        ))

        val results = NpcQueryBuilder(ctx).notAnimating().results()
        assertEquals(1, results.size)
        assertEquals("Idle", results.first()?.name)
    }

    @Test
    fun `notInCombat filters npcs not in combat`() {
        val attacker = fakePlayer(name = "Attacker")
        val npcs = listOf(
            fakeNpc(id = 1, name = "Peaceful", interacting = null, healthRatio = -1),
            fakeNpc(id = 2, name = "Fighting", interacting = attacker, healthRatio = 30)
        )
        val ctx = buildContext(npcs = npcs, npcDefs = mapOf(
            1 to fakeNpcDef(id = 1, name = "Peaceful"),
            2 to fakeNpcDef(id = 2, name = "Fighting")
        ))

        val results = NpcQueryBuilder(ctx).notInCombat().results()
        assertEquals(1, results.size)
        assertEquals("Peaceful", results.first()?.name)
    }

    @Test
    fun `inCombat filters npcs in combat`() {
        val attacker = fakePlayer(name = "Attacker")
        val npcs = listOf(
            fakeNpc(id = 1, name = "Peaceful", interacting = null, healthRatio = -1),
            fakeNpc(id = 2, name = "Fighting", interacting = attacker, healthRatio = 30)
        )
        val ctx = buildContext(npcs = npcs, npcDefs = mapOf(
            1 to fakeNpcDef(id = 1, name = "Peaceful"),
            2 to fakeNpcDef(id = 2, name = "Fighting")
        ))

        val results = NpcQueryBuilder(ctx).inCombat().results()
        assertEquals(1, results.size)
        assertEquals("Fighting", results.first()?.name)
    }

    @Test
    fun `interactingWithMe filters npcs interacting with local player`() {
        val localPlayer = fakePlayer(name = "Me", worldLocation = playerLocation)
        val otherPlayer = fakePlayer(name = "Other")
        val npcs = listOf(
            fakeNpc(id = 1, name = "AttackingMe", interacting = localPlayer),
            fakeNpc(id = 2, name = "AttackingOther", interacting = otherPlayer),
            fakeNpc(id = 3, name = "Idle", interacting = null)
        )
        val ctx = fakeContext {
            every { worldViews.getTopLevelNpcs() } returns npcs
            every { worldViews.getLocalPlayer() } returns localPlayer
            every { definitions.getNpc(1) } returns fakeNpcDef(id = 1, name = "AttackingMe")
            every { definitions.getNpc(2) } returns fakeNpcDef(id = 2, name = "AttackingOther")
            every { definitions.getNpc(3) } returns fakeNpcDef(id = 3, name = "Idle")
        }

        val results = NpcQueryBuilder(ctx).interactingWithMe().results()
        assertEquals(1, results.size)
        assertEquals("AttackingMe", results.first()?.name)
    }

    @Test
    fun `minLevel filters by minimum combat level`() {
        val npcs = listOf(
            fakeNpc(id = 1, name = "Weak", combatLevel = 5),
            fakeNpc(id = 2, name = "Strong", combatLevel = 50)
        )
        val ctx = buildContext(npcs = npcs, npcDefs = mapOf(
            1 to fakeNpcDef(id = 1, name = "Weak"),
            2 to fakeNpcDef(id = 2, name = "Strong")
        ))

        val results = NpcQueryBuilder(ctx).minLevel(10).results()
        assertEquals(1, results.size)
        assertEquals("Strong", results.first()?.name)
    }

    @Test
    fun `maxLevel filters by maximum combat level`() {
        val npcs = listOf(
            fakeNpc(id = 1, name = "Weak", combatLevel = 5),
            fakeNpc(id = 2, name = "Strong", combatLevel = 50)
        )
        val ctx = buildContext(npcs = npcs, npcDefs = mapOf(
            1 to fakeNpcDef(id = 1, name = "Weak"),
            2 to fakeNpcDef(id = 2, name = "Strong")
        ))

        val results = NpcQueryBuilder(ctx).maxLevel(10).results()
        assertEquals(1, results.size)
        assertEquals("Weak", results.first()?.name)
    }

    @Test
    fun `nearest from results returns closest npc`() {
        val npcs = listOf(
            fakeNpc(id = 1, name = "Far", worldLocation = WorldPoint(3210, 3200, 0)),
            fakeNpc(id = 2, name = "Close", worldLocation = WorldPoint(3201, 3200, 0))
        )
        val ctx = buildContext(npcs = npcs, npcDefs = mapOf(
            1 to fakeNpcDef(id = 1, name = "Far"),
            2 to fakeNpcDef(id = 2, name = "Close")
        ))

        val results = NpcQueryBuilder(ctx).results()
        val nearest = results.nearest(playerLocation)
        assertEquals("Close", nearest?.name)
    }
}

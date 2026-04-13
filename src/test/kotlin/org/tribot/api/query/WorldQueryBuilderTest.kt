package org.tribot.api.query

import io.mockk.every
import io.mockk.mockk
import org.tribot.automation.script.core.world.World
import org.tribot.automation.script.core.world.WorldRegion
import org.tribot.api.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorldQueryBuilderTest {

    private fun fakeWorld(
        number: Int = 301,
        region: WorldRegion? = WorldRegion.UNITED_STATES_OF_AMERICA,
        population: Int = 500,
        activity: String? = null,
        isMembers: Boolean = true,
        isPvp: Boolean = false,
        isBounty: Boolean = false,
        isDeadman: Boolean = false,
        isFreshStartWorld: Boolean = false,
        isSkillTotalWorld: Boolean = false,
        isSpeedRunning: Boolean = false,
        isSeasonal: Boolean = false,
        isGridMaster: Boolean = false
    ): World {
        val world = mockk<World>(relaxed = true)
        every { world.number } returns number
        every { world.region } returns region
        every { world.population } returns population
        every { world.activity } returns activity
        every { world.isMembers } returns isMembers
        every { world.isPvp } returns isPvp
        every { world.isBounty } returns isBounty
        every { world.isDeadman } returns isDeadman
        every { world.isFreshStartWorld } returns isFreshStartWorld
        every { world.isSkillTotalWorld } returns isSkillTotalWorld
        every { world.isSpeedRunning } returns isSpeedRunning
        every { world.isSeasonal } returns isSeasonal
        every { world.isGridMaster } returns isGridMaster
        return world
    }

    private fun buildContext(
        worlds: List<World> = emptyList(),
        currentWorld: Int = 301
    ): org.tribot.automation.script.ScriptContext {
        return fakeContext {
            every { worldCache.getAll() } returns worlds
            every { client.world } returns currentWorld
        }
    }

    @Test
    fun `no filters returns all worlds`() {
        val worlds = listOf(
            fakeWorld(number = 301),
            fakeWorld(number = 302),
            fakeWorld(number = 303)
        )
        val ctx = buildContext(worlds = worlds)

        val results = WorldQueryBuilder(ctx).results()
        assertEquals(3, results.size)
    }

    @Test
    fun `members filter returns only member worlds`() {
        val worlds = listOf(
            fakeWorld(number = 301, isMembers = true),
            fakeWorld(number = 308, isMembers = false),
            fakeWorld(number = 310, isMembers = true)
        )
        val ctx = buildContext(worlds = worlds)

        val results = WorldQueryBuilder(ctx).members().results()
        assertEquals(2, results.size)
        assertTrue(results.asList().all { it.isMembers })
    }

    @Test
    fun `free filter returns only free worlds`() {
        val worlds = listOf(
            fakeWorld(number = 301, isMembers = true),
            fakeWorld(number = 308, isMembers = false)
        )
        val ctx = buildContext(worlds = worlds)

        val results = WorldQueryBuilder(ctx).free().results()
        assertEquals(1, results.size)
        assertEquals(308, results.first()?.number)
    }

    @Test
    fun `pvp filter returns only pvp worlds`() {
        val worlds = listOf(
            fakeWorld(number = 301, isPvp = false),
            fakeWorld(number = 325, isPvp = true)
        )
        val ctx = buildContext(worlds = worlds)

        val results = WorldQueryBuilder(ctx).pvp().results()
        assertEquals(1, results.size)
        assertEquals(325, results.first()?.number)
    }

    @Test
    fun `notPvp filter excludes pvp worlds`() {
        val worlds = listOf(
            fakeWorld(number = 301, isPvp = false),
            fakeWorld(number = 325, isPvp = true)
        )
        val ctx = buildContext(worlds = worlds)

        val results = WorldQueryBuilder(ctx).notPvp().results()
        assertEquals(1, results.size)
        assertEquals(301, results.first()?.number)
    }

    @Test
    fun `safe filter excludes dangerous world types`() {
        val worlds = listOf(
            fakeWorld(number = 301),  // normal safe world
            fakeWorld(number = 325, isPvp = true),
            fakeWorld(number = 345, isDeadman = true),
            fakeWorld(number = 401, isFreshStartWorld = true),
            fakeWorld(number = 500, isSkillTotalWorld = true),
            fakeWorld(number = 510, isSeasonal = true),
            fakeWorld(number = 520, isSpeedRunning = true),
            fakeWorld(number = 530, isGridMaster = true)
        )
        val ctx = buildContext(worlds = worlds)

        val results = WorldQueryBuilder(ctx).safe().results()
        assertEquals(1, results.size)
        assertEquals(301, results.first()?.number)
    }

    @Test
    fun `maxPopulation filters by maximum population`() {
        val worlds = listOf(
            fakeWorld(number = 301, population = 200),
            fakeWorld(number = 302, population = 800),
            fakeWorld(number = 303, population = 500)
        )
        val ctx = buildContext(worlds = worlds)

        val results = WorldQueryBuilder(ctx).maxPopulation(500).results()
        assertEquals(2, results.size)
    }

    @Test
    fun `minPopulation filters by minimum population`() {
        val worlds = listOf(
            fakeWorld(number = 301, population = 200),
            fakeWorld(number = 302, population = 800),
            fakeWorld(number = 303, population = 500)
        )
        val ctx = buildContext(worlds = worlds)

        val results = WorldQueryBuilder(ctx).minPopulation(500).results()
        assertEquals(2, results.size)
    }

    @Test
    fun `region filter matches by region`() {
        val worlds = listOf(
            fakeWorld(number = 301, region = WorldRegion.UNITED_STATES_OF_AMERICA),
            fakeWorld(number = 302, region = WorldRegion.UNITED_KINGDOM),
            fakeWorld(number = 303, region = WorldRegion.AUSTRALIA)
        )
        val ctx = buildContext(worlds = worlds)

        val results = WorldQueryBuilder(ctx).region(WorldRegion.UNITED_STATES_OF_AMERICA, WorldRegion.UNITED_KINGDOM).results()
        assertEquals(2, results.size)
    }

    @Test
    fun `numbers filter matches by world number`() {
        val worlds = listOf(
            fakeWorld(number = 301),
            fakeWorld(number = 302),
            fakeWorld(number = 303)
        )
        val ctx = buildContext(worlds = worlds)

        val results = WorldQueryBuilder(ctx).numbers(301, 303).results()
        assertEquals(2, results.size)
    }

    @Test
    fun `notCurrent excludes the current world`() {
        val worlds = listOf(
            fakeWorld(number = 301),
            fakeWorld(number = 302),
            fakeWorld(number = 303)
        )
        val ctx = buildContext(worlds = worlds, currentWorld = 302)

        val results = WorldQueryBuilder(ctx).notCurrent().results()
        assertEquals(2, results.size)
        assertTrue(results.asList().none { it.number == 302 })
    }

    @Test
    fun `activityContains filters by activity text`() {
        val worlds = listOf(
            fakeWorld(number = 301, activity = "Trade - Free"),
            fakeWorld(number = 302, activity = null),
            fakeWorld(number = 303, activity = "PvP Arena")
        )
        val ctx = buildContext(worlds = worlds)

        val results = WorldQueryBuilder(ctx).activityContains("trade").results()
        assertEquals(1, results.size)
        assertEquals(301, results.first()?.number)
    }

    @Test
    fun `notDeadman excludes deadman worlds`() {
        val worlds = listOf(
            fakeWorld(number = 301, isDeadman = false),
            fakeWorld(number = 345, isDeadman = true)
        )
        val ctx = buildContext(worlds = worlds)

        val results = WorldQueryBuilder(ctx).notDeadman().results()
        assertEquals(1, results.size)
        assertEquals(301, results.first()?.number)
    }

    @Test
    fun `notSkillTotal excludes skill total worlds`() {
        val worlds = listOf(
            fakeWorld(number = 301, isSkillTotalWorld = false),
            fakeWorld(number = 500, isSkillTotalWorld = true)
        )
        val ctx = buildContext(worlds = worlds)

        val results = WorldQueryBuilder(ctx).notSkillTotal().results()
        assertEquals(1, results.size)
        assertEquals(301, results.first()?.number)
    }

    @Test
    fun `notSeasonal excludes seasonal worlds`() {
        val worlds = listOf(
            fakeWorld(number = 301, isSeasonal = false),
            fakeWorld(number = 510, isSeasonal = true)
        )
        val ctx = buildContext(worlds = worlds)

        val results = WorldQueryBuilder(ctx).notSeasonal().results()
        assertEquals(1, results.size)
        assertEquals(301, results.first()?.number)
    }

    @Test
    fun `notFreshStart excludes fresh start worlds`() {
        val worlds = listOf(
            fakeWorld(number = 301, isFreshStartWorld = false),
            fakeWorld(number = 401, isFreshStartWorld = true)
        )
        val ctx = buildContext(worlds = worlds)

        val results = WorldQueryBuilder(ctx).notFreshStart().results()
        assertEquals(1, results.size)
        assertEquals(301, results.first()?.number)
    }

    @Test
    fun `chaining multiple filters`() {
        val worlds = listOf(
            fakeWorld(number = 301, isMembers = true, population = 200, region = WorldRegion.UNITED_STATES_OF_AMERICA),
            fakeWorld(number = 302, isMembers = true, population = 800, region = WorldRegion.UNITED_STATES_OF_AMERICA),
            fakeWorld(number = 303, isMembers = false, population = 200, region = WorldRegion.UNITED_KINGDOM),
            fakeWorld(number = 304, isMembers = true, population = 200, region = WorldRegion.UNITED_KINGDOM)
        )
        val ctx = buildContext(worlds = worlds, currentWorld = 304)

        val results = WorldQueryBuilder(ctx)
            .members()
            .maxPopulation(500)
            .region(WorldRegion.UNITED_STATES_OF_AMERICA)
            .notCurrent()
            .results()
        assertEquals(1, results.size)
        assertEquals(301, results.first()?.number)
    }
}

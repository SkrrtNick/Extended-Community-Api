package org.tribot.api.testing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FakesTest {

    @Test
    fun `fakeContext creates relaxed mock`() {
        val ctx = fakeContext()
        assertNotNull(ctx)
    }

    @Test
    fun `fakeNpc returns mock with expected properties`() {
        val npc = fakeNpc(id = 42, name = "Goblin", combatLevel = 5)
        assertEquals(42, npc.id)
        assertEquals("Goblin", npc.name)
        assertEquals(5, npc.combatLevel)
    }

    @Test
    fun `fakePlayer returns mock with expected properties`() {
        val player = fakePlayer(name = "TestPlayer", combatLevel = 126)
        assertEquals("TestPlayer", player.name)
        assertEquals(126, player.combatLevel)
    }

    @Test
    fun `fakeNpcDef returns mock with expected properties`() {
        val def = fakeNpcDef(id = 10, name = "Guard", combatLevel = 21)
        assertEquals(10, def.id)
        assertEquals("Guard", def.name)
        assertEquals(21, def.combatLevel)
    }

    @Test
    fun `fakeObjectDef returns mock with expected properties`() {
        val def = fakeObjectDef(id = 100, name = "Tree")
        assertEquals(100, def.id)
        assertEquals("Tree", def.name)
    }

    @Test
    fun `fakeItemDef returns mock with expected properties`() {
        val def = fakeItemDef(id = 995, name = "Coins", stackable = true)
        assertEquals(995, def.id)
        assertEquals("Coins", def.name)
        assertEquals(true, def.stackable)
    }
}

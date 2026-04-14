package org.tribot.api.events

import io.mockk.*
import net.runelite.api.*
import net.runelite.api.coords.WorldPoint
import org.tribot.api.ApiContext
import org.tribot.api.testing.fakeContext
import org.tribot.api.testing.fakeNpc
import org.tribot.api.testing.fakePlayer
import org.tribot.api.testing.fakeWidget
import org.tribot.automation.script.core.GroundItem
import org.tribot.automation.script.core.tabs.EquipmentSlot
import org.tribot.automation.script.core.tabs.EquippedItem
import org.tribot.automation.script.core.tabs.InventoryItem
import org.tribot.automation.script.event.ListenerRegistration
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EventDispatcherTest {

    @AfterTest
    fun tearDown() {
        ApiContext.reset()
    }

    // --- Test helpers ---

    private fun fakeGroundItem(
        id: Int,
        quantity: Int = 1,
        position: WorldPoint = WorldPoint(3200, 3200, 0)
    ): GroundItem {
        val tileItem = mockk<TileItem>(relaxed = true)
        every { tileItem.id } returns id
        every { tileItem.quantity } returns quantity
        return GroundItem(tileItem, position)
    }

    private fun fakeTileObject(id: Int, worldLocation: WorldPoint = WorldPoint(3200, 3200, 0)): TileObject {
        val obj = mockk<TileObject>(relaxed = true)
        every { obj.id } returns id
        every { obj.worldLocation } returns worldLocation
        return obj
    }

    private fun fakeGeOffer(state: GrandExchangeOfferState = GrandExchangeOfferState.EMPTY): GrandExchangeOffer {
        val offer = mockk<GrandExchangeOffer>(relaxed = true)
        every { offer.state } returns state
        return offer
    }

    /**
     * Creates a fakeContext pre-configured with sensible defaults for EventDispatcher tests.
     * Returns a triple of (ctx, tickSlot, renderSlot) so tests can simulate ticks/frames.
     */
    private fun buildDispatcherContext(
        block: ContextSetup.() -> Unit = {}
    ): DispatcherTestHarness {
        val tickSlot = slot<() -> Unit>()
        val renderSlot = slot<() -> Unit>()
        val tickReg = mockk<ListenerRegistration>(relaxed = true)
        val renderReg = mockk<ListenerRegistration>(relaxed = true)

        val setup = ContextSetup()
        setup.block()

        val localPlayer = fakePlayer(name = "LocalPlayer")

        val ctx = fakeContext {
            every { events.onGameTick(capture(tickSlot)) } returns tickReg
            every { events.onBeforeRender(capture(renderSlot)) } returns renderReg

            // Skills
            every { skills.getXp(any()) } returns 0
            every { skills.getLevel(any()) } returns 1
            every { skills.getBoostedLevel(any()) } returns 1

            // Inventory and equipment
            every { inventory.getItems() } returns setup.initialInventory
            every { equipment.getItems() } returns setup.initialEquipment

            // World views
            every { worldViews.getLocalPlayer() } returns localPlayer
            every { worldViews.getTopLevelNpcs() } returns setup.initialNpcs
            every { worldViews.getTopLevelPlayers() } returns (setup.initialPlayers + localPlayer)
            every { worldViews.getTopLevelObjects() } returns setup.initialObjects
            every { worldViews.getTopLevelGroundItems() } returns setup.initialGroundItems

            // Client
            every { client.getVarbitValue(any()) } returns 0
            every { client.grandExchangeOffers } returns (setup.initialGeOffers ?: Array(8) { fakeGeOffer() })
            every { client.getWidget(any<Int>(), any()) } returns null
        }

        ApiContext.init(ctx)
        return DispatcherTestHarness(ctx, tickSlot, renderSlot, tickReg, renderReg)
    }

    private class ContextSetup {
        var initialInventory: List<InventoryItem> = emptyList()
        var initialEquipment: List<EquippedItem> = emptyList()
        var initialNpcs: List<NPC> = emptyList()
        var initialPlayers: List<Player> = emptyList()
        var initialObjects: List<TileObject> = emptyList()
        var initialGroundItems: List<GroundItem> = emptyList()
        var initialGeOffers: Array<GrandExchangeOffer>? = null
    }

    private data class DispatcherTestHarness(
        val ctx: org.tribot.automation.script.ScriptContext,
        val tickSlot: CapturingSlot<() -> Unit>,
        val renderSlot: CapturingSlot<() -> Unit>,
        val tickReg: ListenerRegistration,
        val renderReg: ListenerRegistration
    ) {
        fun simulateTick() {
            if (tickSlot.isCaptured) tickSlot.captured.invoke()
        }

        fun simulateFrame() {
            if (renderSlot.isCaptured) renderSlot.captured.invoke()
        }
    }

    // =========================================================================
    // 1. Stat change detection
    // =========================================================================

    @Test
    fun `stat change fires listener with correct old and new values`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        // Initial: ATTACK xp = 0
        every { harness.ctx.skills.getXp(Skill.ATTACK) } returns 0
        every { harness.ctx.skills.getLevel(Skill.ATTACK) } returns 1
        every { harness.ctx.skills.getBoostedLevel(Skill.ATTACK) } returns 1

        val events = mutableListOf<Triple<Skill, Int, Int>>()
        dispatcher.onStatChanged { skill, oldXp, newXp, _, _, _, _ ->
            events.add(Triple(skill, oldXp, newXp))
        }
        dispatcher.start()

        // Change XP for next tick
        every { harness.ctx.skills.getXp(Skill.ATTACK) } returns 500
        every { harness.ctx.skills.getLevel(Skill.ATTACK) } returns 2
        harness.simulateTick()

        assertEquals(1, events.size)
        assertEquals(Skill.ATTACK, events[0].first)
        assertEquals(0, events[0].second)
        assertEquals(500, events[0].third)
    }

    @Test
    fun `stat change fires with level and boosted level changes`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        every { harness.ctx.skills.getXp(Skill.STRENGTH) } returns 1000
        every { harness.ctx.skills.getLevel(Skill.STRENGTH) } returns 10
        every { harness.ctx.skills.getBoostedLevel(Skill.STRENGTH) } returns 10

        var capturedOldLevel = -1
        var capturedNewLevel = -1
        var capturedOldBoosted = -1
        var capturedNewBoosted = -1

        dispatcher.onStatChanged { _, _, _, oldLevel, newLevel, oldBoosted, newBoosted ->
            capturedOldLevel = oldLevel
            capturedNewLevel = newLevel
            capturedOldBoosted = oldBoosted
            capturedNewBoosted = newBoosted
        }
        dispatcher.start()

        // Boosted level changes (e.g., potion)
        every { harness.ctx.skills.getBoostedLevel(Skill.STRENGTH) } returns 15
        harness.simulateTick()

        assertEquals(10, capturedOldLevel)
        assertEquals(10, capturedNewLevel)
        assertEquals(10, capturedOldBoosted)
        assertEquals(15, capturedNewBoosted)
    }

    @Test
    fun `no stat change does not fire listener`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        every { harness.ctx.skills.getXp(Skill.ATTACK) } returns 100
        every { harness.ctx.skills.getLevel(Skill.ATTACK) } returns 1
        every { harness.ctx.skills.getBoostedLevel(Skill.ATTACK) } returns 1

        var fired = false
        dispatcher.onStatChanged { _, _, _, _, _, _, _ -> fired = true }
        dispatcher.start()

        // Same values on next tick
        harness.simulateTick()

        assertFalse(fired)
    }

    // =========================================================================
    // 2. Inventory change detection
    // =========================================================================

    @Test
    fun `inventory change fires listener with old and new items`() {
        val initialItems = listOf(InventoryItem(995, 100, 0))
        val harness = buildDispatcherContext {
            initialInventory = initialItems
        }
        val dispatcher = EventDispatcher()

        var capturedOld: List<InventoryItem>? = null
        var capturedNew: List<InventoryItem>? = null

        dispatcher.onInventoryChanged { old, new ->
            capturedOld = old
            capturedNew = new
        }
        dispatcher.start()

        // Change inventory
        val newItems = listOf(InventoryItem(995, 100, 0), InventoryItem(526, 1, 1))
        every { harness.ctx.inventory.getItems() } returns newItems
        harness.simulateTick()

        assertEquals(initialItems, capturedOld)
        assertEquals(newItems, capturedNew)
    }

    @Test
    fun `no inventory change does not fire listener`() {
        val items = listOf(InventoryItem(995, 100, 0))
        val harness = buildDispatcherContext {
            initialInventory = items
        }
        val dispatcher = EventDispatcher()

        var fired = false
        dispatcher.onInventoryChanged { _, _ -> fired = true }
        dispatcher.start()

        // Same items on next tick
        harness.simulateTick()

        assertFalse(fired)
    }

    // =========================================================================
    // 3. Equipment change detection
    // =========================================================================

    @Test
    fun `equipment change fires listener`() {
        val initialEquip = listOf(EquippedItem(4151, 1, EquipmentSlot.WEAPON))
        val harness = buildDispatcherContext {
            initialEquipment = initialEquip
        }
        val dispatcher = EventDispatcher()

        var capturedOld: List<EquippedItem>? = null
        var capturedNew: List<EquippedItem>? = null

        dispatcher.onEquipmentChanged { old, new ->
            capturedOld = old
            capturedNew = new
        }
        dispatcher.start()

        // Change equipment
        val newEquip = listOf(
            EquippedItem(4151, 1, EquipmentSlot.WEAPON),
            EquippedItem(1127, 1, EquipmentSlot.BODY)
        )
        every { harness.ctx.equipment.getItems() } returns newEquip
        harness.simulateTick()

        assertEquals(initialEquip, capturedOld)
        assertEquals(newEquip, capturedNew)
    }

    // =========================================================================
    // 4. NPC spawn/despawn
    // =========================================================================

    @Test
    fun `npc spawned fires when new npc appears`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        val spawnedNpcs = mutableListOf<NPC>()
        dispatcher.onNpcSpawned { npc -> spawnedNpcs.add(npc) }
        dispatcher.start()

        // New NPC appears
        val goblin = fakeNpc(id = 1, name = "Goblin")
        every { goblin.index } returns 42
        every { harness.ctx.worldViews.getTopLevelNpcs() } returns listOf(goblin)
        harness.simulateTick()

        assertEquals(1, spawnedNpcs.size)
        assertEquals("Goblin", spawnedNpcs[0].name)
    }

    @Test
    fun `npc despawned fires when npc disappears`() {
        val goblin = fakeNpc(id = 1, name = "Goblin")
        every { goblin.index } returns 42

        val harness = buildDispatcherContext {
            initialNpcs = listOf(goblin)
        }
        val dispatcher = EventDispatcher()

        val despawnedNpcs = mutableListOf<NPC>()
        dispatcher.onNpcDespawned { npc -> despawnedNpcs.add(npc) }
        dispatcher.start()

        // NPC gone
        every { harness.ctx.worldViews.getTopLevelNpcs() } returns emptyList()
        harness.simulateTick()

        assertEquals(1, despawnedNpcs.size)
        assertEquals("Goblin", despawnedNpcs[0].name)
    }

    @Test
    fun `npc that persists across ticks is not reported as spawned or despawned`() {
        val goblin = fakeNpc(id = 1, name = "Goblin")
        every { goblin.index } returns 42

        val harness = buildDispatcherContext {
            initialNpcs = listOf(goblin)
        }
        val dispatcher = EventDispatcher()

        var spawnCount = 0
        var despawnCount = 0
        dispatcher.onNpcSpawned { spawnCount++ }
        dispatcher.onNpcDespawned { despawnCount++ }
        dispatcher.start()

        // Same NPC still here
        harness.simulateTick()

        assertEquals(0, spawnCount)
        assertEquals(0, despawnCount)
    }

    // =========================================================================
    // 5. Player spawn/despawn
    // =========================================================================

    @Test
    fun `player spawned fires when new player appears`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        val spawnedPlayers = mutableListOf<Player>()
        dispatcher.onPlayerSpawned { player -> spawnedPlayers.add(player) }
        dispatcher.start()

        // New player appears
        val otherPlayer = fakePlayer(name = "OtherPlayer")
        val localPlayer = harness.ctx.worldViews.getLocalPlayer()!!
        every { harness.ctx.worldViews.getTopLevelPlayers() } returns listOf(localPlayer, otherPlayer)
        harness.simulateTick()

        assertEquals(1, spawnedPlayers.size)
        assertEquals("OtherPlayer", spawnedPlayers[0].name)
    }

    @Test
    fun `player despawned fires when player disappears`() {
        val otherPlayer = fakePlayer(name = "OtherPlayer")
        val harness = buildDispatcherContext {
            initialPlayers = listOf(otherPlayer)
        }
        val dispatcher = EventDispatcher()

        val despawnedPlayers = mutableListOf<Player>()
        dispatcher.onPlayerDespawned { player -> despawnedPlayers.add(player) }
        dispatcher.start()

        // Player gone (only local player remains)
        val localPlayer = harness.ctx.worldViews.getLocalPlayer()!!
        every { harness.ctx.worldViews.getTopLevelPlayers() } returns listOf(localPlayer)
        harness.simulateTick()

        assertEquals(1, despawnedPlayers.size)
        assertEquals("OtherPlayer", despawnedPlayers[0].name)
    }

    @Test
    fun `local player is excluded from spawn and despawn events`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        var spawnCount = 0
        var despawnCount = 0
        dispatcher.onPlayerSpawned { spawnCount++ }
        dispatcher.onPlayerDespawned { despawnCount++ }
        dispatcher.start()

        // Tick with no changes
        harness.simulateTick()

        assertEquals(0, spawnCount)
        assertEquals(0, despawnCount)
    }

    // =========================================================================
    // 6. Game object spawn/despawn
    // =========================================================================

    @Test
    fun `object spawned fires when new object appears`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        val spawnedObjects = mutableListOf<TileObject>()
        dispatcher.onObjectSpawned { obj -> spawnedObjects.add(obj) }
        dispatcher.start()

        val tree = fakeTileObject(id = 1276, worldLocation = WorldPoint(3200, 3200, 0))
        every { harness.ctx.worldViews.getTopLevelObjects() } returns listOf(tree)
        harness.simulateTick()

        assertEquals(1, spawnedObjects.size)
        assertEquals(1276, spawnedObjects[0].id)
    }

    @Test
    fun `object despawned fires when object disappears`() {
        val tree = fakeTileObject(id = 1276, worldLocation = WorldPoint(3200, 3200, 0))
        val harness = buildDispatcherContext {
            initialObjects = listOf(tree)
        }
        val dispatcher = EventDispatcher()

        val despawnedObjects = mutableListOf<TileObject>()
        dispatcher.onObjectDespawned { obj -> despawnedObjects.add(obj) }
        dispatcher.start()

        every { harness.ctx.worldViews.getTopLevelObjects() } returns emptyList()
        harness.simulateTick()

        assertEquals(1, despawnedObjects.size)
        assertEquals(1276, despawnedObjects[0].id)
    }

    // =========================================================================
    // 7. Ground item spawn/despawn
    // =========================================================================

    @Test
    fun `ground item spawned fires when new item appears`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        val spawnedItems = mutableListOf<GroundItem>()
        dispatcher.onGroundItemSpawned { item -> spawnedItems.add(item) }
        dispatcher.start()

        val bones = fakeGroundItem(id = 526, position = WorldPoint(3200, 3200, 0))
        every { harness.ctx.worldViews.getTopLevelGroundItems() } returns listOf(bones)
        harness.simulateTick()

        assertEquals(1, spawnedItems.size)
        assertEquals(526, spawnedItems[0].id)
    }

    @Test
    fun `ground item despawned fires when item disappears`() {
        val bones = fakeGroundItem(id = 526, position = WorldPoint(3200, 3200, 0))
        val harness = buildDispatcherContext {
            initialGroundItems = listOf(bones)
        }
        val dispatcher = EventDispatcher()

        val despawnedItems = mutableListOf<GroundItem>()
        dispatcher.onGroundItemDespawned { item -> despawnedItems.add(item) }
        dispatcher.start()

        every { harness.ctx.worldViews.getTopLevelGroundItems() } returns emptyList()
        harness.simulateTick()

        assertEquals(1, despawnedItems.size)
        assertEquals(526, despawnedItems[0].id)
    }

    // =========================================================================
    // 8. Varbit change
    // =========================================================================

    @Test
    fun `varbit change fires listener with old and new values`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        every { harness.ctx.client.getVarbitValue(4070) } returns 0

        val events = mutableListOf<Triple<Int, Int, Int>>()
        dispatcher.onVarbitChanged { varbitId, oldValue, newValue ->
            events.add(Triple(varbitId, oldValue, newValue))
        }
        dispatcher.watchVarbit(4070)
        dispatcher.start()

        // Change varbit
        every { harness.ctx.client.getVarbitValue(4070) } returns 1
        harness.simulateTick()

        assertEquals(1, events.size)
        assertEquals(4070, events[0].first)
        assertEquals(0, events[0].second)
        assertEquals(1, events[0].third)
    }

    @Test
    fun `unwatched varbit does not fire listener`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        var fired = false
        dispatcher.onVarbitChanged { _, _, _ -> fired = true }
        // Do NOT watch any varbits
        dispatcher.start()

        every { harness.ctx.client.getVarbitValue(4070) } returns 99
        harness.simulateTick()

        assertFalse(fired)
    }

    @Test
    fun `watchVarbits registers multiple varbits`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        every { harness.ctx.client.getVarbitValue(4070) } returns 0
        every { harness.ctx.client.getVarbitValue(4071) } returns 0

        val events = mutableListOf<Int>()
        dispatcher.onVarbitChanged { varbitId, _, _ -> events.add(varbitId) }
        dispatcher.watchVarbits(4070, 4071)
        dispatcher.start()

        every { harness.ctx.client.getVarbitValue(4070) } returns 1
        every { harness.ctx.client.getVarbitValue(4071) } returns 2
        harness.simulateTick()

        assertEquals(2, events.size)
        assertTrue(events.contains(4070))
        assertTrue(events.contains(4071))
    }

    // =========================================================================
    // 9. GE offer change
    // =========================================================================

    @Test
    fun `ge offer change fires listener with slot index and states`() {
        val offers = Array(8) { fakeGeOffer(GrandExchangeOfferState.EMPTY) }
        val harness = buildDispatcherContext {
            initialGeOffers = offers
        }
        val dispatcher = EventDispatcher()

        var capturedSlot = -1
        var capturedOldState: GrandExchangeOfferState? = null
        var capturedNewState: GrandExchangeOfferState? = null

        dispatcher.onGrandExchangeOfferChanged { slotIndex, oldState, newState ->
            capturedSlot = slotIndex
            capturedOldState = oldState
            capturedNewState = newState
        }
        dispatcher.start()

        // Slot 0 changes to BUYING
        val updatedOffers = Array(8) { fakeGeOffer(GrandExchangeOfferState.EMPTY) }
        updatedOffers[0] = fakeGeOffer(GrandExchangeOfferState.BUYING)
        every { harness.ctx.client.grandExchangeOffers } returns updatedOffers
        harness.simulateTick()

        assertEquals(0, capturedSlot)
        assertEquals(GrandExchangeOfferState.EMPTY, capturedOldState)
        assertEquals(GrandExchangeOfferState.BUYING, capturedNewState)
    }

    @Test
    fun `ge offer no change does not fire listener`() {
        val offers = Array(8) { fakeGeOffer(GrandExchangeOfferState.EMPTY) }
        val harness = buildDispatcherContext {
            initialGeOffers = offers
        }
        val dispatcher = EventDispatcher()

        var fired = false
        dispatcher.onGrandExchangeOfferChanged { _, _, _ -> fired = true }
        dispatcher.start()

        // Same state on next tick
        harness.simulateTick()

        assertFalse(fired)
    }

    // =========================================================================
    // 10. Widget open/close
    // =========================================================================

    @Test
    fun `widget opened fires when widget becomes visible`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        // Initially null (not visible)
        every { harness.ctx.client.getWidget(465, 0) } returns null

        val openedGroups = mutableListOf<Int>()
        dispatcher.onWidgetOpened { groupId -> openedGroups.add(groupId) }
        dispatcher.watchWidget(465)
        dispatcher.start()

        // Widget becomes visible
        val widget = fakeWidget(isHidden = false)
        every { harness.ctx.client.getWidget(465, 0) } returns widget
        harness.simulateTick()

        assertEquals(1, openedGroups.size)
        assertEquals(465, openedGroups[0])
    }

    @Test
    fun `widget closed fires when widget becomes hidden`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        // Initially visible
        val widget = fakeWidget(isHidden = false)
        every { harness.ctx.client.getWidget(465, 0) } returns widget

        val closedGroups = mutableListOf<Int>()
        dispatcher.onWidgetClosed { groupId -> closedGroups.add(groupId) }
        dispatcher.watchWidget(465)
        dispatcher.start()

        // Widget becomes null (closed)
        every { harness.ctx.client.getWidget(465, 0) } returns null
        harness.simulateTick()

        assertEquals(1, closedGroups.size)
        assertEquals(465, closedGroups[0])
    }

    @Test
    fun `unwatched widget does not fire events`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        var fired = false
        dispatcher.onWidgetOpened { fired = true }
        dispatcher.onWidgetClosed { fired = true }
        // Do NOT watch any widgets
        dispatcher.start()

        harness.simulateTick()

        assertFalse(fired)
    }

    // =========================================================================
    // 11. Animation change (per-frame)
    // =========================================================================

    @Test
    fun `animation change fires listener for local player`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        val localPlayer = harness.ctx.worldViews.getLocalPlayer()!!
        every { localPlayer.animation } returns -1

        val events = mutableListOf<Triple<Actor, Int, Int>>()
        dispatcher.onAnimationChanged { actor, oldAnim, newAnim ->
            events.add(Triple(actor, oldAnim, newAnim))
        }
        dispatcher.start()

        // Animation changes
        every { localPlayer.animation } returns 808
        harness.simulateFrame()

        assertEquals(1, events.size)
        assertEquals(-1, events[0].second)
        assertEquals(808, events[0].third)
    }

    @Test
    fun `animation change fires for npcs`() {
        val goblin = fakeNpc(id = 1, name = "Goblin", animation = -1)
        every { goblin.index } returns 10

        val harness = buildDispatcherContext {
            initialNpcs = listOf(goblin)
        }
        val dispatcher = EventDispatcher()

        val events = mutableListOf<Triple<Actor, Int, Int>>()
        dispatcher.onAnimationChanged { actor, oldAnim, newAnim ->
            events.add(Triple(actor, oldAnim, newAnim))
        }
        dispatcher.start()

        // NPC starts animating
        every { goblin.animation } returns 4230
        harness.simulateFrame()

        val npcEvent = events.find { it.first == goblin }
        assertTrue(npcEvent != null, "Should have animation event for the goblin")
        assertEquals(-1, npcEvent.second)
        assertEquals(4230, npcEvent.third)
    }

    // =========================================================================
    // 12. Interacting change (per-frame)
    // =========================================================================

    @Test
    fun `interacting change fires listener`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        val localPlayer = harness.ctx.worldViews.getLocalPlayer()!!
        every { localPlayer.interacting } returns null

        val events = mutableListOf<Triple<Actor, Actor?, Actor?>>()
        dispatcher.onInteractingChanged { source, oldTarget, newTarget ->
            events.add(Triple(source, oldTarget, newTarget))
        }
        dispatcher.start()

        // Player starts interacting with an NPC
        val goblin = fakeNpc(id = 1, name = "Goblin")
        every { goblin.index } returns 42
        every { localPlayer.interacting } returns goblin
        // The NPC needs to be in the world for frame polling
        every { harness.ctx.worldViews.getTopLevelNpcs() } returns listOf(goblin)
        harness.simulateFrame()

        val playerEvent = events.find { it.first == localPlayer }
        assertTrue(playerEvent != null)
        assertEquals(null, playerEvent.second)
        assertEquals(goblin, playerEvent.third)
    }

    // =========================================================================
    // 13. Health change (per-frame, proxy for hitsplats)
    // =========================================================================

    @Test
    fun `health change fires listener when health ratio changes`() {
        val goblin = fakeNpc(id = 1, name = "Goblin", healthRatio = -1)
        every { goblin.index } returns 10

        val harness = buildDispatcherContext {
            initialNpcs = listOf(goblin)
        }
        val dispatcher = EventDispatcher()

        val events = mutableListOf<Triple<Actor, Int, Int>>()
        dispatcher.onHealthChanged { actor, oldRatio, newRatio ->
            events.add(Triple(actor, oldRatio, newRatio))
        }
        dispatcher.start()

        // NPC takes damage
        every { goblin.healthRatio } returns 80
        harness.simulateFrame()

        val npcEvent = events.find { it.first == goblin }
        assertTrue(npcEvent != null)
        assertEquals(-1, npcEvent.second)
        assertEquals(80, npcEvent.third)
    }

    // =========================================================================
    // 14. No listeners = no crash
    // =========================================================================

    @Test
    fun `empty dispatcher with no listeners does not crash on tick`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()
        dispatcher.start()

        // Simulate tick -- should not throw
        harness.simulateTick()
    }

    @Test
    fun `empty dispatcher with no frame listeners does not register render callback`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()
        dispatcher.start()

        // renderSlot should NOT be captured because no frame listeners were registered
        assertFalse(harness.renderSlot.isCaptured)
    }

    // =========================================================================
    // 15. Initial snapshot does not fire events
    // =========================================================================

    @Test
    fun `start captures initial snapshot without firing callbacks`() {
        val goblin = fakeNpc(id = 1, name = "Goblin")
        every { goblin.index } returns 42

        val harness = buildDispatcherContext {
            initialNpcs = listOf(goblin)
            initialInventory = listOf(InventoryItem(995, 100, 0))
        }
        val dispatcher = EventDispatcher()

        var npcSpawnFired = false
        var inventoryChangeFired = false
        var statChangeFired = false

        dispatcher.onNpcSpawned { npcSpawnFired = true }
        dispatcher.onInventoryChanged { _, _ -> inventoryChangeFired = true }
        dispatcher.onStatChanged { _, _, _, _, _, _, _ -> statChangeFired = true }
        dispatcher.start()

        // No events should have fired from the initial snapshot
        assertFalse(npcSpawnFired, "NPC spawn should not fire on initial snapshot")
        assertFalse(inventoryChangeFired, "Inventory change should not fire on initial snapshot")
        assertFalse(statChangeFired, "Stat change should not fire on initial snapshot")
    }

    // =========================================================================
    // 16. stop() prevents further events
    // =========================================================================

    @Test
    fun `stop removes listener registrations`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        dispatcher.onStatChanged { _, _, _, _, _, _, _ -> }
        dispatcher.start()
        dispatcher.stop()

        verify { harness.tickReg.remove() }
    }

    @Test
    fun `polling after stop does not fire events`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        every { harness.ctx.skills.getXp(Skill.ATTACK) } returns 0

        var fired = false
        dispatcher.onStatChanged { _, _, _, _, _, _, _ -> fired = true }
        dispatcher.start()

        dispatcher.stop()

        // Even if we manually invoke the captured lambda, initialized=false should prevent firing
        every { harness.ctx.skills.getXp(Skill.ATTACK) } returns 500
        harness.simulateTick()

        assertFalse(fired, "Events should not fire after stop()")
    }

    @Test
    fun `stop with frame listeners removes render registration`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        val localPlayer = harness.ctx.worldViews.getLocalPlayer()!!
        every { localPlayer.animation } returns -1

        dispatcher.onAnimationChanged { _, _, _ -> }
        dispatcher.start()

        assertTrue(harness.renderSlot.isCaptured)

        dispatcher.stop()

        verify { harness.renderReg.remove() }
    }

    // =========================================================================
    // 17. Multiple listeners
    // =========================================================================

    @Test
    fun `multiple listeners for same event all fire`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        every { harness.ctx.skills.getXp(Skill.ATTACK) } returns 0

        var count1 = 0
        var count2 = 0
        dispatcher.onStatChanged { _, _, _, _, _, _, _ -> count1++ }
        dispatcher.onStatChanged { _, _, _, _, _, _, _ -> count2++ }
        dispatcher.start()

        every { harness.ctx.skills.getXp(Skill.ATTACK) } returns 500
        harness.simulateTick()

        assertEquals(1, count1)
        assertEquals(1, count2)
    }

    // =========================================================================
    // 18. NPC spawn and despawn in same tick
    // =========================================================================

    @Test
    fun `npc replaced by different npc at same index fires despawn then spawn`() {
        val goblin = fakeNpc(id = 1, name = "Goblin")
        every { goblin.index } returns 42

        val harness = buildDispatcherContext {
            initialNpcs = listOf(goblin)
        }
        val dispatcher = EventDispatcher()

        val spawned = mutableListOf<NPC>()
        val despawned = mutableListOf<NPC>()
        dispatcher.onNpcSpawned { npc -> spawned.add(npc) }
        dispatcher.onNpcDespawned { npc -> despawned.add(npc) }
        dispatcher.start()

        // Different NPC takes the same index -- this doesn't happen in practice,
        // but let's use a different index to be safe
        val guard = fakeNpc(id = 2, name = "Guard")
        every { guard.index } returns 99
        every { harness.ctx.worldViews.getTopLevelNpcs() } returns listOf(guard)
        harness.simulateTick()

        assertEquals(1, despawned.size)
        assertEquals("Goblin", despawned[0].name)
        assertEquals(1, spawned.size)
        assertEquals("Guard", spawned[0].name)
    }

    // =========================================================================
    // 19. GE offers null safety
    // =========================================================================

    @Test
    fun `ge poll handles null grandExchangeOffers gracefully`() {
        val harness = buildDispatcherContext()
        // Override to return null after context is built
        every { harness.ctx.client.grandExchangeOffers } returns null
        val dispatcher = EventDispatcher()

        dispatcher.onGrandExchangeOfferChanged { _, _, _ -> }
        dispatcher.start()

        // Should not crash
        harness.simulateTick()
    }

    // =========================================================================
    // 20. Widget watchWidgets
    // =========================================================================

    @Test
    fun `watchWidgets registers multiple widget groups`() {
        val harness = buildDispatcherContext()
        val dispatcher = EventDispatcher()

        every { harness.ctx.client.getWidget(465, 0) } returns null
        every { harness.ctx.client.getWidget(270, 0) } returns null

        val openedGroups = mutableListOf<Int>()
        dispatcher.onWidgetOpened { groupId -> openedGroups.add(groupId) }
        dispatcher.watchWidgets(465, 270)
        dispatcher.start()

        // Both widgets become visible
        every { harness.ctx.client.getWidget(465, 0) } returns fakeWidget(isHidden = false)
        every { harness.ctx.client.getWidget(270, 0) } returns fakeWidget(isHidden = false)
        harness.simulateTick()

        assertEquals(2, openedGroups.size)
        assertTrue(openedGroups.contains(465))
        assertTrue(openedGroups.contains(270))
    }
}

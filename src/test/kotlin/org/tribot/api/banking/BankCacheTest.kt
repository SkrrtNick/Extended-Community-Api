package org.tribot.api.banking

import org.tribot.api.ApiContext
import org.tribot.automation.script.ScriptContext
import org.tribot.automation.script.core.widgets.BankItem
import org.tribot.automation.script.core.widgets.Banking
import org.tribot.automation.script.event.Events
import org.tribot.automation.script.event.ListenerRegistration
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BankCacheTest {

    @AfterTest
    fun tearDown() {
        ApiContext.reset()
    }

    // -- Test helpers --

    private class FakeListenerRegistration : ListenerRegistration {
        var removed = false
        override fun remove() { removed = true }
        override fun close() { remove() }
    }

    /**
     * Tracks game tick callbacks registered via onGameTick.
     * Uses a dynamic proxy to avoid needing RuneLite types (GameState, ChatMessage, etc.)
     * on the test classpath.
     */
    private class FakeEventsState {
        val registeredGameTickCallbacks = mutableListOf<() -> Unit>()
        val registrations = mutableListOf<FakeListenerRegistration>()
    }

    private fun createFakeEvents(state: FakeEventsState): Events {
        return java.lang.reflect.Proxy.newProxyInstance(
            Events::class.java.classLoader,
            arrayOf(Events::class.java)
        ) { _, method, args ->
            when (method.name) {
                "onGameTick" -> {
                    @Suppress("UNCHECKED_CAST")
                    val callback = args[0] as () -> Unit
                    state.registeredGameTickCallbacks.add(callback)
                    val reg = FakeListenerRegistration()
                    state.registrations.add(reg)
                    reg
                }
                else -> FakeListenerRegistration()
            }
        } as Events
    }

    private class FakeBanking(
        private var open: Boolean = false,
        private var items: List<BankItem> = emptyList()
    ) : Banking {
        override fun isOpen(): Boolean = open
        override fun getItems(): List<BankItem> = items
        override fun close(): Boolean = true
        override fun contains(itemId: Int): Boolean = items.any { it.id == itemId }
        override fun getCount(itemId: Int): Int = items.filter { it.id == itemId }.sumOf { it.quantity }
        override fun withdraw(itemId: Int, amount: Int): Boolean = true
        override fun withdrawAll(itemId: Int): Boolean = true
        override fun deposit(itemId: Int, amount: Int): Boolean = true
        override fun depositAll(itemId: Int): Boolean = true
        override fun depositAllInventory(): Boolean = true
        override fun depositAllEquipment(): Boolean = true
        override fun enterPin(pin: String): Boolean = true
        override fun isPinScreenOpen(): Boolean = false
    }

    /**
     * Minimal ScriptContext that only provides banking and events via dynamic proxy,
     * avoiding resolution of RuneLite types that aren't on the test classpath.
     */
    private fun fakeContext(
        banking: Banking = FakeBanking(),
        events: Events = createFakeEvents(FakeEventsState())
    ): ScriptContext {
        return java.lang.reflect.Proxy.newProxyInstance(
            ScriptContext::class.java.classLoader,
            arrayOf(ScriptContext::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "getBanking" -> banking
                "getEvents" -> events
                else -> throw UnsupportedOperationException("Not implemented in test fake: ${method.name}")
            }
        } as ScriptContext
    }

    // -- Tests --

    @Test
    fun `initial state is not populated with empty items`() {
        val cache = BankCache()

        assertFalse(cache.isPopulated)
        assertEquals(0, cache.lastUpdateTime)
        assertTrue(cache.getItems().isEmpty())
        assertEquals(0, cache.getTotalCount())
        assertEquals(0, cache.getUniqueItemCount())
    }

    @Test
    fun `refresh when bank is open captures items`() {
        val items = listOf(BankItem(379, 100), BankItem(385, 50))
        val banking = FakeBanking(open = true, items = items)
        ApiContext.init(fakeContext(banking = banking))
        val cache = BankCache()

        cache.refresh()

        assertTrue(cache.isPopulated)
        assertTrue(cache.lastUpdateTime > 0)
        assertEquals(2, cache.getItems().size)
        assertEquals(items, cache.getItems())
    }

    @Test
    fun `refresh when bank is closed does nothing`() {
        val banking = FakeBanking(open = false)
        ApiContext.init(fakeContext(banking = banking))
        val cache = BankCache()

        cache.refresh()

        assertFalse(cache.isPopulated)
        assertTrue(cache.getItems().isEmpty())
    }

    @Test
    fun `contains returns true for present item`() {
        val items = listOf(BankItem(379, 100), BankItem(385, 50))
        val banking = FakeBanking(open = true, items = items)
        ApiContext.init(fakeContext(banking = banking))
        val cache = BankCache()
        cache.refresh()

        assertTrue(cache.contains(379))
        assertTrue(cache.contains(385))
        assertFalse(cache.contains(999))
    }

    @Test
    fun `getCount returns quantity for item`() {
        val items = listOf(BankItem(379, 100), BankItem(385, 50))
        val banking = FakeBanking(open = true, items = items)
        ApiContext.init(fakeContext(banking = banking))
        val cache = BankCache()
        cache.refresh()

        assertEquals(100, cache.getCount(379))
        assertEquals(50, cache.getCount(385))
        assertEquals(0, cache.getCount(999))
    }

    @Test
    fun `getCount sums quantities for duplicate item ids`() {
        val items = listOf(BankItem(379, 100), BankItem(379, 50))
        val banking = FakeBanking(open = true, items = items)
        ApiContext.init(fakeContext(banking = banking))
        val cache = BankCache()
        cache.refresh()

        assertEquals(150, cache.getCount(379))
    }

    @Test
    fun `getTotalCount sums all item quantities`() {
        val items = listOf(BankItem(379, 100), BankItem(385, 50), BankItem(391, 25))
        val banking = FakeBanking(open = true, items = items)
        ApiContext.init(fakeContext(banking = banking))
        val cache = BankCache()
        cache.refresh()

        assertEquals(175, cache.getTotalCount())
    }

    @Test
    fun `getUniqueItemCount returns number of distinct entries`() {
        val items = listOf(BankItem(379, 100), BankItem(385, 50), BankItem(391, 25))
        val banking = FakeBanking(open = true, items = items)
        ApiContext.init(fakeContext(banking = banking))
        val cache = BankCache()
        cache.refresh()

        assertEquals(3, cache.getUniqueItemCount())
    }

    @Test
    fun `invalidate clears cache and resets state`() {
        val items = listOf(BankItem(379, 100))
        val banking = FakeBanking(open = true, items = items)
        ApiContext.init(fakeContext(banking = banking))
        val cache = BankCache()
        cache.refresh()

        assertTrue(cache.isPopulated)
        assertEquals(1, cache.getItems().size)

        cache.invalidate()

        assertFalse(cache.isPopulated)
        assertEquals(0, cache.lastUpdateTime)
        assertTrue(cache.getItems().isEmpty())
    }

    @Test
    fun `startListening registers a game tick listener`() {
        val eventsState = FakeEventsState()
        val events = createFakeEvents(eventsState)
        ApiContext.init(fakeContext(events = events))
        val cache = BankCache()

        val reg = cache.startListening()

        assertNotNull(reg)
        assertEquals(1, eventsState.registeredGameTickCallbacks.size)
    }

    @Test
    fun `startListening callback updates cache when bank is open`() {
        val items = listOf(BankItem(379, 100))
        val banking = FakeBanking(open = true, items = items)
        val eventsState = FakeEventsState()
        val events = createFakeEvents(eventsState)
        ApiContext.init(fakeContext(banking = banking, events = events))
        val cache = BankCache()
        cache.startListening()

        // Simulate a game tick
        eventsState.registeredGameTickCallbacks.last().invoke()

        assertTrue(cache.isPopulated)
        assertEquals(1, cache.getItems().size)
        assertEquals(100, cache.getCount(379))
    }

    @Test
    fun `startListening callback does not update cache when bank is closed`() {
        val banking = FakeBanking(open = false)
        val eventsState = FakeEventsState()
        val events = createFakeEvents(eventsState)
        ApiContext.init(fakeContext(banking = banking, events = events))
        val cache = BankCache()
        cache.startListening()

        // Simulate a game tick with bank closed
        eventsState.registeredGameTickCallbacks.last().invoke()

        assertFalse(cache.isPopulated)
        assertTrue(cache.getItems().isEmpty())
    }

    @Test
    fun `stopListening removes the registered listener`() {
        val eventsState = FakeEventsState()
        val events = createFakeEvents(eventsState)
        ApiContext.init(fakeContext(events = events))
        val cache = BankCache()
        cache.startListening()

        cache.stopListening()

        assertTrue(eventsState.registrations.first().removed)
    }

    @Test
    fun `startListening removes previous listener before registering new one`() {
        val eventsState = FakeEventsState()
        val events = createFakeEvents(eventsState)
        ApiContext.init(fakeContext(events = events))
        val cache = BankCache()

        cache.startListening()
        val firstReg = eventsState.registrations.first()
        assertFalse(firstReg.removed)

        cache.startListening()

        assertTrue(firstReg.removed, "First listener should have been removed")
        assertEquals(2, eventsState.registrations.size)
        assertFalse(eventsState.registrations[1].removed, "Second listener should still be active")
    }

    @Test
    fun `getItems returns a defensive copy`() {
        val items = listOf(BankItem(379, 100))
        val banking = FakeBanking(open = true, items = items)
        ApiContext.init(fakeContext(banking = banking))
        val cache = BankCache()
        cache.refresh()

        val result1 = cache.getItems()
        val result2 = cache.getItems()

        assertEquals(result1, result2)
        assertFalse(result1 === result2, "getItems should return a new list each time")
    }
}

package org.tribot.api.banking

import org.tribot.api.ApiContext
import org.tribot.automation.script.core.widgets.BankItem
import org.tribot.automation.script.event.ListenerRegistration

/**
 * In-memory cache of bank contents. Updated each time the bank is opened.
 * Allows checking bank contents without having the bank open.
 *
 * Usage:
 * ```
 * val cache = BankCache()
 * cache.startListening()  // hooks into game tick to snapshot bank when open
 *
 * // Later, even with bank closed:
 * cache.contains(379)  // do we have lobsters?
 * cache.getCount(379)  // how many?
 * ```
 */
class BankCache {
    private var items: List<BankItem> = emptyList()
    private var lastUpdated: Long = 0
    private var listener: ListenerRegistration? = null

    val isPopulated: Boolean get() = lastUpdated > 0
    val lastUpdateTime: Long get() = lastUpdated

    fun getItems(): List<BankItem> = items.toList()

    fun contains(itemId: Int): Boolean = items.any { it.id == itemId }

    fun getCount(itemId: Int): Int = items.filter { it.id == itemId }.sumOf { it.quantity }

    fun getTotalCount(): Int = items.sumOf { it.quantity }

    fun getUniqueItemCount(): Int = items.size

    /**
     * Manually refresh the cache from the current bank state.
     * Bank must be open for this to capture data.
     */
    fun refresh() {
        val ctx = ApiContext.get()
        if (ctx.banking.isOpen()) {
            items = ctx.banking.getItems().toList()
            lastUpdated = System.currentTimeMillis()
        }
    }

    /**
     * Start automatically updating the cache on each game tick when the bank is open.
     * Returns the listener registration for manual cleanup (also cleaned up by [stopListening]).
     */
    fun startListening(): ListenerRegistration {
        val ctx = ApiContext.get()
        stopListening()
        val reg = ctx.events.onGameTick {
            if (ctx.banking.isOpen()) {
                items = ctx.banking.getItems().toList()
                lastUpdated = System.currentTimeMillis()
            }
        }
        listener = reg
        return reg
    }

    fun stopListening() {
        listener?.remove()
        listener = null
    }

    fun invalidate() {
        items = emptyList()
        lastUpdated = 0
    }
}

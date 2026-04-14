package org.tribot.api.loadout

import org.tribot.api.query.NpcQueryBuilder
import org.tribot.api.query.ObjectQueryBuilder
import org.tribot.api.waiting.Conditions
import org.tribot.api.ApiContext
import org.tribot.automation.script.core.tabs.InventoryItem

/**
 * Manages loadout fulfillment: checking, diffing, and restocking from the bank.
 */
object LoadoutManager {

    /**
     * Checks if the current inventory and equipment satisfy the loadout.
     */
    fun isSatisfied(loadout: Loadout): Boolean {
        return getMissingItems(loadout).isEmpty()
                && getMissingEquipment(loadout).isEmpty()
    }

    /**
     * Returns inventory items needed but missing or with insufficient quantity.
     * Each returned [LoadoutItem] has [LoadoutItem.quantity] set to the deficit.
     */
    fun getMissingItems(loadout: Loadout): List<LoadoutItem> {
        val inventoryItems = ApiContext.get().inventory.getItems()
        return loadout.inventory.mapNotNull { needed ->
            val have = inventoryItems
                .filter { it.id in needed.allIds }
                .sumOf { it.quantity }
            val deficit = needed.quantity - have
            if (deficit > 0) needed.copy(quantity = deficit) else null
        }
    }

    /**
     * Returns inventory items that are NOT part of the loadout.
     */
    fun getUnwantedItems(loadout: Loadout): List<InventoryItem> {
        val wantedIds = loadout.inventory.flatMap { it.allIds }.toSet()
        return ApiContext.get().inventory.getItems().filter { it.id !in wantedIds }
    }

    /**
     * Returns equipment items that are not currently worn.
     * Checks by matching any of the item's IDs against equipped items.
     */
    fun getMissingEquipment(loadout: Loadout): List<LoadoutItem> {
        val equippedItems = ApiContext.get().equipment.getItems()
        return loadout.equipment.filter { needed ->
            equippedItems.none { it.id in needed.allIds }
        }
    }

    /**
     * Full restock loop: open bank, deposit unwanted, withdraw missing, equip gear, close bank.
     *
     * @return true if the loadout is fully satisfied after the operation
     */
    fun fulfill(loadout: Loadout): Boolean {
        val ctx = ApiContext.get()
        // Already good — nothing to do
        if (isSatisfied(loadout)) return true

        // Open the nearest bank
        if (!openBank()) return false

        // Deposit unwanted items
        val unwanted = getUnwantedItems(loadout)
        for (item in unwanted) {
            ctx.banking.depositAll(item.id)
            Conditions.waitUntil(ctx.waiting, 1200) {
                !ctx.inventory.contains(item.id)
            }
        }

        // Withdraw missing items
        val missing = getMissingItems(loadout)
        for (item in missing) {
            val withdrawn = withdrawItem(item)
            if (!withdrawn) return false
        }

        // Close bank before equipping
        ctx.banking.close()
        Conditions.waitUntil(ctx.waiting, 1200) {
            !ctx.banking.isOpen()
        }

        // Equip items that need equipping
        val missingEquip = getMissingEquipment(loadout)
        for (item in missingEquip) {
            equipItem(item)
        }

        return isSatisfied(loadout)
    }

    private fun openBank(): Boolean {
        val ctx = ApiContext.get()
        if (ctx.banking.isOpen()) return true

        val playerLocation = ctx.worldViews.getLocalPlayer()?.worldLocation ?: return false

        // Try object with "Bank" action first
        val bankObject = ObjectQueryBuilder()
            .actions("Bank")
            .withinDistance(20)
            .results()
            .nearest(playerLocation)

        if (bankObject != null) {
            ctx.interaction.click(bankObject, "Bank")
            return Conditions.waitUntil(ctx.waiting, 5000) {
                ctx.banking.isOpen()
            }
        }

        // Try NPC with "Bank" action
        val bankNpc = NpcQueryBuilder()
            .actions("Bank")
            .withinDistance(20)
            .results()
            .nearest(playerLocation)

        if (bankNpc != null) {
            ctx.interaction.click(bankNpc, "Bank")
            return Conditions.waitUntil(ctx.waiting, 5000) {
                ctx.banking.isOpen()
            }
        }

        return false
    }

    private fun withdrawItem(item: LoadoutItem): Boolean {
        val ctx = ApiContext.get()
        // Try primary ID first
        if (ctx.banking.contains(item.itemId)) {
            ctx.banking.withdraw(item.itemId, item.quantity)
            return Conditions.waitUntil(ctx.waiting, 1200) {
                ctx.inventory.contains(item.itemId)
            }
        }

        // Try alternate IDs
        for (altId in item.alternateIds) {
            if (ctx.banking.contains(altId)) {
                ctx.banking.withdraw(altId, item.quantity)
                return Conditions.waitUntil(ctx.waiting, 1200) {
                    ctx.inventory.contains(altId)
                }
            }
        }

        return false
    }

    private fun equipItem(item: LoadoutItem) {
        val ctx = ApiContext.get()
        val actions = listOf("Wield", "Wear", "Equip")
        for (action in actions) {
            val itemId = findInventoryId(item) ?: return
            if (ctx.inventory.clickItem(itemId, action)) {
                Conditions.waitUntil(ctx.waiting, 1200) {
                    ctx.equipment.isEquipped(itemId)
                }
                return
            }
        }
    }

    private fun findInventoryId(item: LoadoutItem): Int? {
        val inventoryItems = ApiContext.get().inventory.getItems()
        return item.allIds.firstOrNull { id -> inventoryItems.any { it.id == id } }
    }
}

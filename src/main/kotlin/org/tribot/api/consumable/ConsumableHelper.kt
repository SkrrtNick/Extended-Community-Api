package org.tribot.api.consumable

import net.runelite.api.Skill
import org.tribot.api.waiting.Conditions
import org.tribot.automation.script.ScriptContext

/**
 * Helper functions for eating food and drinking potions in-game.
 */
object ConsumableHelper {

    /**
     * Eats a food item from the inventory and waits for the HP change.
     *
     * @param ctx the script context
     * @param itemId the item ID to eat
     * @return true if the food was eaten successfully (HP changed or item consumed)
     */
    fun eat(ctx: ScriptContext, itemId: Int): Boolean {
        if (!ctx.inventory.contains(itemId)) return false
        val hpBefore = ctx.skills.getBoostedLevel(Skill.HITPOINTS)
        ctx.inventory.clickItem(itemId, "Eat")
        return Conditions.waitUntil(ctx.waiting, 1800) {
            ctx.skills.getBoostedLevel(Skill.HITPOINTS) != hpBefore || !ctx.inventory.contains(itemId)
        }
    }

    /**
     * Drinks a potion from the inventory and waits for it to be consumed.
     *
     * @param ctx the script context
     * @param itemId the item ID to drink
     * @return true if the potion was drunk successfully (item consumed from inventory)
     */
    fun drink(ctx: ScriptContext, itemId: Int): Boolean {
        if (!ctx.inventory.contains(itemId)) return false
        ctx.inventory.clickItem(itemId, "Drink")
        return Conditions.waitUntil(ctx.waiting, 1800) {
            !ctx.inventory.contains(itemId)
        }
    }

    /**
     * Finds the best food in the inventory by healing amount.
     * Does not consider scaling heals (like Anglerfish) -- uses flat HealEffect only.
     *
     * @param ctx the script context
     * @return the Consumable with the highest heal, or null if no food found
     */
    fun findBestFood(ctx: ScriptContext): Consumable? {
        return ctx.inventory.getItems()
            .mapNotNull { ConsumableDatabase.get(it.id) }
            .filter { it.type == ConsumableType.FOOD || it.type == ConsumableType.COMBO }
            .maxByOrNull { consumable ->
                consumable.effects.filterIsInstance<HealEffect>().maxOfOrNull { it.amount } ?: 0
            }
    }

    /**
     * Calculates the total healing from a consumable's effects for a given max HP.
     * Handles flat heals, percent heals, and scaling heals.
     *
     * @param consumable the consumable to evaluate
     * @param maxHp the player's max hitpoints level
     * @return the total HP that would be healed
     */
    fun calculateHealing(consumable: Consumable, maxHp: Int): Int {
        return consumable.effects.sumOf { effect ->
            when (effect) {
                is HealEffect -> effect.amount
                is PercentHealEffect -> effect.calculateHeal(maxHp)
                is ScalingHealEffect -> effect.calculateHeal(maxHp)
                else -> 0
            }
        }
    }
}

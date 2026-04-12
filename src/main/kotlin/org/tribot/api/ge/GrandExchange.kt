package org.tribot.api.ge

import net.runelite.api.GrandExchangeOfferState
import org.tribot.api.query.NpcQueryBuilder
import org.tribot.api.waiting.Conditions
import org.tribot.automation.script.ScriptContext

object GrandExchange {
    private const val GE_WIDGET_GROUP = 465

    fun getOffers(ctx: ScriptContext): List<Offer> {
        val geOffers = ctx.client.grandExchangeOffers ?: return emptyList()
        return geOffers.mapIndexed { index, offer ->
            Offer(
                slotIndex = index,
                state = offer.state,
                itemId = offer.itemId,
                totalQuantity = offer.totalQuantity,
                quantityFilled = offer.quantitySold,
                price = offer.price,
                totalSpent = offer.spent
            )
        }
    }

    fun getEmptySlotIndex(ctx: ScriptContext): Int? {
        val offers = ctx.client.grandExchangeOffers ?: return null
        return offers.indexOfFirst { it.state == GrandExchangeOfferState.EMPTY }.takeIf { it >= 0 }
    }

    fun getCompletedOffers(ctx: ScriptContext): List<Offer> =
        getOffers(ctx).filter { it.isComplete }

    fun getActiveOffers(ctx: ScriptContext): List<Offer> =
        getOffers(ctx).filter { it.isBuying || it.isSelling }

    fun isOpen(ctx: ScriptContext): Boolean {
        val widget = ctx.client.getWidget(GE_WIDGET_GROUP, 0)
        return widget != null && !widget.isHidden
    }

    fun open(ctx: ScriptContext): Boolean {
        if (isOpen(ctx)) return true
        val clerk = NpcQueryBuilder(ctx)
            .actions("Exchange")
            .withinDistance(15)
            .results()
            .nearest(ctx.worldViews.getLocalPlayer()!!.worldLocation)
            ?: return false
        ctx.interaction.click(clerk, "Exchange")
        return Conditions.waitUntil(ctx.waiting, 5000) { isOpen(ctx) }
    }

    fun close(ctx: ScriptContext): Boolean {
        if (!isOpen(ctx)) return true
        val closeWidget = ctx.client.getWidget(GE_WIDGET_GROUP, 2) ?: return false
        ctx.interaction.click(closeWidget, "Close")
        return Conditions.waitUntil(ctx.waiting, 2000) { !isOpen(ctx) }
    }
}

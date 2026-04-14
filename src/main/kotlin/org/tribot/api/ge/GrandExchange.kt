package org.tribot.api.ge

import net.runelite.api.GrandExchangeOfferState
import org.tribot.api.query.NpcQueryBuilder
import org.tribot.api.waiting.Conditions
import org.tribot.api.ApiContext

object GrandExchange {
    private const val GE_WIDGET_GROUP = 465

    fun getOffers(): List<Offer> {
        val geOffers = ApiContext.get().client.grandExchangeOffers ?: return emptyList()
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

    fun getEmptySlotIndex(): Int? {
        val offers = ApiContext.get().client.grandExchangeOffers ?: return null
        return offers.indexOfFirst { it.state == GrandExchangeOfferState.EMPTY }.takeIf { it >= 0 }
    }

    fun getCompletedOffers(): List<Offer> =
        getOffers().filter { it.isComplete }

    fun getActiveOffers(): List<Offer> =
        getOffers().filter { it.isBuying || it.isSelling }

    fun isOpen(): Boolean {
        val widget = ApiContext.get().client.getWidget(GE_WIDGET_GROUP, 0)
        return widget != null && !widget.isHidden
    }

    fun open(): Boolean {
        val ctx = ApiContext.get()
        if (isOpen()) return true
        val clerk = NpcQueryBuilder()
            .actions("Exchange")
            .withinDistance(15)
            .results()
            .nearest(ctx.worldViews.getLocalPlayer()!!.worldLocation)
            ?: return false
        ctx.interaction.click(clerk, "Exchange")
        return Conditions.waitUntil(ctx.waiting, 5000) { isOpen() }
    }

    fun close(): Boolean {
        val ctx = ApiContext.get()
        if (!isOpen()) return true
        val closeWidget = ctx.client.getWidget(GE_WIDGET_GROUP, 2) ?: return false
        ctx.interaction.click(closeWidget, "Close")
        return Conditions.waitUntil(ctx.waiting, 2000) { !isOpen() }
    }
}

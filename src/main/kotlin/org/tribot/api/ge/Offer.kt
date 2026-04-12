package org.tribot.api.ge

import net.runelite.api.GrandExchangeOfferState

data class Offer(
    val slotIndex: Int,
    val state: GrandExchangeOfferState,
    val itemId: Int,
    val totalQuantity: Int,
    val quantityFilled: Int,
    val price: Int,
    val totalSpent: Int
) {
    val isEmpty: Boolean get() = state == GrandExchangeOfferState.EMPTY
    val isBuying: Boolean get() = state == GrandExchangeOfferState.BUYING
    val isSelling: Boolean get() = state == GrandExchangeOfferState.SELLING
    val isComplete: Boolean get() = state == GrandExchangeOfferState.BOUGHT || state == GrandExchangeOfferState.SOLD
    val isCancelled: Boolean get() = state == GrandExchangeOfferState.CANCELLED_BUY || state == GrandExchangeOfferState.CANCELLED_SELL
    val quantityRemaining: Int get() = totalQuantity - quantityFilled
}

package org.tribot.api.ge

import io.mockk.every
import io.mockk.mockk
import net.runelite.api.GrandExchangeOffer
import net.runelite.api.GrandExchangeOfferState
import net.runelite.api.widgets.Widget
import org.tribot.api.testing.fakeContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GrandExchangeTest {

    private fun fakeOffer(
        state: GrandExchangeOfferState = GrandExchangeOfferState.EMPTY,
        itemId: Int = 0,
        totalQuantity: Int = 0,
        quantitySold: Int = 0,
        price: Int = 0,
        spent: Int = 0
    ): GrandExchangeOffer {
        val offer = mockk<GrandExchangeOffer>(relaxed = true)
        every { offer.state } returns state
        every { offer.itemId } returns itemId
        every { offer.totalQuantity } returns totalQuantity
        every { offer.quantitySold } returns quantitySold
        every { offer.price } returns price
        every { offer.spent } returns spent
        return offer
    }

    private fun buildOfferArray(vararg offers: GrandExchangeOffer): Array<GrandExchangeOffer> {
        val result = Array(8) { fakeOffer() }
        offers.forEachIndexed { index, offer ->
            if (index < 8) result[index] = offer
        }
        return result
    }

    @Test
    fun `getOffers returns all 8 offer slots`() {
        val offers = buildOfferArray(
            fakeOffer(state = GrandExchangeOfferState.BUYING, itemId = 995, totalQuantity = 100, price = 5),
            fakeOffer(state = GrandExchangeOfferState.SELLING, itemId = 453, totalQuantity = 50, price = 10)
        )
        val ctx = fakeContext {
            every { client.grandExchangeOffers } returns offers
        }

        val result = GrandExchange.getOffers(ctx)
        assertEquals(8, result.size)
        assertEquals(GrandExchangeOfferState.BUYING, result[0].state)
        assertEquals(995, result[0].itemId)
        assertEquals(100, result[0].totalQuantity)
        assertEquals(5, result[0].price)
        assertEquals(GrandExchangeOfferState.SELLING, result[1].state)
        assertEquals(453, result[1].itemId)
        // Remaining slots should be empty
        for (i in 2..7) {
            assertTrue(result[i].isEmpty)
        }
    }

    @Test
    fun `getEmptySlot returns first empty slot index`() {
        val offers = buildOfferArray(
            fakeOffer(state = GrandExchangeOfferState.BUYING, itemId = 995),
            fakeOffer(state = GrandExchangeOfferState.SELLING, itemId = 453)
        )
        val ctx = fakeContext {
            every { client.grandExchangeOffers } returns offers
        }

        val index = GrandExchange.getEmptySlotIndex(ctx)
        assertEquals(2, index)
    }

    @Test
    fun `getEmptySlot returns null when all slots used`() {
        val offers = Array(8) {
            fakeOffer(state = GrandExchangeOfferState.BUYING, itemId = it + 1)
        }
        val ctx = fakeContext {
            every { client.grandExchangeOffers } returns offers
        }

        val index = GrandExchange.getEmptySlotIndex(ctx)
        assertNull(index)
    }

    @Test
    fun `getCompletedOffers filters correctly`() {
        val offers = buildOfferArray(
            fakeOffer(state = GrandExchangeOfferState.BOUGHT, itemId = 995),
            fakeOffer(state = GrandExchangeOfferState.BUYING, itemId = 453),
            fakeOffer(state = GrandExchangeOfferState.SOLD, itemId = 556),
            fakeOffer(state = GrandExchangeOfferState.CANCELLED_BUY, itemId = 100)
        )
        val ctx = fakeContext {
            every { client.grandExchangeOffers } returns offers
        }

        val completed = GrandExchange.getCompletedOffers(ctx)
        assertEquals(2, completed.size)
        assertTrue(completed.all { it.isComplete })
        assertEquals(995, completed[0].itemId)
        assertEquals(556, completed[1].itemId)
    }

    @Test
    fun `getActiveOffers filters correctly`() {
        val offers = buildOfferArray(
            fakeOffer(state = GrandExchangeOfferState.BOUGHT, itemId = 995),
            fakeOffer(state = GrandExchangeOfferState.BUYING, itemId = 453),
            fakeOffer(state = GrandExchangeOfferState.SELLING, itemId = 556),
            fakeOffer(state = GrandExchangeOfferState.SOLD, itemId = 100)
        )
        val ctx = fakeContext {
            every { client.grandExchangeOffers } returns offers
        }

        val active = GrandExchange.getActiveOffers(ctx)
        assertEquals(2, active.size)
        assertTrue(active.all { it.isBuying || it.isSelling })
        assertEquals(453, active[0].itemId)
        assertEquals(556, active[1].itemId)
    }

    @Test
    fun `isOpen returns true when GE widget visible`() {
        val widget = mockk<Widget>(relaxed = true)
        every { widget.isHidden } returns false
        val ctx = fakeContext {
            every { client.getWidget(465, 0) } returns widget
        }

        assertTrue(GrandExchange.isOpen(ctx))
    }

    @Test
    fun `isOpen returns false when widget null or hidden`() {
        // null widget case
        val ctx1 = fakeContext {
            every { client.getWidget(465, 0) } returns null
        }
        assertFalse(GrandExchange.isOpen(ctx1))

        // hidden widget case
        val hiddenWidget = mockk<Widget>(relaxed = true)
        every { hiddenWidget.isHidden } returns true
        val ctx2 = fakeContext {
            every { client.getWidget(465, 0) } returns hiddenWidget
        }
        assertFalse(GrandExchange.isOpen(ctx2))
    }
}

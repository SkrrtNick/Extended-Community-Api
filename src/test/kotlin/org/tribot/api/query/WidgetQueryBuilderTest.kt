package org.tribot.api.query

import io.mockk.every
import net.runelite.api.widgets.Widget
import org.tribot.api.ApiContext
import org.tribot.api.testing.*
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WidgetQueryBuilderTest {

    @AfterTest
    fun tearDown() {
        ApiContext.reset()
    }

    /**
     * Sets up a mock context where client.getWidget(groupId, index) returns
     * the provided root widgets sequentially, then null for any further index.
     */
    private fun buildContext(
        groupId: Int,
        rootWidgets: List<Widget>
    ) {
        val ctx = fakeContext {
            for ((i, widget) in rootWidgets.withIndex()) {
                every { client.getWidget(groupId, i) } returns widget
            }
            every { client.getWidget(groupId, rootWidgets.size) } returns null
        }
        ApiContext.init(ctx)
    }

    // --- texts filter ---

    @Test
    fun `texts filter matches exact text`() {
        val w1 = fakeWidget(text = "Continue")
        val w2 = fakeWidget(text = "Cancel")
        buildContext(groupId = 219, rootWidgets = listOf(w1, w2))

        val results = WidgetQueryBuilder().group(219).texts("Continue").results()
        assertEquals(1, results.size)
        assertEquals("Continue", results.first()?.text)
    }

    @Test
    fun `texts filter with multiple values`() {
        val w1 = fakeWidget(text = "Continue")
        val w2 = fakeWidget(text = "Cancel")
        val w3 = fakeWidget(text = "Other")
        buildContext(groupId = 219, rootWidgets = listOf(w1, w2, w3))

        val results = WidgetQueryBuilder().group(219).texts("Continue", "Cancel").results()
        assertEquals(2, results.size)
    }

    @Test
    fun `texts filter excludes null text`() {
        val w1 = fakeWidget(text = null)
        val w2 = fakeWidget(text = "Hello")
        buildContext(groupId = 10, rootWidgets = listOf(w1, w2))

        val results = WidgetQueryBuilder().group(10).texts("Hello").results()
        assertEquals(1, results.size)
        assertEquals("Hello", results.first()?.text)
    }

    // --- textContains filter ---

    @Test
    fun `textContains filter matches partial text`() {
        val w1 = fakeWidget(text = "Click here to continue")
        val w2 = fakeWidget(text = "Cancel action")
        buildContext(groupId = 219, rootWidgets = listOf(w1, w2))

        val results = WidgetQueryBuilder().group(219).textContains("continue").results()
        assertEquals(1, results.size)
        assertEquals("Click here to continue", results.first()?.text)
    }

    @Test
    fun `textContains is case insensitive`() {
        val w1 = fakeWidget(text = "HELLO WORLD")
        buildContext(groupId = 5, rootWidgets = listOf(w1))

        val results = WidgetQueryBuilder().group(5).textContains("hello").results()
        assertEquals(1, results.size)
    }

    // --- actions filter ---

    @Test
    fun `actions filter matches by action`() {
        val w1 = fakeWidget(actions = arrayOf("Close", null))
        val w2 = fakeWidget(actions = arrayOf("Open", null))
        buildContext(groupId = 300, rootWidgets = listOf(w1, w2))

        val results = WidgetQueryBuilder().group(300).actions("Close").results()
        assertEquals(1, results.size)
    }

    @Test
    fun `actions filter excludes null actions array`() {
        val w1 = fakeWidget(actions = null)
        val w2 = fakeWidget(actions = arrayOf("Talk"))
        buildContext(groupId = 300, rootWidgets = listOf(w1, w2))

        val results = WidgetQueryBuilder().group(300).actions("Talk").results()
        assertEquals(1, results.size)
    }

    // --- actionContains filter ---

    @Test
    fun `actionContains filter matches partial action text`() {
        val w1 = fakeWidget(actions = arrayOf("Talk-to banker", null))
        val w2 = fakeWidget(actions = arrayOf("Close window", null))
        buildContext(groupId = 300, rootWidgets = listOf(w1, w2))

        val results = WidgetQueryBuilder().group(300).actionContains("banker").results()
        assertEquals(1, results.size)
    }

    // --- visible/hidden filters ---

    @Test
    fun `visible filter only returns non-hidden widgets`() {
        val w1 = fakeWidget(text = "visible", isHidden = false, isSelfHidden = false)
        val w2 = fakeWidget(text = "hidden", isHidden = true, isSelfHidden = false)
        val w3 = fakeWidget(text = "self-hidden", isHidden = false, isSelfHidden = true)
        buildContext(groupId = 100, rootWidgets = listOf(w1, w2, w3))

        val results = WidgetQueryBuilder().group(100).visible().results()
        assertEquals(1, results.size)
        assertEquals("visible", results.first()?.text)
    }

    @Test
    fun `hidden filter returns hidden or self-hidden widgets`() {
        val w1 = fakeWidget(text = "visible", isHidden = false, isSelfHidden = false)
        val w2 = fakeWidget(text = "hidden", isHidden = true, isSelfHidden = false)
        val w3 = fakeWidget(text = "self-hidden", isHidden = false, isSelfHidden = true)
        buildContext(groupId = 100, rootWidgets = listOf(w1, w2, w3))

        val results = WidgetQueryBuilder().group(100).hidden().results()
        assertEquals(2, results.size)
    }

    // --- spriteIds filter ---

    @Test
    fun `spriteIds filter matches by sprite ID`() {
        val w1 = fakeWidget(spriteId = 573)
        val w2 = fakeWidget(spriteId = 999)
        buildContext(groupId = 50, rootWidgets = listOf(w1, w2))

        val results = WidgetQueryBuilder().group(50).spriteIds(573).results()
        assertEquals(1, results.size)
        assertEquals(573, results.first()?.spriteId)
    }

    @Test
    fun `spriteIds filter with multiple IDs`() {
        val w1 = fakeWidget(spriteId = 573)
        val w2 = fakeWidget(spriteId = 999)
        val w3 = fakeWidget(spriteId = 100)
        buildContext(groupId = 50, rootWidgets = listOf(w1, w2, w3))

        val results = WidgetQueryBuilder().group(50).spriteIds(573, 100).results()
        assertEquals(2, results.size)
    }

    // --- itemIds filter ---

    @Test
    fun `itemIds filter matches by item ID`() {
        val w1 = fakeWidget(itemId = 995)
        val w2 = fakeWidget(itemId = 526)
        buildContext(groupId = 149, rootWidgets = listOf(w1, w2))

        val results = WidgetQueryBuilder().group(149).itemIds(995).results()
        assertEquals(1, results.size)
        assertEquals(995, results.first()?.itemId)
    }

    // --- types filter ---

    @Test
    fun `types filter matches by widget type`() {
        val w1 = fakeWidget(type = 4)
        val w2 = fakeWidget(type = 5)
        buildContext(groupId = 200, rootWidgets = listOf(w1, w2))

        val results = WidgetQueryBuilder().group(200).types(4).results()
        assertEquals(1, results.size)
        assertEquals(4, results.first()?.type)
    }

    // --- hasText filter ---

    @Test
    fun `hasText filter matches widgets with non-empty text`() {
        val w1 = fakeWidget(text = "Hello")
        val w2 = fakeWidget(text = "")
        val w3 = fakeWidget(text = null)
        buildContext(groupId = 10, rootWidgets = listOf(w1, w2, w3))

        val results = WidgetQueryBuilder().group(10).hasText().results()
        assertEquals(1, results.size)
        assertEquals("Hello", results.first()?.text)
    }

    // --- childIndex filter ---

    @Test
    fun `childIndex filter matches by index`() {
        val w1 = fakeWidget(index = 0, text = "first")
        val w2 = fakeWidget(index = 1, text = "second")
        val w3 = fakeWidget(index = 2, text = "third")
        buildContext(groupId = 10, rootWidgets = listOf(w1, w2, w3))

        val results = WidgetQueryBuilder().group(10).childIndex(0, 2).results()
        assertEquals(2, results.size)
    }

    // --- group is required ---

    @Test
    fun `returns empty results when group is not set`() {
        ApiContext.init(fakeContext())
        val results = WidgetQueryBuilder().texts("Hello").results()
        assertTrue(results.isEmpty)
        assertEquals(0, results.size)
    }

    // --- children traversal ---

    @Test
    fun `searches dynamic children by default`() {
        val child1 = fakeWidget(text = "Child A")
        val child2 = fakeWidget(text = "Child B")
        val root = fakeWidget(
            text = "Root",
            dynamicChildren = arrayOf(child1, child2)
        )
        buildContext(groupId = 10, rootWidgets = listOf(root))

        val results = WidgetQueryBuilder().group(10).texts("Child A").results()
        assertEquals(1, results.size)
        assertEquals("Child A", results.first()?.text)
    }

    @Test
    fun `searches nested children by default`() {
        val child = fakeWidget(text = "Nested Child")
        val root = fakeWidget(
            text = "Root",
            nestedChildren = arrayOf(child)
        )
        buildContext(groupId = 10, rootWidgets = listOf(root))

        val results = WidgetQueryBuilder().group(10).texts("Nested Child").results()
        assertEquals(1, results.size)
    }

    @Test
    fun `includeGrandchildren searches one more level deep`() {
        val grandchild = fakeWidget(text = "Grandchild")
        val child = fakeWidget(
            text = "Child",
            dynamicChildren = arrayOf(grandchild)
        )
        val root = fakeWidget(
            text = "Root",
            dynamicChildren = arrayOf(child)
        )
        buildContext(groupId = 10, rootWidgets = listOf(root))

        // Without grandchildren, grandchild is not found
        val withoutGrand = WidgetQueryBuilder().group(10).texts("Grandchild").results()
        assertEquals(0, withoutGrand.size)

        // With grandchildren, grandchild is found
        val withGrand = WidgetQueryBuilder().group(10)
            .includeGrandchildren()
            .texts("Grandchild")
            .results()
        assertEquals(1, withGrand.size)
        assertEquals("Grandchild", withGrand.first()?.text)
    }

    // --- combined filters ---

    @Test
    fun `multiple filters combine with AND logic`() {
        val w1 = fakeWidget(text = "Hello", spriteId = 100, isHidden = false, isSelfHidden = false)
        val w2 = fakeWidget(text = "Hello", spriteId = 200, isHidden = false, isSelfHidden = false)
        val w3 = fakeWidget(text = "World", spriteId = 100, isHidden = false, isSelfHidden = false)
        buildContext(groupId = 10, rootWidgets = listOf(w1, w2, w3))

        val results = WidgetQueryBuilder()
            .group(10)
            .texts("Hello")
            .spriteIds(100)
            .visible()
            .results()
        assertEquals(1, results.size)
        assertEquals("Hello", results.first()?.text)
        assertEquals(100, results.first()?.spriteId)
    }
}

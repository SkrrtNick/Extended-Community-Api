package org.tribot.api.query

import net.runelite.api.coords.WorldPoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class QueryResultsTest {

    @Test
    fun `first returns first element`() {
        val results = QueryResults(listOf("a", "b", "c"))
        assertEquals("a", results.first())
    }

    @Test
    fun `first returns null when empty`() {
        val results = QueryResults(emptyList<String>())
        assertNull(results.first())
    }

    @Test
    fun `last returns last element`() {
        val results = QueryResults(listOf("a", "b", "c"))
        assertEquals("c", results.last())
    }

    @Test
    fun `last returns null when empty`() {
        val results = QueryResults(emptyList<String>())
        assertNull(results.last())
    }

    @Test
    fun `random returns element from list`() {
        val items = listOf("a", "b", "c")
        val results = QueryResults(items)
        val random = results.random()
        assertTrue(random in items)
    }

    @Test
    fun `random returns null when empty`() {
        val results = QueryResults(emptyList<String>())
        assertNull(results.random())
    }

    @Test
    fun `get returns element at index`() {
        val results = QueryResults(listOf("a", "b", "c"))
        assertEquals("b", results.get(1))
    }

    @Test
    fun `get returns null for out of bounds index`() {
        val results = QueryResults(listOf("a", "b"))
        assertNull(results.get(5))
    }

    @Test
    fun `get returns null for negative index`() {
        val results = QueryResults(listOf("a"))
        assertNull(results.get(-1))
    }

    @Test
    fun `limit returns new results with at most count elements`() {
        val results = QueryResults(listOf("a", "b", "c", "d"))
        val limited = results.limit(2)
        assertEquals(2, limited.size)
        assertEquals("a", limited.first())
        assertEquals("b", limited.last())
    }

    @Test
    fun `limit with count larger than size returns all`() {
        val results = QueryResults(listOf("a", "b"))
        val limited = results.limit(10)
        assertEquals(2, limited.size)
    }

    @Test
    fun `sort returns sorted results`() {
        val results = QueryResults(listOf(3, 1, 2))
        val sorted = results.sort(compareBy { it })
        assertEquals(listOf(1, 2, 3), sorted.asList())
    }

    @Test
    fun `isEmpty returns true for empty results`() {
        val results = QueryResults(emptyList<String>())
        assertTrue(results.isEmpty)
    }

    @Test
    fun `isEmpty returns false for non-empty results`() {
        val results = QueryResults(listOf("a"))
        assertFalse(results.isEmpty)
    }

    @Test
    fun `size returns correct count`() {
        val results = QueryResults(listOf("a", "b", "c"))
        assertEquals(3, results.size)
    }

    @Test
    fun `asList returns underlying list`() {
        val items = listOf("a", "b")
        val results = QueryResults(items)
        assertEquals(items, results.asList())
    }

    @Test
    fun `iterator allows iteration`() {
        val items = listOf("a", "b", "c")
        val results = QueryResults(items)
        val collected = mutableListOf<String>()
        for (item in results) {
            collected.add(item)
        }
        assertEquals(items, collected)
    }

    // LocatableQueryResults tests

    @Test
    fun `nearest returns closest element`() {
        val origin = WorldPoint(3200, 3200, 0)
        val points = listOf(
            WorldPoint(3210, 3200, 0), // distance 10
            WorldPoint(3202, 3200, 0), // distance 2
            WorldPoint(3205, 3200, 0)  // distance 5
        )
        val results = LocatableQueryResults(points) { it }
        val nearest = results.nearest(origin)
        assertEquals(WorldPoint(3202, 3200, 0), nearest)
    }

    @Test
    fun `nearest returns null when empty`() {
        val results = LocatableQueryResults(emptyList<WorldPoint>()) { it }
        assertNull(results.nearest(WorldPoint(3200, 3200, 0)))
    }

    @Test
    fun `furthest returns furthest element`() {
        val origin = WorldPoint(3200, 3200, 0)
        val points = listOf(
            WorldPoint(3210, 3200, 0), // distance 10
            WorldPoint(3202, 3200, 0), // distance 2
            WorldPoint(3205, 3200, 0)  // distance 5
        )
        val results = LocatableQueryResults(points) { it }
        val furthest = results.furthest(origin)
        assertEquals(WorldPoint(3210, 3200, 0), furthest)
    }

    @Test
    fun `furthest returns null when empty`() {
        val results = LocatableQueryResults(emptyList<WorldPoint>()) { it }
        assertNull(results.furthest(WorldPoint(3200, 3200, 0)))
    }

    @Test
    fun `sortByDistance returns sorted results`() {
        val origin = WorldPoint(3200, 3200, 0)
        val points = listOf(
            WorldPoint(3210, 3200, 0), // distance 10
            WorldPoint(3202, 3200, 0), // distance 2
            WorldPoint(3205, 3200, 0)  // distance 5
        )
        val results = LocatableQueryResults(points) { it }
        val sorted = results.sortByDistance(origin)
        assertEquals(
            listOf(
                WorldPoint(3202, 3200, 0),
                WorldPoint(3205, 3200, 0),
                WorldPoint(3210, 3200, 0)
            ),
            sorted.asList()
        )
    }

    @Test
    fun `sortByDistance returns LocatableQueryResults`() {
        val origin = WorldPoint(3200, 3200, 0)
        val points = listOf(WorldPoint(3210, 3200, 0))
        val results = LocatableQueryResults(points) { it }
        val sorted = results.sortByDistance(origin)
        // Verify it's still a LocatableQueryResults by calling nearest
        assertEquals(WorldPoint(3210, 3200, 0), sorted.nearest(origin))
    }
}

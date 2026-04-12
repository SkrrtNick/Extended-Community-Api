package org.tribot.api.query

import net.runelite.api.coords.WorldPoint

/**
 * Query results for entities that have a world position.
 * Adds distance-based methods: nearest, furthest, sortByDistance.
 */
class LocatableQueryResults<T>(
    items: List<T>,
    private val positionOf: (T) -> WorldPoint
) : QueryResults<T>(items) {

    fun nearest(origin: WorldPoint): T? =
        asList().minByOrNull { positionOf(it).distanceTo(origin) }

    fun furthest(origin: WorldPoint): T? =
        asList().maxByOrNull { positionOf(it).distanceTo(origin) }

    fun sortByDistance(origin: WorldPoint): LocatableQueryResults<T> =
        LocatableQueryResults(
            asList().sortedBy { positionOf(it).distanceTo(origin) },
            positionOf
        )
}

package org.tribot.api.requirements

import net.runelite.api.coords.WorldPoint
import org.tribot.api.ApiContext

/**
 * Requires the player to be inside (or outside) a rectangular zone defined
 * by a south-west and north-east [WorldPoint].
 */
class ZoneRequirement(
    val southWest: WorldPoint,
    val northEast: WorldPoint,
    val checkInZone: Boolean = true,
    private val name: String = "Zone (${southWest.x},${southWest.y})-(${northEast.x},${northEast.y})"
) : Requirement {

    override fun check(): Boolean {
        val pos = ApiContext.get().worldViews.getLocalPlayer()?.worldLocation ?: return false
        val inZone = pos.x in southWest.x..northEast.x &&
            pos.y in southWest.y..northEast.y &&
            pos.plane in southWest.plane..northEast.plane
        return if (checkInZone) inZone else !inZone
    }

    override val displayText: String get() = if (checkInZone) "In $name" else "Not in $name"
}

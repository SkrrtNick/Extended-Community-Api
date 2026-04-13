package org.tribot.api.magic

import org.tribot.automation.script.ScriptContext

/**
 * Staves and tomes that provide unlimited runes when equipped.
 *
 * Item IDs sourced from OSRS Wiki and validated against local Inferno project data.
 *
 * @property itemIds All item ID variants for this staff (e.g. normal + ornament kit)
 * @property providedRunes Rune types this staff provides unlimited of when equipped
 */
enum class Staff(
    val itemIds: List<Int>,
    val providedRunes: List<RuneType>
) {
    // Basic elemental staves
    STAFF_OF_AIR(listOf(1381), listOf(RuneType.AIR)),
    STAFF_OF_WATER(listOf(1383), listOf(RuneType.WATER)),
    STAFF_OF_EARTH(listOf(1385), listOf(RuneType.EARTH)),
    STAFF_OF_FIRE(listOf(1387), listOf(RuneType.FIRE)),

    // Elemental battlestaves
    AIR_BATTLESTAFF(listOf(1397), listOf(RuneType.AIR)),
    WATER_BATTLESTAFF(listOf(1395), listOf(RuneType.WATER)),
    EARTH_BATTLESTAFF(listOf(1399), listOf(RuneType.EARTH)),
    FIRE_BATTLESTAFF(listOf(1393), listOf(RuneType.FIRE)),

    // Combination battlestaves
    DUST_BATTLESTAFF(listOf(20736), listOf(RuneType.AIR, RuneType.EARTH)),
    LAVA_BATTLESTAFF(listOf(3053, 21198), listOf(RuneType.EARTH, RuneType.FIRE)),
    MIST_BATTLESTAFF(listOf(20730), listOf(RuneType.AIR, RuneType.WATER)),
    MUD_BATTLESTAFF(listOf(6562), listOf(RuneType.WATER, RuneType.EARTH)),
    SMOKE_BATTLESTAFF(listOf(11998), listOf(RuneType.AIR, RuneType.FIRE)),
    STEAM_BATTLESTAFF(listOf(11787, 12795), listOf(RuneType.WATER, RuneType.FIRE)),

    // Mystic elemental staves
    MYSTIC_AIR_STAFF(listOf(1405), listOf(RuneType.AIR)),
    MYSTIC_WATER_STAFF(listOf(1403), listOf(RuneType.WATER)),
    MYSTIC_EARTH_STAFF(listOf(1407), listOf(RuneType.EARTH)),
    MYSTIC_FIRE_STAFF(listOf(1401), listOf(RuneType.FIRE)),

    // Mystic combination staves
    MYSTIC_DUST_STAFF(listOf(20739), listOf(RuneType.AIR, RuneType.EARTH)),
    MYSTIC_LAVA_STAFF(listOf(3054, 21200), listOf(RuneType.EARTH, RuneType.FIRE)),
    MYSTIC_MIST_STAFF(listOf(20733), listOf(RuneType.AIR, RuneType.WATER)),
    MYSTIC_MUD_STAFF(listOf(6563), listOf(RuneType.WATER, RuneType.EARTH)),
    MYSTIC_SMOKE_STAFF(listOf(12000), listOf(RuneType.AIR, RuneType.FIRE)),
    MYSTIC_STEAM_STAFF(listOf(11789, 12796), listOf(RuneType.WATER, RuneType.FIRE)),

    // Tomes (shield slot — provide unlimited runes when charged)
    TOME_OF_FIRE(listOf(20714), listOf(RuneType.FIRE)),
    TOME_OF_WATER(listOf(25574), listOf(RuneType.WATER)),
    TOME_OF_EARTH(listOf(30064), listOf(RuneType.EARTH)),

    // Special weapons with unlimited runes
    KODAI_WAND(listOf(21006), listOf(RuneType.WATER)),
    TWINFLAME_STAFF(listOf(30634), listOf(RuneType.FIRE, RuneType.WATER));

    companion object {
        /**
         * Returns all staffs/tomes currently equipped.
         */
        fun getEquipped(ctx: ScriptContext): List<Staff> {
            val equippedIds = ctx.equipment.getItems().map { it.id }.toSet()
            return entries.filter { staff -> staff.itemIds.any { it in equippedIds } }
        }

        /**
         * Returns the set of rune types provided by all currently equipped staffs/tomes.
         */
        fun getProvidedRunes(ctx: ScriptContext): Set<RuneType> =
            getEquipped(ctx).flatMap { it.providedRunes }.toSet()
    }
}

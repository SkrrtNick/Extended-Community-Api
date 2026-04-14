package org.tribot.api.requirements

import org.tribot.api.ApiContext
import org.tribot.automation.script.core.tabs.PrayerType

/**
 * Requires a specific prayer to be active (or inactive).
 */
class PrayerRequirement(
    val prayer: PrayerType,
    val active: Boolean = true
) : Requirement {

    override fun check(): Boolean {
        val isActive = ApiContext.get().prayer.isActive(prayer)
        return isActive == active
    }

    override val displayText: String get() = "${prayer.name} ${if (active) "active" else "inactive"}"
}

package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext
import org.tribot.automation.script.core.tabs.PrayerType

/**
 * Requires a specific prayer to be active (or inactive).
 */
class PrayerRequirement(
    val prayer: PrayerType,
    val active: Boolean = true
) : Requirement {

    override fun check(ctx: ScriptContext): Boolean {
        val isActive = ctx.prayer.isActive(prayer)
        return isActive == active
    }

    override val displayText: String get() = "${prayer.name} ${if (active) "active" else "inactive"}"
}

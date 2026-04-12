package org.tribot.api.waiting

import org.tribot.api.playersense.PlayerProfile
import org.tribot.api.playersense.ProfileProperty
import org.tribot.automation.script.util.Waiting

object Conditions {

    fun waitUntil(
        waiting: Waiting,
        timeoutMs: Long,
        pollMs: Long = 100,
        condition: () -> Boolean
    ): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return true
            val remaining = deadline - System.currentTimeMillis()
            if (remaining <= 0) break
            waiting.sleep(pollMs.coerceAtMost(remaining))
        }
        return false
    }

    fun waitUntil(
        waiting: Waiting,
        profile: PlayerProfile,
        timeoutMs: Long,
        pollMs: Long = 100,
        condition: () -> Boolean
    ): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return true
            val remaining = deadline - System.currentTimeMillis()
            if (remaining <= 0) break
            val humanizedPoll = profile.generate(
                ProfileProperty.ACTION_VARIANCE,
                (pollMs * 0.7).toLong(),
                (pollMs * 1.5).toLong()
            )
            waiting.sleep(humanizedPoll.coerceAtMost(remaining))
        }
        return false
    }

    fun sleepRange(waiting: Waiting, profile: PlayerProfile, minMs: Long, maxMs: Long) {
        val duration = profile.generate(ProfileProperty.REACTION_TIME, minMs, maxMs)
        waiting.sleep(duration)
    }

    fun sleep(waiting: Waiting, ms: Long) {
        waiting.sleep(ms)
    }
}

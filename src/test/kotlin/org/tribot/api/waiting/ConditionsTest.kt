package org.tribot.api.waiting

import io.mockk.mockk
import io.mockk.verify
import org.tribot.api.preferences.PlayerProfile
import org.tribot.automation.script.util.Waiting
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConditionsTest {

    private val waiting = mockk<Waiting>(relaxed = true)

    @Test
    fun `waitUntil returns true when condition passes immediately`() {
        val result = Conditions.waitUntil(waiting, timeoutMs = 5000, pollMs = 100) { true }
        assertTrue(result)
    }

    @Test
    fun `waitUntil returns false on timeout`() {
        val result = Conditions.waitUntil(waiting, timeoutMs = 200, pollMs = 50) { false }
        assertFalse(result)
    }

    @Test
    fun `waitUntil returns true when condition becomes true`() {
        var counter = 0
        val result = Conditions.waitUntil(waiting, timeoutMs = 5000, pollMs = 50) {
            counter++
            counter >= 3
        }
        assertTrue(result)
        assertEquals(3, counter)
    }

    @Test
    fun `waitUntil calls sleep between polls`() {
        var counter = 0
        Conditions.waitUntil(waiting, timeoutMs = 5000, pollMs = 100) {
            counter++
            counter >= 2
        }
        verify(atLeast = 1) { waiting.sleep(any()) }
    }

    @Test
    fun `sleepRange generates value within bounds`() {
        val profile = PlayerProfile.default()
        Conditions.sleepRange(waiting, profile, 100L, 200L)
        verify { waiting.sleep(match { it in 100L..200L }) }
    }

    @Test
    fun `waitUntil with profile applies humanized polling`() {
        val profile = PlayerProfile.default()
        var counter = 0
        val result = Conditions.waitUntil(waiting, profile, timeoutMs = 5000, pollMs = 100) {
            counter++
            counter >= 2
        }
        assertTrue(result)
        verify(atLeast = 1) { waiting.sleep(any()) }
    }

    @Test
    fun `sleep delegates to waiting`() {
        Conditions.sleep(waiting, 500)
        verify { waiting.sleep(500) }
    }
}

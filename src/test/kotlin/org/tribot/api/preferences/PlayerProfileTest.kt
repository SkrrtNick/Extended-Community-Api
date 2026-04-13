package org.tribot.api.preferences

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlayerProfileTest {

    @Test
    fun `same seed produces same profile`() {
        val a = PlayerProfile.fromSeed("account1")
        val b = PlayerProfile.fromSeed("account1")
        assertEquals(a[ProfileProperty.REACTION_TIME], b[ProfileProperty.REACTION_TIME])
        assertEquals(a[ProfileProperty.MOUSE_SPEED], b[ProfileProperty.MOUSE_SPEED])
        assertEquals(a[ProfileProperty.IDLE_DURATION], b[ProfileProperty.IDLE_DURATION])
    }

    @Test
    fun `different seeds produce different profiles`() {
        val a = PlayerProfile.fromSeed("account1")
        val b = PlayerProfile.fromSeed("account2")
        val allSame = ProfileProperty.entries.all { a[it] == b[it] }
        assertTrue(!allSame, "Different seeds should produce different profiles")
    }

    @Test
    fun `values fall within property bounds`() {
        val profile = PlayerProfile.fromSeed("testAccount")
        for (prop in ProfileProperty.entries) {
            val value = profile[prop]
            assertTrue(value >= prop.min, "${prop.name} value $value below min ${prop.min}")
            assertTrue(value <= prop.max, "${prop.name} value $value above max ${prop.max}")
        }
    }

    @Test
    fun `override replaces generated value`() {
        val profile = PlayerProfile.fromSeed("account1")
            .withOverride(ProfileProperty.REACTION_TIME, 0.5)
        assertEquals(0.5, profile[ProfileProperty.REACTION_TIME])
    }

    @Test
    fun `override does not affect other properties`() {
        val original = PlayerProfile.fromSeed("account1")
        val overridden = original.withOverride(ProfileProperty.REACTION_TIME, 0.5)
        assertEquals(original[ProfileProperty.MOUSE_SPEED], overridden[ProfileProperty.MOUSE_SPEED])
    }

    @Test
    fun `generate returns value in range using profile`() {
        val profile = PlayerProfile.fromSeed("account1")
        repeat(100) {
            val value = profile.generate(ProfileProperty.REACTION_TIME, 100L, 300L)
            assertTrue(value in 100L..300L, "Generated value $value out of range 100..300")
        }
    }

    @Test
    fun `profiles with different reaction times bias differently`() {
        val slow = PlayerProfile.fromSeed("account1")
            .withOverride(ProfileProperty.REACTION_TIME, 1.0)
        val fast = PlayerProfile.fromSeed("account1")
            .withOverride(ProfileProperty.REACTION_TIME, 0.0)

        val slowAvg = (1..1000).map { slow.generate(ProfileProperty.REACTION_TIME, 100L, 500L) }.average()
        val fastAvg = (1..1000).map { fast.generate(ProfileProperty.REACTION_TIME, 100L, 500L) }.average()
        assertTrue(slowAvg > fastAvg, "Slow profile ($slowAvg) should average higher than fast ($fastAvg)")
    }

    @Test
    fun `default profile uses midpoint values`() {
        val profile = PlayerProfile.default()
        for (prop in ProfileProperty.entries) {
            val value = profile[prop]
            val expected = (prop.min + prop.max) / 2.0
            assertEquals(expected, value, 0.001, "${prop.name} default should be midpoint")
        }
    }
}

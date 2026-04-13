# Community API Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a suite of higher-level APIs on top of the TribotRS automation-sdk, providing script authors with player profiling, conditional waiting, RuneMate-style entity queries, a composable requirements system, a bank-and-restock loadout manager, and a Grand Exchange API.

**Architecture:** Extension library published via JitPack. All APIs take `ScriptContext` as their entry point. Player profile feeds humanized variance into waiting and other systems. Query builders wrap `WorldViews` + `Definitions` in a fluent RuneMate-style API. Requirements provide composable game-state checks. Loadout and GE build on top of queries and requirements.

**Tech Stack:** Kotlin 2.3.10, Java 21, automation-sdk (latest), RuneLite client API (transitive compileOnly), JUnit 5 + Mockito/MockK for testing.

**Dependency Order:**
```
#1 Player Profile ──┐
                     ├──> #2 Waiting API
                     │         │
                     │         v
                     ├──> #3 Query APIs
                     │         │
                     │         v
                     └──> #4 Requirements API
                               │
                          ┌────┴────┐
                          v         v
                    #5 Loadout    #6 GE API
```

**Package structure:**
```
src/main/kotlin/org/tribot/api/
├── playersense/
│   ├── PlayerProfile.kt
│   └── ProfileProperty.kt
├── waiting/
│   └── Conditions.kt
├── query/
│   ├── QueryBuilder.kt          (abstract base)
│   ├── QueryResults.kt
│   ├── LocatableQueryResults.kt
│   ├── NpcQueryBuilder.kt
│   ├── ObjectQueryBuilder.kt
│   ├── GroundItemQueryBuilder.kt
│   ├── PlayerQueryBuilder.kt
│   ├── InventoryQueryBuilder.kt
│   └── BankQueryBuilder.kt
├── requirements/
│   ├── Requirement.kt           (interface + abstract base)
│   ├── LogicType.kt
│   ├── CompositeRequirement.kt
│   ├── ItemRequirement.kt
│   ├── EquipmentRequirement.kt
│   ├── SkillRequirement.kt
│   ├── QuestRequirement.kt
│   ├── VarbitRequirement.kt
│   ├── VarplayerRequirement.kt
│   ├── ZoneRequirement.kt
│   ├── CombatLevelRequirement.kt
│   ├── PrayerRequirement.kt
│   ├── FreeSlotRequirement.kt
│   └── WidgetRequirement.kt
├── loadout/
│   ├── Loadout.kt
│   └── LoadoutManager.kt
└── ge/
    ├── GrandExchange.kt
    ├── Offer.kt
    └── PriceLookup.kt

src/test/kotlin/org/tribot/api/
├── testing/
│   └── Fakes.kt                 (shared fake implementations of SDK interfaces)
├── playersense/
│   └── PlayerProfileTest.kt
├── waiting/
│   └── ConditionsTest.kt
├── query/
│   ├── QueryResultsTest.kt
│   ├── NpcQueryBuilderTest.kt
│   ├── ObjectQueryBuilderTest.kt
│   ├── GroundItemQueryBuilderTest.kt
│   ├── InventoryQueryBuilderTest.kt
│   └── BankQueryBuilderTest.kt
├── requirements/
│   ├── CompositeRequirementTest.kt
│   ├── ItemRequirementTest.kt
│   ├── SkillRequirementTest.kt
│   └── VarbitRequirementTest.kt
├── loadout/
│   └── LoadoutManagerTest.kt
└── ge/
    └── GrandExchangeTest.kt
```

**Testing note:** The automation-sdk is entirely interfaces — no implementations. Tests will use MockK to mock `ScriptContext` and its sub-interfaces. We'll also create lightweight fakes in `testing/Fakes.kt` for commonly used interfaces to keep tests readable.

---

## Task 0: Project Setup — Add MockK and test infrastructure

**Files:**
- Modify: `build.gradle.kts`
- Create: `src/test/kotlin/org/tribot/api/testing/Fakes.kt`

**Step 1: Add MockK dependency**

In `build.gradle.kts`, add to dependencies:

```kotlin
dependencies {
    api("com.github.TribotRS:automation-sdk:latest.release")
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.16")
}
```

**Step 2: Create shared test fakes**

Create `src/test/kotlin/org/tribot/api/testing/Fakes.kt`:

```kotlin
package org.tribot.api.testing

import io.mockk.every
import io.mockk.mockk
import net.runelite.api.*
import net.runelite.api.coords.WorldPoint
import org.tribot.automation.script.ScriptContext
import org.tribot.automation.script.core.WorldViews
import org.tribot.automation.script.core.GroundItem
import org.tribot.automation.script.core.definition.*
import org.tribot.automation.script.core.tabs.*
import org.tribot.automation.script.core.widgets.Banking
import org.tribot.automation.script.util.Waiting

/**
 * Creates a mock ScriptContext with sensible defaults.
 * Individual tests can override specific behaviors.
 */
fun fakeContext(block: ScriptContext.() -> Unit = {}): ScriptContext {
    val ctx = mockk<ScriptContext>(relaxed = true)
    ctx.block()
    return ctx
}

/**
 * Creates a mock NPC with the given properties.
 */
fun fakeNpc(
    id: Int = 1,
    name: String = "Npc",
    worldLocation: WorldPoint = WorldPoint(3200, 3200, 0),
    combatLevel: Int = 1,
    animation: Int = -1,
    interacting: Actor? = null,
    healthRatio: Int = -1,
    healthScale: Int = -1
): NPC {
    val npc = mockk<NPC>(relaxed = true)
    every { npc.id } returns id
    every { npc.name } returns name
    every { npc.worldLocation } returns worldLocation
    every { npc.combatLevel } returns combatLevel
    every { npc.animation } returns animation
    every { npc.interacting } returns interacting
    every { npc.healthRatio } returns healthRatio
    every { npc.healthScale } returns healthScale
    return npc
}

/**
 * Creates a mock Player with the given properties.
 */
fun fakePlayer(
    name: String = "Player",
    worldLocation: WorldPoint = WorldPoint(3200, 3200, 0),
    combatLevel: Int = 70,
    animation: Int = -1
): Player {
    val player = mockk<Player>(relaxed = true)
    every { player.name } returns name
    every { player.worldLocation } returns worldLocation
    every { player.combatLevel } returns combatLevel
    every { player.animation } returns animation
    return player
}

/**
 * Creates a mock NpcDefinition.
 */
fun fakeNpcDef(
    id: Int = 1,
    name: String = "Npc",
    combatLevel: Int = 1,
    actions: List<String?> = listOf("Attack", null, null, null, null)
): NpcDefinition {
    val def = mockk<NpcDefinition>(relaxed = true)
    every { def.id } returns id
    every { def.name } returns name
    every { def.combatLevel } returns combatLevel
    every { def.actions } returns actions
    return def
}

/**
 * Creates a mock ObjectDefinition.
 */
fun fakeObjectDef(
    id: Int = 1,
    name: String = "Object",
    actions: List<String?> = listOf("Use", null, null, null, null)
): ObjectDefinition {
    val def = mockk<ObjectDefinition>(relaxed = true)
    every { def.id } returns id
    every { def.name } returns name
    every { def.actions } returns actions
    return def
}

/**
 * Creates a mock ItemDefinition.
 */
fun fakeItemDef(
    id: Int = 1,
    name: String = "Item",
    stackable: Boolean = false,
    tradable: Boolean = true,
    members: Boolean = false,
    noted: Boolean = false,
    inventoryActions: List<String?> = listOf("Use", null, null, "Drop", null)
): ItemDefinition {
    val def = mockk<ItemDefinition>(relaxed = true)
    every { def.id } returns id
    every { def.name } returns name
    every { def.stackable } returns stackable
    every { def.tradable } returns tradable
    every { def.members } returns members
    every { def.inventoryActions } returns inventoryActions
    every { def.isNoted() } returns noted
    return def
}
```

**Step 3: Verify build compiles and tests run**

Run: `cd /Users/nick/Tribot/Community-Api && ./gradlew test`
Expected: BUILD SUCCESSFUL (no tests yet, but dependencies resolve)

**Step 4: Commit**

```bash
git add build.gradle.kts src/test/
git commit -m "chore: add MockK and shared test fakes"
```

---

## Task 1: Player Profile / Player Sense System

**Files:**
- Create: `src/main/kotlin/org/tribot/api/playersense/ProfileProperty.kt`
- Create: `src/main/kotlin/org/tribot/api/playersense/PlayerProfile.kt`
- Test: `src/test/kotlin/org/tribot/api/playersense/PlayerProfileTest.kt`

### Step 1: Write tests for PlayerProfile

Create `src/test/kotlin/org/tribot/api/playersense/PlayerProfileTest.kt`:

```kotlin
package org.tribot.api.preferences

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
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
        // Statistically near-impossible for all properties to match
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
            .withOverride(ProfileProperty.REACTION_TIME, 1.0) // max
        val fast = PlayerProfile.fromSeed("account1")
            .withOverride(ProfileProperty.REACTION_TIME, 0.0) // min

        // Generate many samples and compare averages
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
```

**Step 2: Run tests to verify they fail**

Run: `cd /Users/nick/Tribot/Community-Api && ./gradlew test`
Expected: FAIL — classes don't exist yet.

**Step 3: Implement ProfileProperty**

Create `src/main/kotlin/org/tribot/api/playersense/ProfileProperty.kt`:

```kotlin
package org.tribot.api.preferences

/**
 * Behavioral properties that vary per player profile.
 *
 * Each property is a normalized value between [min] and [max] (typically 0.0 to 1.0).
 * The value represents where on the spectrum a player falls — 0.0 is one extreme,
 * 1.0 is the other.
 *
 * @property min Minimum normalized value
 * @property max Maximum normalized value
 * @property description Human-readable description of what this property affects
 */
enum class ProfileProperty(
    val min: Double,
    val max: Double,
    val description: String
) {
    /** How quickly the player reacts to events. 0.0 = fast, 1.0 = slow. */
    REACTION_TIME(0.0, 1.0, "Reaction speed to game events"),

    /** Preferred mouse movement speed. 0.0 = slow, 1.0 = fast. */
    MOUSE_SPEED(0.0, 1.0, "Mouse movement speed preference"),

    /** How long the player idles between actions. 0.0 = short, 1.0 = long. */
    IDLE_DURATION(0.0, 1.0, "Duration of idle pauses between actions"),

    /** How often the player performs extra/unnecessary actions. 0.0 = never, 1.0 = often. */
    EXTRA_ACTION_FREQUENCY(0.0, 1.0, "Frequency of unnecessary extra actions"),

    /** Tendency to misclick. 0.0 = precise, 1.0 = clumsy. */
    MISCLICK_RATE(0.0, 0.1, "Probability of misclicking"),

    /** How much variance in timing between repeated actions. 0.0 = consistent, 1.0 = erratic. */
    ACTION_VARIANCE(0.0, 1.0, "Variance in timing between repeated actions"),

    /** Preferred camera movement style bias. 0.0 = keyboard, 1.0 = mouse. */
    CAMERA_STYLE(0.0, 1.0, "Camera control preference: keyboard vs mouse"),

    /** How often the player checks tabs (inventory, skills, etc.). 0.0 = rarely, 1.0 = often. */
    TAB_CHECK_FREQUENCY(0.0, 1.0, "Frequency of checking game tabs"),

    /** How fatigued the player's behavior becomes over time. 0.0 = consistent, 1.0 = degrades fast. */
    FATIGUE_RATE(0.0, 1.0, "Rate at which behavior degrades over time"),

    /** Preferred interaction distance. 0.0 = close, 1.0 = max range. */
    INTERACTION_DISTANCE(0.0, 1.0, "Preferred distance for interacting with entities")
}
```

**Step 4: Implement PlayerProfile**

Create `src/main/kotlin/org/tribot/api/playersense/PlayerProfile.kt`:

```kotlin
package org.tribot.api.preferences

import java.util.Random

/**
 * A per-account behavioral profile that produces consistent, human-like variance.
 *
 * Generated deterministically from a seed (e.g. account name) so the same account
 * always behaves the same way. Individual properties can be overridden.
 *
 * Usage:
 * ```
 * val profile = PlayerProfile.fromSeed("myAccount")
 * val reactionMs = profile.generate(ProfileProperty.REACTION_TIME, 100L, 300L)
 * ```
 */
class PlayerProfile private constructor(
    private val values: Map<ProfileProperty, Double>,
    private val overrides: Map<ProfileProperty, Double>
) {

    /**
     * Gets the normalized value (between property min and max) for the given property.
     * Returns the override if one is set, otherwise the seed-generated value.
     */
    operator fun get(property: ProfileProperty): Double =
        overrides[property] ?: values[property]
            ?: ((property.min + property.max) / 2.0)

    /**
     * Returns a new profile with the given property overridden.
     */
    fun withOverride(property: ProfileProperty, value: Double): PlayerProfile =
        PlayerProfile(values, overrides + (property to value.coerceIn(property.min, property.max)))

    /**
     * Returns a new profile with the given override removed.
     */
    fun clearOverride(property: ProfileProperty): PlayerProfile =
        PlayerProfile(values, overrides - property)

    /**
     * Generates a value in [rangeLow]..[rangeHigh], biased by the profile's value
     * for the given property. The property value (0.0–1.0) shifts the distribution
     * center within the range.
     *
     * @param property The profile property to use for biasing
     * @param rangeLow Minimum output value (inclusive)
     * @param rangeHigh Maximum output value (inclusive)
     * @return A biased random value within the range
     */
    fun generate(property: ProfileProperty, rangeLow: Long, rangeHigh: Long): Long {
        val bias = this[property]
        val normalized = (bias - property.min) / (property.max - property.min)
        val range = rangeHigh - rangeLow
        // Use gaussian centered on bias point, clamped to range
        val center = rangeLow + (range * normalized)
        val stdDev = range * 0.15
        val value = center + (threadLocalRandom.nextGaussian() * stdDev)
        return value.toLong().coerceIn(rangeLow, rangeHigh)
    }

    companion object {
        private val threadLocalRandom = ThreadLocal.withInitial { Random() }

        /**
         * Creates a profile deterministically from the given seed string.
         * Same seed always produces the same profile.
         */
        fun fromSeed(seed: String): PlayerProfile {
            val rng = Random(seed.hashCode().toLong())
            val values = ProfileProperty.entries.associateWith { prop ->
                prop.min + (rng.nextDouble() * (prop.max - prop.min))
            }
            return PlayerProfile(values, emptyMap())
        }

        /**
         * Creates a profile with all properties at their midpoint values.
         */
        fun default(): PlayerProfile {
            val values = ProfileProperty.entries.associateWith { prop ->
                (prop.min + prop.max) / 2.0
            }
            return PlayerProfile(values, emptyMap())
        }
    }
}
```

**Step 5: Run tests**

Run: `cd /Users/nick/Tribot/Community-Api && ./gradlew test`
Expected: ALL PASS

**Step 6: Commit**

```bash
git add src/
git commit -m "feat: add player profile / player sense system"
```

---

## Task 2: Conditional Waiting / Polling API

**Files:**
- Create: `src/main/kotlin/org/tribot/api/waiting/Conditions.kt`
- Test: `src/test/kotlin/org/tribot/api/waiting/ConditionsTest.kt`

### Step 1: Write tests

Create `src/test/kotlin/org/tribot/api/waiting/ConditionsTest.kt`:

```kotlin
package org.tribot.api.waiting

import io.mockk.every
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
        // Should have slept once between the two polls
        verify(atLeast = 1) { waiting.sleep(any()) }
    }

    @Test
    fun `sleepRange generates value within bounds`() {
        val profile = PlayerProfile.default()
        // Can't easily test the actual sleep call since Waiting is mocked,
        // but we can verify it doesn't throw and calls sleep
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
```

**Step 2: Run tests to verify they fail**

Run: `cd /Users/nick/Tribot/Community-Api && ./gradlew test`
Expected: FAIL — `Conditions` class doesn't exist.

**Step 3: Implement Conditions**

Create `src/main/kotlin/org/tribot/api/waiting/Conditions.kt`:

```kotlin
package org.tribot.api.waiting

import org.tribot.api.preferences.PlayerProfile
import org.tribot.api.preferences.ProfileProperty
import org.tribot.automation.script.util.Waiting

/**
 * Conditional waiting and polling utilities.
 *
 * Provides [waitUntil] for blocking until a condition is met (or timeout),
 * and [sleepRange] for humanized random-duration sleeps.
 */
object Conditions {

    /**
     * Blocks until [condition] returns true, polling every [pollMs] milliseconds.
     * Returns false if [timeoutMs] elapses without the condition passing.
     *
     * @param waiting The SDK waiting instance for sleeping
     * @param timeoutMs Maximum time to wait in milliseconds
     * @param pollMs Time between condition checks in milliseconds
     * @param condition The condition to check
     * @return true if condition passed, false if timed out
     */
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

    /**
     * Blocks until [condition] returns true, with humanized poll intervals
     * based on the player profile.
     *
     * @param waiting The SDK waiting instance for sleeping
     * @param profile Player profile for humanized variance
     * @param timeoutMs Maximum time to wait in milliseconds
     * @param pollMs Base time between condition checks (varied by profile)
     * @param condition The condition to check
     * @return true if condition passed, false if timed out
     */
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

    /**
     * Sleeps for a random duration between [minMs] and [maxMs],
     * biased by the player profile's reaction time.
     */
    fun sleepRange(waiting: Waiting, profile: PlayerProfile, minMs: Long, maxMs: Long) {
        val duration = profile.generate(ProfileProperty.REACTION_TIME, minMs, maxMs)
        waiting.sleep(duration)
    }

    /**
     * Sleeps for the given duration.
     */
    fun sleep(waiting: Waiting, ms: Long) {
        waiting.sleep(ms)
    }
}
```

**Step 4: Run tests**

Run: `cd /Users/nick/Tribot/Community-Api && ./gradlew test`
Expected: ALL PASS

**Step 5: Commit**

```bash
git add src/
git commit -m "feat: add conditional waiting / polling API"
```

---

## Task 3: Query APIs — RuneMate-style Builders

This is the largest task. We build it bottom-up: base classes first, then concrete builders.

**Files:**
- Create: `src/main/kotlin/org/tribot/api/query/QueryResults.kt`
- Create: `src/main/kotlin/org/tribot/api/query/LocatableQueryResults.kt`
- Create: `src/main/kotlin/org/tribot/api/query/QueryBuilder.kt`
- Create: `src/main/kotlin/org/tribot/api/query/NpcQueryBuilder.kt`
- Create: `src/main/kotlin/org/tribot/api/query/ObjectQueryBuilder.kt`
- Create: `src/main/kotlin/org/tribot/api/query/GroundItemQueryBuilder.kt`
- Create: `src/main/kotlin/org/tribot/api/query/PlayerQueryBuilder.kt`
- Create: `src/main/kotlin/org/tribot/api/query/InventoryQueryBuilder.kt`
- Create: `src/main/kotlin/org/tribot/api/query/BankQueryBuilder.kt`
- Test: `src/test/kotlin/org/tribot/api/query/QueryResultsTest.kt`
- Test: `src/test/kotlin/org/tribot/api/query/NpcQueryBuilderTest.kt`
- Test: `src/test/kotlin/org/tribot/api/query/ObjectQueryBuilderTest.kt`
- Test: `src/test/kotlin/org/tribot/api/query/GroundItemQueryBuilderTest.kt`
- Test: `src/test/kotlin/org/tribot/api/query/InventoryQueryBuilderTest.kt`
- Test: `src/test/kotlin/org/tribot/api/query/BankQueryBuilderTest.kt`

### Step 1: Write QueryResults tests

Create `src/test/kotlin/org/tribot/api/query/QueryResultsTest.kt`:

```kotlin
package org.tribot.api.query

import net.runelite.api.coords.WorldPoint
import org.tribot.api.testing.fakeNpc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
    fun `limit returns at most N elements`() {
        val results = QueryResults(listOf(1, 2, 3, 4, 5))
        assertEquals(3, results.limit(3).size)
    }

    @Test
    fun `isEmpty and size work correctly`() {
        assertTrue(QueryResults(emptyList<Int>()).isEmpty)
        assertEquals(3, QueryResults(listOf(1, 2, 3)).size)
    }

    @Test
    fun `asList returns all elements`() {
        val results = QueryResults(listOf(1, 2, 3))
        assertEquals(listOf(1, 2, 3), results.asList())
    }

    @Test
    fun `random returns element from results`() {
        val items = listOf(1, 2, 3)
        val results = QueryResults(items)
        val random = results.random()
        assertTrue(random in items)
    }

    @Test
    fun `random returns null when empty`() {
        assertNull(QueryResults(emptyList<Int>()).random())
    }

    @Test
    fun `sort applies comparator`() {
        val results = QueryResults(listOf(3, 1, 2))
        assertEquals(listOf(1, 2, 3), results.sort(compareBy { it }).asList())
    }

    @Test
    fun `locatable results nearest returns closest to point`() {
        val origin = WorldPoint(3200, 3200, 0)
        val close = fakeNpc(name = "Close", worldLocation = WorldPoint(3201, 3200, 0))
        val far = fakeNpc(name = "Far", worldLocation = WorldPoint(3210, 3210, 0))

        val results = LocatableQueryResults(listOf(far, close)) { it.worldLocation }
        val nearest = results.nearest(origin)
        assertEquals("Close", nearest?.name)
    }

    @Test
    fun `locatable results sortByDistance orders correctly`() {
        val origin = WorldPoint(3200, 3200, 0)
        val close = fakeNpc(name = "Close", worldLocation = WorldPoint(3201, 3200, 0))
        val mid = fakeNpc(name = "Mid", worldLocation = WorldPoint(3205, 3200, 0))
        val far = fakeNpc(name = "Far", worldLocation = WorldPoint(3210, 3210, 0))

        val results = LocatableQueryResults(listOf(far, close, mid)) { it.worldLocation }
        val sorted = results.sortByDistance(origin)
        assertEquals(listOf("Close", "Mid", "Far"), sorted.asList().map { it.name })
    }
}
```

**Step 2: Implement QueryResults and LocatableQueryResults**

Create `src/main/kotlin/org/tribot/api/query/QueryResults.kt`:

```kotlin
package org.tribot.api.query

/**
 * Wrapper around query results with convenience methods for accessing and
 * manipulating the result set.
 *
 * @param T The entity type
 */
open class QueryResults<T>(private val items: List<T>) : Iterable<T> {

    val size: Int get() = items.size

    val isEmpty: Boolean get() = items.isEmpty()

    fun first(): T? = items.firstOrNull()

    fun last(): T? = items.lastOrNull()

    fun random(): T? = if (items.isEmpty()) null else items.random()

    fun get(index: Int): T? = items.getOrNull(index)

    fun limit(count: Int): QueryResults<T> = QueryResults(items.take(count))

    fun sort(comparator: Comparator<T>): QueryResults<T> =
        QueryResults(items.sortedWith(comparator))

    fun asList(): List<T> = items.toList()

    override fun iterator(): Iterator<T> = items.iterator()
}
```

Create `src/main/kotlin/org/tribot/api/query/LocatableQueryResults.kt`:

```kotlin
package org.tribot.api.query

import net.runelite.api.coords.WorldPoint

/**
 * Query results for entities that have a world position.
 * Adds distance-based methods: [nearest], [furthest], [sortByDistance].
 *
 * @param T The entity type
 * @param positionOf Function to extract the WorldPoint from an entity
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
```

**Step 3: Run QueryResults tests**

Run: `cd /Users/nick/Tribot/Community-Api && ./gradlew test`
Expected: ALL PASS

**Step 4: Commit**

```bash
git add src/
git commit -m "feat: add QueryResults and LocatableQueryResults"
```

### Step 5: Write NpcQueryBuilder tests

Create `src/test/kotlin/org/tribot/api/query/NpcQueryBuilderTest.kt`:

```kotlin
package org.tribot.api.query

import io.mockk.every
import io.mockk.mockk
import net.runelite.api.NPC
import net.runelite.api.coords.WorldPoint
import org.tribot.api.testing.*
import org.tribot.automation.script.ScriptContext
import org.tribot.automation.script.core.definition.Definitions
import org.tribot.automation.script.core.definition.NpcDefinition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NpcQueryBuilderTest {

    private val cow = fakeNpc(id = 1, name = "Cow", worldLocation = WorldPoint(3200, 3200, 0), combatLevel = 2)
    private val guard = fakeNpc(id = 2, name = "Guard", worldLocation = WorldPoint(3210, 3210, 0), combatLevel = 21)
    private val goblin = fakeNpc(id = 3, name = "Goblin", worldLocation = WorldPoint(3202, 3202, 0), combatLevel = 5)
    private val cowDef = fakeNpcDef(id = 1, name = "Cow", actions = listOf("Attack", null, null, null, null))
    private val guardDef = fakeNpcDef(id = 2, name = "Guard", actions = listOf("Attack", "Talk-to", null, null, null))
    private val goblinDef = fakeNpcDef(id = 3, name = "Goblin", actions = listOf("Attack", null, null, null, null))

    private fun builder(): NpcQueryBuilder {
        val ctx = fakeContext()
        every { ctx.worldViews.getTopLevelNpcs() } returns listOf(cow, guard, goblin)
        every { ctx.definitions.getNpc(1) } returns cowDef
        every { ctx.definitions.getNpc(2) } returns guardDef
        every { ctx.definitions.getNpc(3) } returns goblinDef
        every { ctx.worldViews.getLocalPlayer() } returns fakePlayer(worldLocation = WorldPoint(3200, 3200, 0))
        return NpcQueryBuilder(ctx)
    }

    @Test
    fun `no filters returns all npcs`() {
        val results = builder().results()
        assertEquals(3, results.size)
    }

    @Test
    fun `names filters by exact name`() {
        val results = builder().names("Cow").results()
        assertEquals(1, results.size)
        assertEquals("Cow", results.first()?.name)
    }

    @Test
    fun `names with multiple values matches any`() {
        val results = builder().names("Cow", "Goblin").results()
        assertEquals(2, results.size)
    }

    @Test
    fun `ids filters by npc id`() {
        val results = builder().ids(2).results()
        assertEquals(1, results.size)
        assertEquals("Guard", results.first()?.name)
    }

    @Test
    fun `actions filters by available action`() {
        val results = builder().actions("Talk-to").results()
        assertEquals(1, results.size)
        assertEquals("Guard", results.first()?.name)
    }

    @Test
    fun `withinDistance filters by euclidean distance from player`() {
        // Player at 3200,3200. Cow at 3200,3200 (dist 0), Goblin at 3202,3202 (~2.8), Guard at 3210,3210 (~14.1)
        val results = builder().withinDistance(5).results()
        assertEquals(2, results.size)
    }

    @Test
    fun `filter applies custom predicate`() {
        val results = builder().filter { it.combatLevel > 10 }.results()
        assertEquals(1, results.size)
        assertEquals("Guard", results.first()?.name)
    }

    @Test
    fun `results nearest returns closest to player`() {
        val ctx = fakeContext()
        every { ctx.worldViews.getTopLevelNpcs() } returns listOf(cow, guard, goblin)
        every { ctx.definitions.getNpc(any()) } returns cowDef
        every { ctx.worldViews.getLocalPlayer() } returns fakePlayer(worldLocation = WorldPoint(3200, 3200, 0))

        val nearest = NpcQueryBuilder(ctx).results().nearest(WorldPoint(3200, 3200, 0))
        assertEquals("Cow", nearest?.name)
    }

    @Test
    fun `chaining multiple filters works`() {
        val results = builder()
            .names("Cow", "Guard", "Goblin")
            .withinDistance(5)
            .filter { it.combatLevel < 10 }
            .results()
        assertEquals(2, results.size) // Cow (lvl 2) and Goblin (lvl 5), both within 5 tiles
    }

    @Test
    fun `animating filters by animation state`() {
        val animatingCow = fakeNpc(id = 1, name = "Cow", animation = 123)
        val idleCow = fakeNpc(id = 4, name = "Cow", animation = -1)
        val ctx = fakeContext()
        every { ctx.worldViews.getTopLevelNpcs() } returns listOf(animatingCow, idleCow)
        every { ctx.definitions.getNpc(any()) } returns cowDef
        every { ctx.worldViews.getLocalPlayer() } returns fakePlayer()

        val results = NpcQueryBuilder(ctx).animating().results()
        assertEquals(1, results.size)
    }

    @Test
    fun `notInCombat filters out npcs being interacted with`() {
        val fightingCow = fakeNpc(id = 1, name = "Cow", interacting = fakePlayer(), healthRatio = 50, healthScale = 100)
        val idleCow = fakeNpc(id = 4, name = "Cow")
        val ctx = fakeContext()
        every { ctx.worldViews.getTopLevelNpcs() } returns listOf(fightingCow, idleCow)
        every { ctx.definitions.getNpc(any()) } returns cowDef
        every { ctx.worldViews.getLocalPlayer() } returns fakePlayer()

        val results = NpcQueryBuilder(ctx).notInCombat().results()
        assertEquals(1, results.size)
    }
}
```

### Step 6: Implement QueryBuilder base and NpcQueryBuilder

Create `src/main/kotlin/org/tribot/api/query/QueryBuilder.kt`:

```kotlin
package org.tribot.api.query

/**
 * Abstract base for all query builders. Collects filters and applies them
 * when [results] is called.
 *
 * Follows the RuneMate pattern: reusable builders with fluent filter methods
 * and a terminal [results] call.
 *
 * @param T The entity type being queried
 * @param B The concrete builder type (for fluent returns)
 */
@Suppress("UNCHECKED_CAST")
abstract class QueryBuilder<T, B : QueryBuilder<T, B>> {

    protected val filters = mutableListOf<(T) -> Boolean>()

    /**
     * Adds a custom predicate filter. Entities that don't match are excluded.
     */
    fun filter(predicate: (T) -> Boolean): B {
        filters.add(predicate)
        return this as B
    }

    /**
     * Executes the query: fetches entities from the source, applies all filters,
     * and returns the results.
     */
    abstract fun results(): QueryResults<T>

    /**
     * Fetches the raw entity list from the game.
     */
    protected abstract fun fetchEntities(): List<T>

    /**
     * Applies all collected filters to the entity list.
     */
    protected fun applyFilters(entities: List<T>): List<T> =
        entities.filter { entity -> filters.all { filter -> filter(entity) } }
}
```

Create `src/main/kotlin/org/tribot/api/query/NpcQueryBuilder.kt`:

```kotlin
package org.tribot.api.query

import net.runelite.api.NPC
import net.runelite.api.coords.WorldPoint
import org.tribot.automation.script.ScriptContext

/**
 * Fluent query builder for finding NPCs in the game world.
 *
 * Usage:
 * ```
 * val banker = NpcQueryBuilder(ctx)
 *     .names("Banker")
 *     .actions("Bank")
 *     .withinDistance(10)
 *     .results()
 *     .nearest(ctx.worldViews.getLocalPlayer()!!.worldLocation)
 * ```
 */
class NpcQueryBuilder(private val ctx: ScriptContext) : QueryBuilder<NPC, NpcQueryBuilder>() {

    fun names(vararg names: String): NpcQueryBuilder = filter { npc ->
        val def = ctx.definitions.getNpc(npc.id)
        def != null && def.name in names
    }

    fun ids(vararg ids: Int): NpcQueryBuilder = filter { npc ->
        npc.id in ids.toSet()
    }

    fun actions(vararg actions: String): NpcQueryBuilder = filter { npc ->
        val def = ctx.definitions.getNpc(npc.id)
        def != null && def.actions.any { it in actions }
    }

    fun withinDistance(maxDistance: Int): NpcQueryBuilder = filter { npc ->
        val player = ctx.worldViews.getLocalPlayer() ?: return@filter false
        npc.worldLocation.distanceTo(player.worldLocation) <= maxDistance
    }

    fun animating(): NpcQueryBuilder = filter { it.animation != -1 }

    fun notAnimating(): NpcQueryBuilder = filter { it.animation == -1 }

    fun notInCombat(): NpcQueryBuilder = filter { npc ->
        npc.interacting == null && npc.healthRatio == -1
    }

    fun inCombat(): NpcQueryBuilder = filter { npc ->
        npc.interacting != null || npc.healthRatio != -1
    }

    fun interactingWithMe(): NpcQueryBuilder = filter { npc ->
        val player = ctx.worldViews.getLocalPlayer()
        npc.interacting == player
    }

    fun minLevel(min: Int): NpcQueryBuilder = filter { it.combatLevel >= min }

    fun maxLevel(max: Int): NpcQueryBuilder = filter { it.combatLevel <= max }

    override fun fetchEntities(): List<NPC> = ctx.worldViews.getTopLevelNpcs()

    override fun results(): LocatableQueryResults<NPC> =
        LocatableQueryResults(applyFilters(fetchEntities())) { it.worldLocation }
}
```

**Step 7: Run tests**

Run: `cd /Users/nick/Tribot/Community-Api && ./gradlew test`
Expected: ALL PASS

**Step 8: Commit**

```bash
git add src/
git commit -m "feat: add QueryBuilder base and NpcQueryBuilder"
```

### Step 9: Implement ObjectQueryBuilder

Create `src/test/kotlin/org/tribot/api/query/ObjectQueryBuilderTest.kt`:

```kotlin
package org.tribot.api.query

import io.mockk.every
import io.mockk.mockk
import net.runelite.api.TileObject
import net.runelite.api.coords.WorldPoint
import org.tribot.api.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ObjectQueryBuilderTest {

    private fun fakeObject(id: Int, location: WorldPoint): TileObject {
        val obj = mockk<TileObject>(relaxed = true)
        every { obj.id } returns id
        every { obj.worldLocation } returns location
        return obj
    }

    @Test
    fun `names filters by object definition name`() {
        val oak = fakeObject(1, WorldPoint(3200, 3200, 0))
        val willow = fakeObject(2, WorldPoint(3205, 3205, 0))
        val oakDef = fakeObjectDef(id = 1, name = "Oak tree", actions = listOf("Chop down", null, null, null, null))
        val willowDef = fakeObjectDef(id = 2, name = "Willow tree", actions = listOf("Chop down", null, null, null, null))

        val ctx = fakeContext()
        every { ctx.worldViews.getTopLevelObjects() } returns listOf(oak, willow)
        every { ctx.definitions.getObject(1) } returns oakDef
        every { ctx.definitions.getObject(2) } returns willowDef
        every { ctx.worldViews.getLocalPlayer() } returns fakePlayer(worldLocation = WorldPoint(3200, 3200, 0))

        val results = ObjectQueryBuilder(ctx).names("Oak tree").results()
        assertEquals(1, results.size)
    }

    @Test
    fun `actions filters by available action`() {
        val bank = fakeObject(1, WorldPoint(3200, 3200, 0))
        val door = fakeObject(2, WorldPoint(3205, 3205, 0))
        val bankDef = fakeObjectDef(id = 1, name = "Bank booth", actions = listOf("Bank", "Collect", null, null, null))
        val doorDef = fakeObjectDef(id = 2, name = "Door", actions = listOf("Open", null, null, null, null))

        val ctx = fakeContext()
        every { ctx.worldViews.getTopLevelObjects() } returns listOf(bank, door)
        every { ctx.definitions.getObject(1) } returns bankDef
        every { ctx.definitions.getObject(2) } returns doorDef
        every { ctx.worldViews.getLocalPlayer() } returns fakePlayer(worldLocation = WorldPoint(3200, 3200, 0))

        val results = ObjectQueryBuilder(ctx).actions("Bank").results()
        assertEquals(1, results.size)
    }

    @Test
    fun `withinDistance filters by distance from player`() {
        val close = fakeObject(1, WorldPoint(3200, 3200, 0))
        val far = fakeObject(2, WorldPoint(3220, 3220, 0))

        val ctx = fakeContext()
        every { ctx.worldViews.getTopLevelObjects() } returns listOf(close, far)
        every { ctx.definitions.getObject(any()) } returns fakeObjectDef()
        every { ctx.worldViews.getLocalPlayer() } returns fakePlayer(worldLocation = WorldPoint(3200, 3200, 0))

        val results = ObjectQueryBuilder(ctx).withinDistance(5).results()
        assertEquals(1, results.size)
    }
}
```

Create `src/main/kotlin/org/tribot/api/query/ObjectQueryBuilder.kt`:

```kotlin
package org.tribot.api.query

import net.runelite.api.TileObject
import org.tribot.automation.script.ScriptContext

/**
 * Fluent query builder for finding tile objects (game objects, walls, etc.).
 */
class ObjectQueryBuilder(private val ctx: ScriptContext) : QueryBuilder<TileObject, ObjectQueryBuilder>() {

    fun names(vararg names: String): ObjectQueryBuilder = filter { obj ->
        val def = ctx.definitions.getObject(obj.id)
        def != null && def.name in names
    }

    fun ids(vararg ids: Int): ObjectQueryBuilder = filter { obj ->
        obj.id in ids.toSet()
    }

    fun actions(vararg actions: String): ObjectQueryBuilder = filter { obj ->
        val def = ctx.definitions.getObject(obj.id)
        def != null && def.actions.any { it in actions }
    }

    fun withinDistance(maxDistance: Int): ObjectQueryBuilder = filter { obj ->
        val player = ctx.worldViews.getLocalPlayer() ?: return@filter false
        obj.worldLocation.distanceTo(player.worldLocation) <= maxDistance
    }

    override fun fetchEntities(): List<TileObject> = ctx.worldViews.getTopLevelObjects()

    override fun results(): LocatableQueryResults<TileObject> =
        LocatableQueryResults(applyFilters(fetchEntities())) { it.worldLocation }
}
```

### Step 10: Implement GroundItemQueryBuilder

Create `src/test/kotlin/org/tribot/api/query/GroundItemQueryBuilderTest.kt`:

```kotlin
package org.tribot.api.query

import io.mockk.every
import io.mockk.mockk
import net.runelite.api.TileItem
import net.runelite.api.coords.WorldPoint
import org.tribot.api.testing.*
import org.tribot.automation.script.core.GroundItem
import kotlin.test.Test
import kotlin.test.assertEquals

class GroundItemQueryBuilderTest {

    private fun fakeGroundItem(id: Int, quantity: Int, position: WorldPoint): GroundItem {
        val tileItem = mockk<TileItem>(relaxed = true)
        every { tileItem.id } returns id
        every { tileItem.quantity } returns quantity
        return GroundItem(tileItem, position)
    }

    @Test
    fun `names filters by item definition name`() {
        val bones = fakeGroundItem(526, 1, WorldPoint(3200, 3200, 0))
        val coins = fakeGroundItem(995, 100, WorldPoint(3205, 3205, 0))

        val ctx = fakeContext()
        every { ctx.worldViews.getTopLevelGroundItems() } returns listOf(bones, coins)
        every { ctx.definitions.getItem(526) } returns fakeItemDef(id = 526, name = "Bones")
        every { ctx.definitions.getItem(995) } returns fakeItemDef(id = 995, name = "Coins")
        every { ctx.worldViews.getLocalPlayer() } returns fakePlayer()

        val results = GroundItemQueryBuilder(ctx).names("Bones").results()
        assertEquals(1, results.size)
    }

    @Test
    fun `ids filters by item id`() {
        val bones = fakeGroundItem(526, 1, WorldPoint(3200, 3200, 0))
        val coins = fakeGroundItem(995, 100, WorldPoint(3205, 3205, 0))

        val ctx = fakeContext()
        every { ctx.worldViews.getTopLevelGroundItems() } returns listOf(bones, coins)
        every { ctx.definitions.getItem(any()) } returns fakeItemDef()
        every { ctx.worldViews.getLocalPlayer() } returns fakePlayer()

        val results = GroundItemQueryBuilder(ctx).ids(526).results()
        assertEquals(1, results.size)
        assertEquals(526, results.first()?.id)
    }

    @Test
    fun `minQuantity filters by stack size`() {
        val smallStack = fakeGroundItem(995, 10, WorldPoint(3200, 3200, 0))
        val largeStack = fakeGroundItem(995, 1000, WorldPoint(3205, 3205, 0))

        val ctx = fakeContext()
        every { ctx.worldViews.getTopLevelGroundItems() } returns listOf(smallStack, largeStack)
        every { ctx.definitions.getItem(any()) } returns fakeItemDef()
        every { ctx.worldViews.getLocalPlayer() } returns fakePlayer()

        val results = GroundItemQueryBuilder(ctx).minQuantity(100).results()
        assertEquals(1, results.size)
    }
}
```

Create `src/main/kotlin/org/tribot/api/query/GroundItemQueryBuilder.kt`:

```kotlin
package org.tribot.api.query

import org.tribot.automation.script.ScriptContext
import org.tribot.automation.script.core.GroundItem

/**
 * Fluent query builder for finding ground items.
 */
class GroundItemQueryBuilder(private val ctx: ScriptContext) : QueryBuilder<GroundItem, GroundItemQueryBuilder>() {

    fun names(vararg names: String): GroundItemQueryBuilder = filter { item ->
        val def = ctx.definitions.getItem(item.id)
        def != null && def.name in names
    }

    fun ids(vararg ids: Int): GroundItemQueryBuilder = filter { item ->
        item.id in ids.toSet()
    }

    fun minQuantity(min: Int): GroundItemQueryBuilder = filter { it.quantity >= min }

    fun maxQuantity(max: Int): GroundItemQueryBuilder = filter { it.quantity <= max }

    fun withinDistance(maxDistance: Int): GroundItemQueryBuilder = filter { item ->
        val player = ctx.worldViews.getLocalPlayer() ?: return@filter false
        item.position.distanceTo(player.worldLocation) <= maxDistance
    }

    override fun fetchEntities(): List<GroundItem> = ctx.worldViews.getTopLevelGroundItems()

    override fun results(): LocatableQueryResults<GroundItem> =
        LocatableQueryResults(applyFilters(fetchEntities())) { it.position }
}
```

### Step 11: Implement InventoryQueryBuilder and BankQueryBuilder

Create `src/test/kotlin/org/tribot/api/query/InventoryQueryBuilderTest.kt`:

```kotlin
package org.tribot.api.query

import io.mockk.every
import org.tribot.api.testing.*
import org.tribot.automation.script.core.tabs.InventoryItem
import kotlin.test.Test
import kotlin.test.assertEquals

class InventoryQueryBuilderTest {

    @Test
    fun `names filters by item definition name`() {
        val ctx = fakeContext()
        every { ctx.inventory.getItems() } returns listOf(
            InventoryItem(379, 5, 0),  // Lobster
            InventoryItem(590, 1, 1)   // Tinderbox
        )
        every { ctx.definitions.getItem(379) } returns fakeItemDef(id = 379, name = "Lobster")
        every { ctx.definitions.getItem(590) } returns fakeItemDef(id = 590, name = "Tinderbox")

        val results = InventoryQueryBuilder(ctx).names("Lobster").results()
        assertEquals(1, results.size)
        assertEquals(379, results.first()?.id)
    }

    @Test
    fun `ids filters by item id`() {
        val ctx = fakeContext()
        every { ctx.inventory.getItems() } returns listOf(
            InventoryItem(379, 5, 0),
            InventoryItem(590, 1, 1)
        )

        val results = InventoryQueryBuilder(ctx).ids(590).results()
        assertEquals(1, results.size)
    }

    @Test
    fun `actions filters by item actions`() {
        val ctx = fakeContext()
        every { ctx.inventory.getItems() } returns listOf(
            InventoryItem(379, 5, 0),
            InventoryItem(590, 1, 1)
        )
        every { ctx.definitions.getItem(379) } returns fakeItemDef(id = 379, name = "Lobster", inventoryActions = listOf("Eat", null, null, "Drop", null))
        every { ctx.definitions.getItem(590) } returns fakeItemDef(id = 590, name = "Tinderbox", inventoryActions = listOf("Use", null, null, "Drop", null))

        val results = InventoryQueryBuilder(ctx).actions("Eat").results()
        assertEquals(1, results.size)
    }
}
```

Create `src/test/kotlin/org/tribot/api/query/BankQueryBuilderTest.kt`:

```kotlin
package org.tribot.api.query

import io.mockk.every
import org.tribot.api.testing.*
import org.tribot.automation.script.core.widgets.BankItem
import kotlin.test.Test
import kotlin.test.assertEquals

class BankQueryBuilderTest {

    @Test
    fun `names filters by item definition name`() {
        val ctx = fakeContext()
        every { ctx.banking.getItems() } returns listOf(
            BankItem(379, 100),
            BankItem(590, 1)
        )
        every { ctx.definitions.getItem(379) } returns fakeItemDef(id = 379, name = "Lobster")
        every { ctx.definitions.getItem(590) } returns fakeItemDef(id = 590, name = "Tinderbox")

        val results = BankQueryBuilder(ctx).names("Lobster").results()
        assertEquals(1, results.size)
    }

    @Test
    fun `minQuantity filters by stack size`() {
        val ctx = fakeContext()
        every { ctx.banking.getItems() } returns listOf(
            BankItem(379, 100),
            BankItem(590, 1)
        )
        every { ctx.definitions.getItem(any()) } returns fakeItemDef()

        val results = BankQueryBuilder(ctx).minQuantity(50).results()
        assertEquals(1, results.size)
    }
}
```

Create `src/main/kotlin/org/tribot/api/query/InventoryQueryBuilder.kt`:

```kotlin
package org.tribot.api.query

import org.tribot.automation.script.ScriptContext
import org.tribot.automation.script.core.tabs.InventoryItem

/**
 * Fluent query builder for inventory items.
 */
class InventoryQueryBuilder(private val ctx: ScriptContext) : QueryBuilder<InventoryItem, InventoryQueryBuilder>() {

    fun names(vararg names: String): InventoryQueryBuilder = filter { item ->
        val def = ctx.definitions.getItem(item.id)
        def != null && def.name in names
    }

    fun ids(vararg ids: Int): InventoryQueryBuilder = filter { item ->
        item.id in ids.toSet()
    }

    fun actions(vararg actions: String): InventoryQueryBuilder = filter { item ->
        val def = ctx.definitions.getItem(item.id)
        def != null && def.inventoryActions.any { it in actions }
    }

    fun minQuantity(min: Int): InventoryQueryBuilder = filter { it.quantity >= min }

    fun maxQuantity(max: Int): InventoryQueryBuilder = filter { it.quantity <= max }

    override fun fetchEntities(): List<InventoryItem> = ctx.inventory.getItems()

    override fun results(): QueryResults<InventoryItem> =
        QueryResults(applyFilters(fetchEntities()))
}
```

Create `src/main/kotlin/org/tribot/api/query/BankQueryBuilder.kt`:

```kotlin
package org.tribot.api.query

import org.tribot.automation.script.ScriptContext
import org.tribot.automation.script.core.widgets.BankItem

/**
 * Fluent query builder for bank items.
 */
class BankQueryBuilder(private val ctx: ScriptContext) : QueryBuilder<BankItem, BankQueryBuilder>() {

    fun names(vararg names: String): BankQueryBuilder = filter { item ->
        val def = ctx.definitions.getItem(item.id)
        def != null && def.name in names
    }

    fun ids(vararg ids: Int): BankQueryBuilder = filter { item ->
        item.id in ids.toSet()
    }

    fun actions(vararg actions: String): BankQueryBuilder = filter { item ->
        val def = ctx.definitions.getItem(item.id)
        def != null && def.inventoryActions.any { it in actions }
    }

    fun minQuantity(min: Int): BankQueryBuilder = filter { it.quantity >= min }

    fun maxQuantity(max: Int): BankQueryBuilder = filter { it.quantity <= max }

    override fun fetchEntities(): List<BankItem> = ctx.banking.getItems()

    override fun results(): QueryResults<BankItem> =
        QueryResults(applyFilters(fetchEntities()))
}
```

### Step 12: Implement PlayerQueryBuilder

Create `src/main/kotlin/org/tribot/api/query/PlayerQueryBuilder.kt`:

```kotlin
package org.tribot.api.query

import net.runelite.api.Player
import org.tribot.automation.script.ScriptContext

/**
 * Fluent query builder for finding players.
 */
class PlayerQueryBuilder(private val ctx: ScriptContext) : QueryBuilder<Player, PlayerQueryBuilder>() {

    private var includeLocal = false

    fun names(vararg names: String): PlayerQueryBuilder = filter { it.name in names }

    fun minCombatLevel(min: Int): PlayerQueryBuilder = filter { it.combatLevel >= min }

    fun maxCombatLevel(max: Int): PlayerQueryBuilder = filter { it.combatLevel <= max }

    fun animating(): PlayerQueryBuilder = filter { it.animation != -1 }

    fun notAnimating(): PlayerQueryBuilder = filter { it.animation == -1 }

    fun withinDistance(maxDistance: Int): PlayerQueryBuilder = filter { player ->
        val local = ctx.worldViews.getLocalPlayer() ?: return@filter false
        player.worldLocation.distanceTo(local.worldLocation) <= maxDistance
    }

    fun includeLocalPlayer(): PlayerQueryBuilder {
        includeLocal = true
        return this
    }

    override fun fetchEntities(): List<Player> {
        val players = ctx.worldViews.getTopLevelPlayers()
        if (includeLocal) return players
        val local = ctx.worldViews.getLocalPlayer()
        return players.filter { it != local }
    }

    override fun results(): LocatableQueryResults<Player> =
        LocatableQueryResults(applyFilters(fetchEntities())) { it.worldLocation }
}
```

### Step 13: Run all query tests

Run: `cd /Users/nick/Tribot/Community-Api && ./gradlew test`
Expected: ALL PASS

### Step 14: Commit

```bash
git add src/
git commit -m "feat: add full query API — NPC, object, ground item, player, inventory, bank builders"
```

---

## Task 4: Requirements API

**Files:**
- Create: `src/main/kotlin/org/tribot/api/requirements/Requirement.kt`
- Create: `src/main/kotlin/org/tribot/api/requirements/LogicType.kt`
- Create: `src/main/kotlin/org/tribot/api/requirements/CompositeRequirement.kt`
- Create: `src/main/kotlin/org/tribot/api/requirements/ItemRequirement.kt`
- Create: `src/main/kotlin/org/tribot/api/requirements/EquipmentRequirement.kt`
- Create: `src/main/kotlin/org/tribot/api/requirements/SkillRequirement.kt`
- Create: `src/main/kotlin/org/tribot/api/requirements/QuestRequirement.kt`
- Create: `src/main/kotlin/org/tribot/api/requirements/VarbitRequirement.kt`
- Create: `src/main/kotlin/org/tribot/api/requirements/VarplayerRequirement.kt`
- Create: `src/main/kotlin/org/tribot/api/requirements/ZoneRequirement.kt`
- Create: `src/main/kotlin/org/tribot/api/requirements/CombatLevelRequirement.kt`
- Create: `src/main/kotlin/org/tribot/api/requirements/PrayerRequirement.kt`
- Create: `src/main/kotlin/org/tribot/api/requirements/FreeSlotRequirement.kt`
- Create: `src/main/kotlin/org/tribot/api/requirements/WidgetRequirement.kt`
- Test: `src/test/kotlin/org/tribot/api/requirements/CompositeRequirementTest.kt`
- Test: `src/test/kotlin/org/tribot/api/requirements/ItemRequirementTest.kt`
- Test: `src/test/kotlin/org/tribot/api/requirements/SkillRequirementTest.kt`
- Test: `src/test/kotlin/org/tribot/api/requirements/VarbitRequirementTest.kt`

### Step 1: Write tests for core requirements

Create `src/test/kotlin/org/tribot/api/requirements/ItemRequirementTest.kt`:

```kotlin
package org.tribot.api.requirements

import io.mockk.every
import org.tribot.api.testing.fakeContext
import org.tribot.automation.script.core.tabs.InventoryItem
import org.tribot.automation.script.core.tabs.EquipmentSlot
import org.tribot.automation.script.core.tabs.EquippedItem
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ItemRequirementTest {

    @Test
    fun `satisfied when item exists in inventory with enough quantity`() {
        val ctx = fakeContext()
        every { ctx.inventory.getItems() } returns listOf(InventoryItem(379, 14, 0))

        val req = ItemRequirement(itemId = 379, quantity = 14)
        assertTrue(req.check(ctx))
    }

    @Test
    fun `not satisfied when quantity is insufficient`() {
        val ctx = fakeContext()
        every { ctx.inventory.getItems() } returns listOf(InventoryItem(379, 5, 0))

        val req = ItemRequirement(itemId = 379, quantity = 14)
        assertFalse(req.check(ctx))
    }

    @Test
    fun `not satisfied when item is missing`() {
        val ctx = fakeContext()
        every { ctx.inventory.getItems() } returns emptyList()

        val req = ItemRequirement(itemId = 379, quantity = 1)
        assertFalse(req.check(ctx))
    }

    @Test
    fun `accepts alternate item ids`() {
        val ctx = fakeContext()
        every { ctx.inventory.getItems() } returns listOf(InventoryItem(1360, 1, 0)) // rune axe

        val req = ItemRequirement(itemId = 1351, quantity = 1, alternateIds = listOf(1349, 1353, 1355, 1357, 1359, 1360))
        assertTrue(req.check(ctx))
    }

    @Test
    fun `equipped flag checks equipment instead`() {
        val ctx = fakeContext()
        every { ctx.inventory.getItems() } returns emptyList()
        every { ctx.equipment.getItems() } returns listOf(EquippedItem(1360, 1, EquipmentSlot.WEAPON))

        val req = ItemRequirement(itemId = 1360, quantity = 1, equipped = true)
        assertTrue(req.check(ctx))
    }

    @Test
    fun `sums quantity across multiple inventory slots`() {
        val ctx = fakeContext()
        every { ctx.inventory.getItems() } returns listOf(
            InventoryItem(379, 5, 0),
            InventoryItem(379, 5, 1),
            InventoryItem(379, 4, 2)
        )

        val req = ItemRequirement(itemId = 379, quantity = 14)
        assertTrue(req.check(ctx))
    }

    @Test
    fun `displayText describes the requirement`() {
        val req = ItemRequirement(itemId = 379, quantity = 14, displayName = "Lobster")
        assertTrue(req.displayText.contains("14"))
        assertTrue(req.displayText.contains("Lobster"))
    }
}
```

Create `src/test/kotlin/org/tribot/api/requirements/SkillRequirementTest.kt`:

```kotlin
package org.tribot.api.requirements

import io.mockk.every
import net.runelite.api.Skill
import org.tribot.api.testing.fakeContext
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SkillRequirementTest {

    @Test
    fun `satisfied when level meets requirement`() {
        val ctx = fakeContext()
        every { ctx.skills.getLevel(Skill.WOODCUTTING) } returns 60

        val req = SkillRequirement(Skill.WOODCUTTING, 60)
        assertTrue(req.check(ctx))
    }

    @Test
    fun `not satisfied when level is too low`() {
        val ctx = fakeContext()
        every { ctx.skills.getLevel(Skill.WOODCUTTING) } returns 40

        val req = SkillRequirement(Skill.WOODCUTTING, 60)
        assertFalse(req.check(ctx))
    }

    @Test
    fun `boostable checks boosted level`() {
        val ctx = fakeContext()
        every { ctx.skills.getLevel(Skill.WOODCUTTING) } returns 58
        every { ctx.skills.getBoostedLevel(Skill.WOODCUTTING) } returns 61

        val req = SkillRequirement(Skill.WOODCUTTING, 60, boostable = true)
        assertTrue(req.check(ctx))
    }

    @Test
    fun `non-boostable ignores boosted level`() {
        val ctx = fakeContext()
        every { ctx.skills.getLevel(Skill.WOODCUTTING) } returns 58
        every { ctx.skills.getBoostedLevel(Skill.WOODCUTTING) } returns 61

        val req = SkillRequirement(Skill.WOODCUTTING, 60, boostable = false)
        assertFalse(req.check(ctx))
    }
}
```

Create `src/test/kotlin/org/tribot/api/requirements/VarbitRequirementTest.kt`:

```kotlin
package org.tribot.api.requirements

import io.mockk.every
import org.tribot.api.testing.fakeContext
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VarbitRequirementTest {

    @Test
    fun `exact match satisfied`() {
        val ctx = fakeContext()
        every { ctx.client.getVarbitValue(1234) } returns 5

        val req = VarbitRequirement(1234, 5)
        assertTrue(req.check(ctx))
    }

    @Test
    fun `exact match not satisfied`() {
        val ctx = fakeContext()
        every { ctx.client.getVarbitValue(1234) } returns 3

        val req = VarbitRequirement(1234, 5)
        assertFalse(req.check(ctx))
    }

    @Test
    fun `greater-equal operation`() {
        val ctx = fakeContext()
        every { ctx.client.getVarbitValue(1234) } returns 7

        val req = VarbitRequirement(1234, 5, Operation.GREATER_EQUAL)
        assertTrue(req.check(ctx))
    }

    @Test
    fun `less-than operation`() {
        val ctx = fakeContext()
        every { ctx.client.getVarbitValue(1234) } returns 3

        val req = VarbitRequirement(1234, 5, Operation.LESS)
        assertTrue(req.check(ctx))
    }
}
```

Create `src/test/kotlin/org/tribot/api/requirements/CompositeRequirementTest.kt`:

```kotlin
package org.tribot.api.requirements

import io.mockk.every
import net.runelite.api.Skill
import org.tribot.api.testing.fakeContext
import org.tribot.automation.script.core.tabs.InventoryItem
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompositeRequirementTest {

    @Test
    fun `AND requires all to pass`() {
        val ctx = fakeContext()
        every { ctx.inventory.getItems() } returns listOf(InventoryItem(379, 14, 0))
        every { ctx.skills.getLevel(Skill.COOKING) } returns 40

        val req = CompositeRequirement(
            LogicType.AND,
            ItemRequirement(379, 14),
            SkillRequirement(Skill.COOKING, 40)
        )
        assertTrue(req.check(ctx))
    }

    @Test
    fun `AND fails when one fails`() {
        val ctx = fakeContext()
        every { ctx.inventory.getItems() } returns listOf(InventoryItem(379, 5, 0))
        every { ctx.skills.getLevel(Skill.COOKING) } returns 40

        val req = CompositeRequirement(
            LogicType.AND,
            ItemRequirement(379, 14),
            SkillRequirement(Skill.COOKING, 40)
        )
        assertFalse(req.check(ctx))
    }

    @Test
    fun `OR passes when any passes`() {
        val ctx = fakeContext()
        every { ctx.inventory.getItems() } returns listOf(InventoryItem(379, 5, 0))
        every { ctx.skills.getLevel(Skill.COOKING) } returns 40

        val req = CompositeRequirement(
            LogicType.OR,
            ItemRequirement(379, 14),
            SkillRequirement(Skill.COOKING, 40)
        )
        assertTrue(req.check(ctx))
    }

    @Test
    fun `OR fails when all fail`() {
        val ctx = fakeContext()
        every { ctx.inventory.getItems() } returns emptyList()
        every { ctx.skills.getLevel(Skill.COOKING) } returns 20

        val req = CompositeRequirement(
            LogicType.OR,
            ItemRequirement(379, 14),
            SkillRequirement(Skill.COOKING, 40)
        )
        assertFalse(req.check(ctx))
    }

    @Test
    fun `NOR passes when none pass`() {
        val ctx = fakeContext()
        every { ctx.inventory.getItems() } returns emptyList()
        every { ctx.skills.getLevel(Skill.COOKING) } returns 20

        val req = CompositeRequirement(
            LogicType.NOR,
            ItemRequirement(379, 14),
            SkillRequirement(Skill.COOKING, 40)
        )
        assertTrue(req.check(ctx))
    }

    @Test
    fun `helper functions work`() {
        val ctx = fakeContext()
        every { ctx.inventory.getItems() } returns listOf(InventoryItem(379, 14, 0))
        every { ctx.skills.getLevel(Skill.COOKING) } returns 40

        val all = Requirements.all(
            ItemRequirement(379, 14),
            SkillRequirement(Skill.COOKING, 40)
        )
        assertTrue(all.check(ctx))

        val any = Requirements.any(
            ItemRequirement(379, 14),
            SkillRequirement(Skill.COOKING, 99)
        )
        assertTrue(any.check(ctx))
    }
}
```

### Step 2: Implement Requirement interface and core types

Create `src/main/kotlin/org/tribot/api/requirements/Requirement.kt`:

```kotlin
package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext

/**
 * A game-state condition that can be checked against the current script context.
 */
interface Requirement {
    /**
     * Check whether this requirement is currently satisfied.
     */
    fun check(ctx: ScriptContext): Boolean

    /**
     * Human-readable description of this requirement.
     */
    val displayText: String
}

/**
 * Comparison operations for numeric requirements.
 */
enum class Operation(val symbol: String) {
    EQUAL("=="),
    NOT_EQUAL("!="),
    GREATER(">"),
    LESS("<"),
    GREATER_EQUAL(">="),
    LESS_EQUAL("<=");

    fun check(actual: Int, expected: Int): Boolean = when (this) {
        EQUAL -> actual == expected
        NOT_EQUAL -> actual != expected
        GREATER -> actual > expected
        LESS -> actual < expected
        GREATER_EQUAL -> actual >= expected
        LESS_EQUAL -> actual <= expected
    }
}
```

Create `src/main/kotlin/org/tribot/api/requirements/LogicType.kt`:

```kotlin
package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext

enum class LogicType {
    /** All requirements must pass. */
    AND,
    /** At least one requirement must pass. */
    OR,
    /** No requirements may pass. */
    NOR,
    /** Not all requirements pass (at least one fails). */
    NAND,
    /** Exactly one requirement passes. */
    XOR;

    fun test(requirements: List<Requirement>, ctx: ScriptContext): Boolean {
        val passed = requirements.count { it.check(ctx) }
        return when (this) {
            AND -> passed == requirements.size
            OR -> passed > 0
            NOR -> passed == 0
            NAND -> passed < requirements.size
            XOR -> passed == 1
        }
    }
}
```

Create `src/main/kotlin/org/tribot/api/requirements/CompositeRequirement.kt`:

```kotlin
package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext

/**
 * A requirement composed of multiple sub-requirements combined with a [LogicType].
 */
class CompositeRequirement(
    private val logicType: LogicType,
    private val requirements: List<Requirement>,
    private val name: String? = null
) : Requirement {

    constructor(logicType: LogicType, vararg requirements: Requirement) :
        this(logicType, requirements.toList())

    override fun check(ctx: ScriptContext): Boolean =
        logicType.test(requirements, ctx)

    override val displayText: String
        get() = name ?: requirements.joinToString(" ${logicType.name} ") { it.displayText }
}

/**
 * Convenience factory methods for composing requirements.
 */
object Requirements {
    fun all(vararg requirements: Requirement) = CompositeRequirement(LogicType.AND, *requirements)
    fun any(vararg requirements: Requirement) = CompositeRequirement(LogicType.OR, *requirements)
    fun none(vararg requirements: Requirement) = CompositeRequirement(LogicType.NOR, *requirements)
    fun not(requirement: Requirement) = CompositeRequirement(LogicType.NOR, requirement)
}
```

Create `src/main/kotlin/org/tribot/api/requirements/ItemRequirement.kt`:

```kotlin
package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext

/**
 * Requires a specific item (or any of its alternates) in inventory or equipment.
 *
 * @param itemId Primary item ID
 * @param quantity Required quantity (summed across slots)
 * @param equipped If true, checks equipment instead of inventory
 * @param alternateIds Alternative item IDs that also satisfy this requirement
 * @param displayName Human-readable item name for display
 */
class ItemRequirement(
    val itemId: Int,
    val quantity: Int = 1,
    val equipped: Boolean = false,
    val alternateIds: List<Int> = emptyList(),
    val displayName: String = "Item #$itemId"
) : Requirement {

    private val allIds: Set<Int> get() = setOf(itemId) + alternateIds

    override fun check(ctx: ScriptContext): Boolean {
        if (equipped) {
            return ctx.equipment.getItems().any { it.id in allIds && it.quantity >= quantity }
        }
        val total = ctx.inventory.getItems()
            .filter { it.id in allIds }
            .sumOf { it.quantity }
        return total >= quantity
    }

    override val displayText: String
        get() = "$quantity x $displayName${if (equipped) " (equipped)" else ""}"
}
```

Create `src/main/kotlin/org/tribot/api/requirements/EquipmentRequirement.kt`:

```kotlin
package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext
import org.tribot.automation.script.core.tabs.EquipmentSlot

/**
 * Requires a specific item equipped in a specific slot.
 */
class EquipmentRequirement(
    val itemId: Int,
    val slot: EquipmentSlot,
    val alternateIds: List<Int> = emptyList(),
    val displayName: String = "Item #$itemId"
) : Requirement {

    private val allIds: Set<Int> get() = setOf(itemId) + alternateIds

    override fun check(ctx: ScriptContext): Boolean {
        val equipped = ctx.equipment.getItemIn(slot) ?: return false
        return equipped.id in allIds
    }

    override val displayText: String get() = "$displayName in ${slot.name}"
}
```

Create `src/main/kotlin/org/tribot/api/requirements/SkillRequirement.kt`:

```kotlin
package org.tribot.api.requirements

import net.runelite.api.Skill
import org.tribot.automation.script.ScriptContext

/**
 * Requires a minimum skill level.
 *
 * @param skill The skill to check
 * @param level Required level
 * @param boostable If true, accepts boosted level (e.g. from potions)
 */
class SkillRequirement(
    val skill: Skill,
    val level: Int,
    val boostable: Boolean = false
) : Requirement {

    override fun check(ctx: ScriptContext): Boolean {
        val actual = if (boostable) ctx.skills.getBoostedLevel(skill) else ctx.skills.getLevel(skill)
        return actual >= level
    }

    override val displayText: String
        get() = "$level ${skill.getName()}${if (boostable) " (boostable)" else ""}"
}
```

Create `src/main/kotlin/org/tribot/api/requirements/QuestRequirement.kt`:

```kotlin
package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext

/**
 * Requires a quest varbit to be at or above a certain value.
 * Quest state is typically tracked via varbits in OSRS.
 *
 * @param questName Human-readable quest name
 * @param varbitId The varbit ID that tracks quest progress
 * @param requiredValue The minimum varbit value (e.g. final value = completed)
 * @param operation Comparison operation (default: GREATER_EQUAL)
 */
class QuestRequirement(
    val questName: String,
    val varbitId: Int,
    val requiredValue: Int,
    val operation: Operation = Operation.GREATER_EQUAL
) : Requirement {

    override fun check(ctx: ScriptContext): Boolean {
        val actual = ctx.client.getVarbitValue(varbitId)
        return operation.check(actual, requiredValue)
    }

    override val displayText: String get() = questName
}
```

Create `src/main/kotlin/org/tribot/api/requirements/VarbitRequirement.kt`:

```kotlin
package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext

/**
 * Requires a varbit to match a value using the given operation.
 */
class VarbitRequirement(
    val varbitId: Int,
    val value: Int,
    val operation: Operation = Operation.EQUAL,
    private val name: String = "Varbit $varbitId ${operation.symbol} $value"
) : Requirement {

    override fun check(ctx: ScriptContext): Boolean {
        val actual = ctx.client.getVarbitValue(varbitId)
        return operation.check(actual, value)
    }

    override val displayText: String get() = name
}
```

Create `src/main/kotlin/org/tribot/api/requirements/VarplayerRequirement.kt`:

```kotlin
package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext

/**
 * Requires a varplayer (varp) to match a value using the given operation.
 */
class VarplayerRequirement(
    val varpId: Int,
    val value: Int,
    val operation: Operation = Operation.EQUAL,
    private val name: String = "Varp $varpId ${operation.symbol} $value"
) : Requirement {

    override fun check(ctx: ScriptContext): Boolean {
        val actual = ctx.client.getVarpValue(varpId)
        return operation.check(actual, value)
    }

    override val displayText: String get() = name
}
```

Create `src/main/kotlin/org/tribot/api/requirements/ZoneRequirement.kt`:

```kotlin
package org.tribot.api.requirements

import net.runelite.api.coords.WorldPoint
import org.tribot.automation.script.ScriptContext

/**
 * Requires the player to be within a rectangular zone defined by two corner points.
 */
class ZoneRequirement(
    val southWest: WorldPoint,
    val northEast: WorldPoint,
    val checkInZone: Boolean = true,
    private val name: String = "Zone (${southWest.x},${southWest.y})-(${northEast.x},${northEast.y})"
) : Requirement {

    override fun check(ctx: ScriptContext): Boolean {
        val pos = ctx.worldViews.getLocalPlayer()?.worldLocation ?: return false
        val inZone = pos.x in southWest.x..northEast.x &&
            pos.y in southWest.y..northEast.y &&
            pos.plane in southWest.plane..northEast.plane
        return if (checkInZone) inZone else !inZone
    }

    override val displayText: String get() = if (checkInZone) "In $name" else "Not in $name"
}
```

Create `src/main/kotlin/org/tribot/api/requirements/CombatLevelRequirement.kt`:

```kotlin
package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext

/**
 * Requires the player to have a minimum combat level.
 */
class CombatLevelRequirement(
    val level: Int,
    val operation: Operation = Operation.GREATER_EQUAL
) : Requirement {

    override fun check(ctx: ScriptContext): Boolean {
        val actual = ctx.worldViews.getLocalPlayer()?.combatLevel ?: return false
        return operation.check(actual, level)
    }

    override val displayText: String get() = "Combat level ${operation.symbol} $level"
}
```

Create `src/main/kotlin/org/tribot/api/requirements/PrayerRequirement.kt`:

```kotlin
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

    override val displayText: String
        get() = "${prayer.name} ${if (active) "active" else "inactive"}"
}
```

Create `src/main/kotlin/org/tribot/api/requirements/FreeSlotRequirement.kt`:

```kotlin
package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext

/**
 * Requires a minimum number of free inventory slots.
 */
class FreeSlotRequirement(
    val slots: Int
) : Requirement {

    override fun check(ctx: ScriptContext): Boolean =
        ctx.inventory.emptySlots() >= slots

    override val displayText: String get() = "$slots free inventory slots"
}
```

Create `src/main/kotlin/org/tribot/api/requirements/WidgetRequirement.kt`:

```kotlin
package org.tribot.api.requirements

import org.tribot.automation.script.ScriptContext

/**
 * Requires a widget to be visible (or hidden). Uses a predicate on the Client
 * to check widget state, since the SDK doesn't expose a direct widget query.
 *
 * @param groupId Widget group ID
 * @param childId Widget child ID
 * @param visible Whether the widget should be visible (true) or hidden (false)
 */
class WidgetRequirement(
    val groupId: Int,
    val childId: Int,
    val visible: Boolean = true,
    private val name: String = "Widget $groupId:$childId"
) : Requirement {

    override fun check(ctx: ScriptContext): Boolean {
        val widget = ctx.client.getWidget(groupId, childId) ?: return !visible
        return widget.isHidden != visible
    }

    override val displayText: String
        get() = "$name ${if (visible) "visible" else "hidden"}"
}
```

### Step 3: Run all requirements tests

Run: `cd /Users/nick/Tribot/Community-Api && ./gradlew test`
Expected: ALL PASS

### Step 4: Commit

```bash
git add src/
git commit -m "feat: add requirements API — item, skill, quest, varbit, zone, prayer, combat, and composite logic"
```

---

## Task 5: Loadout System

**Files:**
- Create: `src/main/kotlin/org/tribot/api/loadout/Loadout.kt`
- Create: `src/main/kotlin/org/tribot/api/loadout/LoadoutManager.kt`
- Test: `src/test/kotlin/org/tribot/api/loadout/LoadoutManagerTest.kt`

### Step 1: Write tests

Create `src/test/kotlin/org/tribot/api/loadout/LoadoutManagerTest.kt`:

```kotlin
package org.tribot.api.loadout

import io.mockk.*
import org.tribot.api.testing.fakeContext
import org.tribot.automation.script.core.tabs.EquipmentSlot
import org.tribot.automation.script.core.tabs.EquippedItem
import org.tribot.automation.script.core.tabs.InventoryItem
import org.tribot.automation.script.core.widgets.BankItem
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoadoutManagerTest {

    @Test
    fun `isSatisfied returns true when loadout matches`() {
        val ctx = fakeContext()
        every { ctx.inventory.getItems() } returns listOf(
            InventoryItem(379, 14, 0)
        )
        every { ctx.equipment.getItems() } returns listOf(
            EquippedItem(1333, 1, EquipmentSlot.WEAPON)
        )

        val loadout = Loadout(
            inventory = listOf(LoadoutItem(379, 14)),
            equipment = listOf(LoadoutItem(1333, 1, EquipmentSlot.WEAPON))
        )
        assertTrue(LoadoutManager.isSatisfied(ctx, loadout))
    }

    @Test
    fun `isSatisfied returns false when items missing`() {
        val ctx = fakeContext()
        every { ctx.inventory.getItems() } returns emptyList()
        every { ctx.equipment.getItems() } returns emptyList()

        val loadout = Loadout(
            inventory = listOf(LoadoutItem(379, 14))
        )
        assertFalse(LoadoutManager.isSatisfied(ctx, loadout))
    }

    @Test
    fun `getMissingItems returns items not in inventory`() {
        val ctx = fakeContext()
        every { ctx.inventory.getItems() } returns listOf(
            InventoryItem(379, 5, 0)
        )
        every { ctx.equipment.getItems() } returns emptyList()

        val loadout = Loadout(
            inventory = listOf(LoadoutItem(379, 14), LoadoutItem(590, 1))
        )
        val missing = LoadoutManager.getMissingItems(ctx, loadout)
        // Need 9 more lobsters and 1 tinderbox
        assertTrue(missing.any { it.itemId == 379 && it.quantity == 9 })
        assertTrue(missing.any { it.itemId == 590 && it.quantity == 1 })
    }

    @Test
    fun `getUnwantedItems returns inventory items not in loadout`() {
        val ctx = fakeContext()
        every { ctx.inventory.getItems() } returns listOf(
            InventoryItem(379, 5, 0),
            InventoryItem(995, 1000, 1)  // coins, not in loadout
        )

        val loadout = Loadout(
            inventory = listOf(LoadoutItem(379, 14))
        )
        val unwanted = LoadoutManager.getUnwantedItems(ctx, loadout)
        assertTrue(unwanted.any { it.id == 995 })
        assertFalse(unwanted.any { it.id == 379 })
    }

    @Test
    fun `getMissingEquipment returns equipment not worn`() {
        val ctx = fakeContext()
        every { ctx.equipment.getItems() } returns emptyList()

        val loadout = Loadout(
            equipment = listOf(LoadoutItem(1333, 1, EquipmentSlot.WEAPON))
        )
        val missing = LoadoutManager.getMissingEquipment(ctx, loadout)
        assertTrue(missing.any { it.itemId == 1333 })
    }

    @Test
    fun `alternateIds are considered when checking satisfaction`() {
        val ctx = fakeContext()
        every { ctx.inventory.getItems() } returns listOf(
            InventoryItem(1360, 1, 0)  // rune axe (alternate)
        )
        every { ctx.equipment.getItems() } returns emptyList()

        val loadout = Loadout(
            inventory = listOf(LoadoutItem(1351, 1, alternateIds = listOf(1349, 1353, 1355, 1357, 1359, 1360)))
        )
        assertTrue(LoadoutManager.isSatisfied(ctx, loadout))
    }
}
```

### Step 2: Implement Loadout and LoadoutManager

Create `src/main/kotlin/org/tribot/api/loadout/Loadout.kt`:

```kotlin
package org.tribot.api.loadout

import org.tribot.automation.script.core.tabs.EquipmentSlot

/**
 * A single item in a loadout definition.
 *
 * @param itemId Primary item ID
 * @param quantity Desired quantity
 * @param slot Equipment slot (null for inventory items)
 * @param alternateIds Alternative item IDs that also satisfy this slot
 */
data class LoadoutItem(
    val itemId: Int,
    val quantity: Int = 1,
    val slot: EquipmentSlot? = null,
    val alternateIds: List<Int> = emptyList()
) {
    val allIds: Set<Int> get() = setOf(itemId) + alternateIds
}

/**
 * Defines a desired inventory + equipment state.
 *
 * Usage:
 * ```
 * val loadout = Loadout(
 *     inventory = listOf(
 *         LoadoutItem(379, 14),           // 14 lobsters
 *         LoadoutItem(590, 1),            // 1 tinderbox
 *     ),
 *     equipment = listOf(
 *         LoadoutItem(1333, 1, EquipmentSlot.WEAPON),  // rune scimitar
 *     )
 * )
 * ```
 */
data class Loadout(
    val inventory: List<LoadoutItem> = emptyList(),
    val equipment: List<LoadoutItem> = emptyList()
)
```

Create `src/main/kotlin/org/tribot/api/loadout/LoadoutManager.kt`:

```kotlin
package org.tribot.api.loadout

import org.tribot.api.query.ObjectQueryBuilder
import org.tribot.api.query.NpcQueryBuilder
import org.tribot.api.waiting.Conditions
import org.tribot.automation.script.ScriptContext
import org.tribot.automation.script.core.tabs.InventoryItem

/**
 * Manages restocking inventory and equipment to match a [Loadout].
 *
 * Handles the full bank-and-restock loop:
 * 1. Open nearest bank
 * 2. Deposit items not in the loadout
 * 3. Withdraw missing items
 * 4. Equip gear that should be equipped
 * 5. Close bank
 */
object LoadoutManager {

    /**
     * Checks if the current inventory and equipment match the loadout.
     */
    fun isSatisfied(ctx: ScriptContext, loadout: Loadout): Boolean =
        getMissingItems(ctx, loadout).isEmpty() && getMissingEquipment(ctx, loadout).isEmpty()

    /**
     * Returns inventory items needed but missing or insufficient.
     * Each returned [LoadoutItem] has the deficit quantity.
     */
    fun getMissingItems(ctx: ScriptContext, loadout: Loadout): List<LoadoutItem> {
        val invItems = ctx.inventory.getItems()
        return loadout.inventory.mapNotNull { needed ->
            val have = invItems.filter { it.id in needed.allIds }.sumOf { it.quantity }
            val deficit = needed.quantity - have
            if (deficit > 0) needed.copy(quantity = deficit) else null
        }
    }

    /**
     * Returns inventory items that are NOT part of the loadout.
     */
    fun getUnwantedItems(ctx: ScriptContext, loadout: Loadout): List<InventoryItem> {
        val wantedIds = loadout.inventory.flatMap { it.allIds }.toSet()
        return ctx.inventory.getItems().filter { it.id !in wantedIds }
    }

    /**
     * Returns equipment items that should be worn but aren't.
     */
    fun getMissingEquipment(ctx: ScriptContext, loadout: Loadout): List<LoadoutItem> {
        val equipped = ctx.equipment.getItems()
        return loadout.equipment.filter { needed ->
            val slot = needed.slot ?: return@filter false
            val current = equipped.find { it.slot == slot }
            current == null || current.id !in needed.allIds
        }
    }

    /**
     * Executes the full restock loop: open bank, deposit unwanted, withdraw missing,
     * equip gear, close bank.
     *
     * @return true if loadout is satisfied after restocking
     */
    fun fulfill(ctx: ScriptContext, loadout: Loadout): Boolean {
        if (isSatisfied(ctx, loadout)) return true

        // Open bank if not already open
        if (!ctx.banking.isOpen()) {
            if (!openNearestBank(ctx)) return false
        }

        // Handle PIN
        if (ctx.banking.isPinScreenOpen()) {
            val pin = ctx.runtime.getOsrsAccountHash() // PIN should come from account config
            // PIN entry is left to the caller or login handler
        }

        // Deposit unwanted items
        val unwanted = getUnwantedItems(ctx, loadout)
        for (item in unwanted) {
            ctx.banking.depositAll(item.id)
            Conditions.waitUntil(ctx.waiting, 1200) { !ctx.inventory.contains(item.id) }
        }

        // Withdraw missing items
        val missing = getMissingItems(ctx, loadout)
        for (item in missing) {
            if (!ctx.banking.contains(item.itemId)) {
                // Try alternates
                val available = item.alternateIds.firstOrNull { ctx.banking.contains(it) }
                if (available != null) {
                    ctx.banking.withdraw(available, item.quantity)
                } else {
                    continue // Item not available
                }
            } else {
                ctx.banking.withdraw(item.itemId, item.quantity)
            }
            Conditions.waitUntil(ctx.waiting, 1200) {
                ctx.inventory.getItems().any { it.id in item.allIds }
            }
        }

        // Close bank before equipping
        val needsEquip = getMissingEquipment(ctx, loadout)
        if (needsEquip.isNotEmpty()) {
            ctx.banking.close()
            Conditions.waitUntil(ctx.waiting, 1200) { !ctx.banking.isOpen() }

            // Equip items from inventory
            for (item in needsEquip) {
                ctx.inventory.clickItem(item.itemId, "Wield")
                    || ctx.inventory.clickItem(item.itemId, "Wear")
                    || ctx.inventory.clickItem(item.itemId, "Equip")
                Conditions.waitUntil(ctx.waiting, 1200) { ctx.equipment.isEquipped(item.itemId) }
            }
        } else {
            ctx.banking.close()
        }

        return isSatisfied(ctx, loadout)
    }

    private fun openNearestBank(ctx: ScriptContext): Boolean {
        // Try bank booth first
        val booth = ObjectQueryBuilder(ctx)
            .actions("Bank")
            .withinDistance(20)
            .results()
            .nearest(ctx.worldViews.getLocalPlayer()!!.worldLocation)

        if (booth != null) {
            ctx.interaction.click(booth, "Bank")
            return Conditions.waitUntil(ctx.waiting, 5000) { ctx.banking.isOpen() }
        }

        // Try bank NPC
        val banker = NpcQueryBuilder(ctx)
            .actions("Bank")
            .withinDistance(20)
            .results()
            .nearest(ctx.worldViews.getLocalPlayer()!!.worldLocation)

        if (banker != null) {
            ctx.interaction.click(banker, "Bank")
            return Conditions.waitUntil(ctx.waiting, 5000) { ctx.banking.isOpen() }
        }

        return false
    }
}
```

### Step 3: Run tests

Run: `cd /Users/nick/Tribot/Community-Api && ./gradlew test`
Expected: ALL PASS

### Step 4: Commit

```bash
git add src/
git commit -m "feat: add loadout system — define desired gear/inventory and auto-restock from bank"
```

---

## Task 6: Grand Exchange API

**Files:**
- Create: `src/main/kotlin/org/tribot/api/ge/Offer.kt`
- Create: `src/main/kotlin/org/tribot/api/ge/GrandExchange.kt`
- Create: `src/main/kotlin/org/tribot/api/ge/PriceLookup.kt`
- Test: `src/test/kotlin/org/tribot/api/ge/GrandExchangeTest.kt`

### Step 1: Write tests

Create `src/test/kotlin/org/tribot/api/ge/GrandExchangeTest.kt`:

```kotlin
package org.tribot.api.ge

import io.mockk.*
import net.runelite.api.*
import net.runelite.api.widgets.Widget
import org.tribot.api.testing.fakeContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GrandExchangeTest {

    @Test
    fun `getOffers returns all 8 offer slots`() {
        val ctx = fakeContext()
        val geOffers = Array(8) {
            mockk<GrandExchangeOffer>(relaxed = true).also { offer ->
                every { offer.state } returns GrandExchangeOfferState.EMPTY
                every { offer.itemId } returns 0
                every { offer.totalQuantity } returns 0
                every { offer.quantitySold } returns 0
                every { offer.price } returns 0
                every { offer.spent } returns 0
            }
        }
        every { ctx.client.grandExchangeOffers } returns geOffers

        val offers = GrandExchange.getOffers(ctx)
        assertEquals(8, offers.size)
    }

    @Test
    fun `getEmptySlot returns first empty slot index`() {
        val ctx = fakeContext()
        val geOffers = Array(8) { i ->
            mockk<GrandExchangeOffer>(relaxed = true).also { offer ->
                every { offer.state } returns if (i < 3) GrandExchangeOfferState.BUYING else GrandExchangeOfferState.EMPTY
            }
        }
        every { ctx.client.grandExchangeOffers } returns geOffers

        assertEquals(3, GrandExchange.getEmptySlotIndex(ctx))
    }

    @Test
    fun `getEmptySlot returns null when all slots used`() {
        val ctx = fakeContext()
        val geOffers = Array(8) {
            mockk<GrandExchangeOffer>(relaxed = true).also { offer ->
                every { offer.state } returns GrandExchangeOfferState.BUYING
            }
        }
        every { ctx.client.grandExchangeOffers } returns geOffers

        assertEquals(null, GrandExchange.getEmptySlotIndex(ctx))
    }

    @Test
    fun `getCompletedOffers filters correctly`() {
        val ctx = fakeContext()
        val geOffers = Array(8) { i ->
            mockk<GrandExchangeOffer>(relaxed = true).also { offer ->
                val state = when (i) {
                    0 -> GrandExchangeOfferState.BOUGHT
                    1 -> GrandExchangeOfferState.SOLD
                    2 -> GrandExchangeOfferState.BUYING
                    else -> GrandExchangeOfferState.EMPTY
                }
                every { offer.state } returns state
                every { offer.itemId } returns i
                every { offer.totalQuantity } returns 100
                every { offer.quantitySold } returns 100
                every { offer.price } returns 50
                every { offer.spent } returns 5000
            }
        }
        every { ctx.client.grandExchangeOffers } returns geOffers

        val completed = GrandExchange.getCompletedOffers(ctx)
        assertEquals(2, completed.size)
    }
}
```

### Step 2: Implement GE types

Create `src/main/kotlin/org/tribot/api/ge/Offer.kt`:

```kotlin
package org.tribot.api.ge

import net.runelite.api.GrandExchangeOfferState

/**
 * Represents a Grand Exchange offer slot.
 */
data class Offer(
    val slotIndex: Int,
    val state: GrandExchangeOfferState,
    val itemId: Int,
    val totalQuantity: Int,
    val quantityFilled: Int,
    val price: Int,
    val totalSpent: Int
) {
    val isEmpty: Boolean get() = state == GrandExchangeOfferState.EMPTY
    val isBuying: Boolean get() = state == GrandExchangeOfferState.BUYING
    val isSelling: Boolean get() = state == GrandExchangeOfferState.SELLING
    val isComplete: Boolean get() = state == GrandExchangeOfferState.BOUGHT || state == GrandExchangeOfferState.SOLD
    val isCancelled: Boolean get() = state == GrandExchangeOfferState.CANCELLED_BUY || state == GrandExchangeOfferState.CANCELLED_SELL
    val quantityRemaining: Int get() = totalQuantity - quantityFilled
}
```

Create `src/main/kotlin/org/tribot/api/ge/PriceLookup.kt`:

```kotlin
package org.tribot.api.ge

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Looks up Grand Exchange prices via the OSRS Wiki real-time prices API.
 */
object PriceLookup {

    private val httpClient = HttpClient.newHttpClient()
    private val gson = Gson()
    private const val BASE_URL = "https://prices.runescape.wiki/api/v1/osrs"

    /**
     * Gets the latest price for an item.
     *
     * @return Pair of (buyPrice, sellPrice) or null if not found
     */
    fun getPrice(itemId: Int): Pair<Int, Int>? {
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$BASE_URL/latest?id=$itemId"))
                .header("User-Agent", "tribot-community-api")
                .GET()
                .build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            val json = gson.fromJson(response.body(), JsonObject::class.java)
            val data = json.getAsJsonObject("data")?.getAsJsonObject(itemId.toString()) ?: return null
            val high = data.get("high")?.asInt ?: return null
            val low = data.get("low")?.asInt ?: return null
            Pair(high, low)
        } catch (e: Exception) {
            null
        }
    }
}
```

Create `src/main/kotlin/org/tribot/api/ge/GrandExchange.kt`:

```kotlin
package org.tribot.api.ge

import net.runelite.api.GrandExchangeOfferState
import org.tribot.api.query.NpcQueryBuilder
import org.tribot.api.waiting.Conditions
import org.tribot.automation.script.ScriptContext

/**
 * Higher-level Grand Exchange interaction API.
 *
 * Provides methods to read offer state, buy/sell items, collect completed offers,
 * and open/close the GE interface.
 */
object GrandExchange {

    private const val GE_WIDGET_GROUP = 465

    /**
     * Gets all 8 GE offer slots as [Offer] objects.
     */
    fun getOffers(ctx: ScriptContext): List<Offer> {
        val geOffers = ctx.client.grandExchangeOffers ?: return emptyList()
        return geOffers.mapIndexed { index, offer ->
            Offer(
                slotIndex = index,
                state = offer.state,
                itemId = offer.itemId,
                totalQuantity = offer.totalQuantity,
                quantityFilled = offer.quantitySold,
                price = offer.price,
                totalSpent = offer.spent
            )
        }
    }

    /**
     * Returns the index of the first empty offer slot, or null if all are in use.
     */
    fun getEmptySlotIndex(ctx: ScriptContext): Int? {
        val offers = ctx.client.grandExchangeOffers ?: return null
        return offers.indexOfFirst { it.state == GrandExchangeOfferState.EMPTY }.takeIf { it >= 0 }
    }

    /**
     * Returns all completed (bought/sold) offers that need collection.
     */
    fun getCompletedOffers(ctx: ScriptContext): List<Offer> =
        getOffers(ctx).filter { it.isComplete }

    /**
     * Returns all active (in-progress) offers.
     */
    fun getActiveOffers(ctx: ScriptContext): List<Offer> =
        getOffers(ctx).filter { it.isBuying || it.isSelling }

    /**
     * Checks if the GE interface is open.
     */
    fun isOpen(ctx: ScriptContext): Boolean {
        val widget = ctx.client.getWidget(GE_WIDGET_GROUP, 0)
        return widget != null && !widget.isHidden
    }

    /**
     * Opens the GE by interacting with a GE clerk NPC.
     *
     * @return true if the GE was successfully opened
     */
    fun open(ctx: ScriptContext): Boolean {
        if (isOpen(ctx)) return true

        val clerk = NpcQueryBuilder(ctx)
            .actions("Exchange")
            .withinDistance(15)
            .results()
            .nearest(ctx.worldViews.getLocalPlayer()!!.worldLocation)
            ?: return false

        ctx.interaction.click(clerk, "Exchange")
        return Conditions.waitUntil(ctx.waiting, 5000) { isOpen(ctx) }
    }

    /**
     * Closes the GE interface.
     */
    fun close(ctx: ScriptContext): Boolean {
        if (!isOpen(ctx)) return true
        val closeWidget = ctx.client.getWidget(GE_WIDGET_GROUP, 2) ?: return false
        ctx.interaction.click(closeWidget, "Close")
        return Conditions.waitUntil(ctx.waiting, 2000) { !isOpen(ctx) }
    }
}
```

### Step 3: Run tests

Run: `cd /Users/nick/Tribot/Community-Api && ./gradlew test`
Expected: ALL PASS

### Step 4: Commit

```bash
git add src/
git commit -m "feat: add Grand Exchange API — offer management, price lookups, and GE interaction"
```

---

## Summary of commits

| # | Commit | Task |
|---|--------|------|
| 1 | `chore: add MockK and shared test fakes` | Task 0 |
| 2 | `feat: add player profile / player sense system` | Task 1 |
| 3 | `feat: add conditional waiting / polling API` | Task 2 |
| 4 | `feat: add QueryResults and LocatableQueryResults` | Task 3a |
| 5 | `feat: add QueryBuilder base and NpcQueryBuilder` | Task 3b |
| 6 | `feat: add full query API — NPC, object, ground item, player, inventory, bank builders` | Task 3c |
| 7 | `feat: add requirements API — item, skill, quest, varbit, zone, prayer, combat, and composite logic` | Task 4 |
| 8 | `feat: add loadout system — define desired gear/inventory and auto-restock from bank` | Task 5 |
| 9 | `feat: add Grand Exchange API — offer management, price lookups, and GE interaction` | Task 6 |

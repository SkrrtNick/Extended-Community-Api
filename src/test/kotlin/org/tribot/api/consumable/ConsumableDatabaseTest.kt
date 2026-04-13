package org.tribot.api.consumable

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.runelite.api.Skill
import org.tribot.api.testing.fakeContext
import org.tribot.automation.script.core.tabs.Inventory
import org.tribot.automation.script.core.tabs.InventoryItem
import org.tribot.automation.script.core.tabs.Skills
import org.tribot.automation.script.util.Waiting
import kotlin.test.*

class ConsumableDatabaseTest {

    // =========================================================================
    // Food lookup tests
    // =========================================================================

    @Test
    fun `shrimps exist and heal 3`() {
        // Source: OSRS Wiki - Shrimps
        val shrimps = ConsumableDatabase.get(315)
        assertNotNull(shrimps)
        assertEquals("Shrimps", shrimps.name)
        assertEquals(ConsumableType.FOOD, shrimps.type)
        val heal = shrimps.effects.filterIsInstance<HealEffect>().single()
        assertEquals(3, heal.amount)
    }

    @Test
    fun `lobster exists and heals 12`() {
        // Source: OSRS Wiki - Lobster
        val lobster = ConsumableDatabase.get(379)
        assertNotNull(lobster)
        assertEquals("Lobster", lobster.name)
        val heal = lobster.effects.filterIsInstance<HealEffect>().single()
        assertEquals(12, heal.amount)
    }

    @Test
    fun `swordfish exists and heals 14`() {
        // Source: OSRS Wiki - Swordfish
        val swordfish = ConsumableDatabase.get(373)
        assertNotNull(swordfish)
        assertEquals("Swordfish", swordfish.name)
        val heal = swordfish.effects.filterIsInstance<HealEffect>().single()
        assertEquals(14, heal.amount)
    }

    @Test
    fun `monkfish exists and heals 16`() {
        // Source: OSRS Wiki - Monkfish
        val monkfish = ConsumableDatabase.get(7946)
        assertNotNull(monkfish)
        assertEquals("Monkfish", monkfish.name)
        val heal = monkfish.effects.filterIsInstance<HealEffect>().single()
        assertEquals(16, heal.amount)
    }

    @Test
    fun `shark exists and heals 20`() {
        // Source: OSRS Wiki - Shark
        val shark = ConsumableDatabase.get(385)
        assertNotNull(shark)
        assertEquals("Shark", shark.name)
        val heal = shark.effects.filterIsInstance<HealEffect>().single()
        assertEquals(20, heal.amount)
    }

    @Test
    fun `manta ray exists and heals 22`() {
        // Source: OSRS Wiki - Manta ray
        val manta = ConsumableDatabase.get(391)
        assertNotNull(manta)
        assertEquals("Manta ray", manta.name)
        val heal = manta.effects.filterIsInstance<HealEffect>().single()
        assertEquals(22, heal.amount)
    }

    @Test
    fun `dark crab exists and heals 22`() {
        // Source: OSRS Wiki - Dark crab
        val darkCrab = ConsumableDatabase.get(11936)
        assertNotNull(darkCrab)
        assertEquals("Dark crab", darkCrab.name)
        val heal = darkCrab.effects.filterIsInstance<HealEffect>().single()
        assertEquals(22, heal.amount)
    }

    @Test
    fun `tuna potato exists and heals 22`() {
        // Source: OSRS Wiki - Tuna potato
        val tunaPotato = ConsumableDatabase.get(7060)
        assertNotNull(tunaPotato)
        assertEquals("Tuna potato", tunaPotato.name)
        val heal = tunaPotato.effects.filterIsInstance<HealEffect>().single()
        assertEquals(22, heal.amount)
    }

    @Test
    fun `sea turtle exists and heals 21`() {
        // Source: OSRS Wiki - Sea turtle
        val turtle = ConsumableDatabase.get(397)
        assertNotNull(turtle)
        assertEquals("Sea turtle", turtle.name)
        val heal = turtle.effects.filterIsInstance<HealEffect>().single()
        assertEquals(21, heal.amount)
    }

    @Test
    fun `karambwan is combo food and heals 18`() {
        // Source: OSRS Wiki - Cooked karambwan
        val karambwan = ConsumableDatabase.get(3144)
        assertNotNull(karambwan)
        assertEquals("Cooked karambwan", karambwan.name)
        assertEquals(ConsumableType.COMBO, karambwan.type)
        val heal = karambwan.effects.filterIsInstance<HealEffect>().single()
        assertEquals(18, heal.amount)
    }

    // =========================================================================
    // Anglerfish scaling heal tests
    // =========================================================================

    @Test
    fun `anglerfish heals 22 at 99 HP`() {
        // Source: OSRS Wiki - Anglerfish, RuneLite Anglerfish.java
        // At 99 HP: floor(99/10) + 13 = 9 + 13 = 22
        val angler = ConsumableDatabase.get(13441)
        assertNotNull(angler)
        assertEquals("Anglerfish", angler.name)
        val scalingHeal = angler.effects.filterIsInstance<ScalingHealEffect>().single()
        assertTrue(scalingHeal.overheal)
        assertEquals(22, scalingHeal.calculateHeal(99))
    }

    @Test
    fun `anglerfish heals 3 at 10 HP`() {
        // Source: OSRS Wiki - Anglerfish
        // At 10 HP: floor(10/10) + 2 = 1 + 2 = 3
        val angler = ConsumableDatabase.get(13441)!!
        val heal = angler.effects.filterIsInstance<ScalingHealEffect>().single()
        assertEquals(3, heal.calculateHeal(10))
    }

    @Test
    fun `anglerfish heals 8 at 40 HP`() {
        // Source: OSRS Wiki - Anglerfish
        // At 40 HP: floor(40/10) + 4 = 4 + 4 = 8
        val angler = ConsumableDatabase.get(13441)!!
        val heal = angler.effects.filterIsInstance<ScalingHealEffect>().single()
        assertEquals(8, heal.calculateHeal(40))
    }

    @Test
    fun `anglerfish heals 13 at 70 HP`() {
        // Source: OSRS Wiki - Anglerfish
        // At 70 HP: floor(70/10) + 6 = 7 + 6 = 13
        val angler = ConsumableDatabase.get(13441)!!
        val heal = angler.effects.filterIsInstance<ScalingHealEffect>().single()
        assertEquals(13, heal.calculateHeal(70))
    }

    @Test
    fun `anglerfish heals 17 at 90 HP`() {
        // Source: OSRS Wiki - Anglerfish
        // At 90 HP: floor(90/10) + 8 = 9 + 8 = 17
        val angler = ConsumableDatabase.get(13441)!!
        val heal = angler.effects.filterIsInstance<ScalingHealEffect>().single()
        assertEquals(17, heal.calculateHeal(90))
    }

    // =========================================================================
    // Pizza tests
    // =========================================================================

    @Test
    fun `pineapple pizza heals 11 per slice`() {
        // Source: OSRS Wiki - Pineapple pizza
        val pizza = ConsumableDatabase.get(2301)
        assertNotNull(pizza)
        assertEquals(ConsumableType.PIE, pizza.type)
        val heal = pizza.effects.filterIsInstance<HealEffect>().single()
        assertEquals(11, heal.amount)
    }

    @Test
    fun `half pineapple pizza exists`() {
        val half = ConsumableDatabase.get(2303)
        assertNotNull(half)
        assertEquals(ConsumableType.PIE, half.type)
    }

    // =========================================================================
    // Potion boost formula tests
    // =========================================================================

    @Test
    fun `attack potion boosts 10 pct plus 3`() {
        // Source: RuneLite ItemStatChanges.java - boost(ATTACK, perc(.10, 3))
        val potion = ConsumableDatabase.get(2428)  // 4-dose
        assertNotNull(potion)
        assertEquals(ConsumableType.POTION, potion.type)
        val boost = potion.effects.filterIsInstance<StatBoostEffect>().single()
        assertEquals(Skill.ATTACK, boost.skill)
        // At level 99: floor(99 * 0.10) + 3 = 9 + 3 = 12
        assertEquals(12, boost.calculateBoost(99))
        // At level 70: floor(70 * 0.10) + 3 = 7 + 3 = 10
        assertEquals(10, boost.calculateBoost(70))
    }

    @Test
    fun `super attack boosts 15 pct plus 5`() {
        // Source: RuneLite ItemStatChanges.java - boost(ATTACK, perc(.15, 5))
        val potion = ConsumableDatabase.get(2436)  // 4-dose
        assertNotNull(potion)
        val boost = potion.effects.filterIsInstance<StatBoostEffect>().single()
        assertEquals(Skill.ATTACK, boost.skill)
        // At level 99: floor(99 * 0.15) + 5 = 14 + 5 = 19
        assertEquals(19, boost.calculateBoost(99))
        // At level 75: floor(75 * 0.15) + 5 = 11 + 5 = 16
        assertEquals(16, boost.calculateBoost(75))
    }

    @Test
    fun `super strength boosts 15 pct plus 5`() {
        // Source: RuneLite ItemStatChanges.java - boost(STRENGTH, perc(.15, 5))
        val potion = ConsumableDatabase.get(2440)  // 4-dose
        assertNotNull(potion)
        val boost = potion.effects.filterIsInstance<StatBoostEffect>().single()
        assertEquals(Skill.STRENGTH, boost.skill)
        // At level 99: floor(99 * 0.15) + 5 = 14 + 5 = 19
        assertEquals(19, boost.calculateBoost(99))
    }

    @Test
    fun `super defence boosts 15 pct plus 5`() {
        // Source: RuneLite ItemStatChanges.java - boost(DEFENCE, perc(.15, 5))
        val potion = ConsumableDatabase.get(2442)  // 4-dose
        assertNotNull(potion)
        val boost = potion.effects.filterIsInstance<StatBoostEffect>().single()
        assertEquals(Skill.DEFENCE, boost.skill)
        assertEquals(19, boost.calculateBoost(99))
    }

    @Test
    fun `ranging potion boosts 10 pct plus 4`() {
        // Source: RuneLite ItemStatChanges.java - boost(RANGED, perc(.10, 4))
        val potion = ConsumableDatabase.get(2444)  // 4-dose
        assertNotNull(potion)
        val boost = potion.effects.filterIsInstance<StatBoostEffect>().single()
        assertEquals(Skill.RANGED, boost.skill)
        // At level 99: floor(99 * 0.10) + 4 = 9 + 4 = 13
        assertEquals(13, boost.calculateBoost(99))
    }

    @Test
    fun `magic potion boosts flat 4`() {
        // Source: RuneLite ItemStatChanges.java - boost(MAGIC, 4)
        val potion = ConsumableDatabase.get(3040)  // 4-dose
        assertNotNull(potion)
        val boost = potion.effects.filterIsInstance<StatBoostEffect>().single()
        assertEquals(Skill.MAGIC, boost.skill)
        // Flat 4 at any level
        assertEquals(4, boost.calculateBoost(99))
        assertEquals(4, boost.calculateBoost(1))
    }

    @Test
    fun `all attack potion doses have same effect`() {
        val ids = listOf(2428, 121, 123, 125)
        val effects = ids.map { ConsumableDatabase.get(it)!!.effects }
        assertTrue(effects.all { it == effects.first() })
    }

    // =========================================================================
    // Prayer / Restore potion tests
    // =========================================================================

    @Test
    fun `prayer potion restores 25 pct plus 7`() {
        // Source: OSRS Wiki - Prayer potion
        // Formula: floor(level * 0.25) + 7
        val potion = ConsumableDatabase.get(2434)  // 4-dose
        assertNotNull(potion)
        val restore = potion.effects.filterIsInstance<PrayerRestoreEffect>().single()
        // At level 70: floor(70 * 0.25) + 7 = 17 + 7 = 24
        assertEquals(24, restore.calculateRestore(70))
        // At level 99: floor(99 * 0.25) + 7 = 24 + 7 = 31
        assertEquals(31, restore.calculateRestore(99))
    }

    @Test
    fun `super restore restores 25 pct plus 8`() {
        // Source: OSRS Wiki - Super restore
        // Formula: floor(level * 0.25) + 8
        val potion = ConsumableDatabase.get(3024)  // 4-dose
        assertNotNull(potion)
        val restore = potion.effects.filterIsInstance<PrayerRestoreEffect>().single()
        // At level 99: floor(99 * 0.25) + 8 = 24 + 8 = 32
        assertEquals(32, restore.calculateRestore(99))
        // At level 70: floor(70 * 0.25) + 8 = 17 + 8 = 25
        assertEquals(25, restore.calculateRestore(70))
    }

    // =========================================================================
    // Saradomin brew tests
    // =========================================================================

    @Test
    fun `saradomin brew has correct effects`() {
        // Source: OSRS Wiki - Saradomin brew
        val brew = ConsumableDatabase.get(6685)  // 4-dose
        assertNotNull(brew)
        assertEquals(ConsumableType.BREW, brew.type)

        // HP heal: 15% + 2, overheal
        val heal = brew.effects.filterIsInstance<PercentHealEffect>().single()
        assertTrue(heal.overheal)
        // At 99 HP: floor(99 * 0.15) + 2 = 14 + 2 = 16
        assertEquals(16, heal.calculateHeal(99))

        // Defence boost: 20% + 2
        val defBoost = brew.effects.filterIsInstance<StatBoostEffect>()
            .single { it.skill == Skill.DEFENCE }
        // At 99 Def: floor(99 * 0.20) + 2 = 19 + 2 = 21
        assertEquals(21, defBoost.calculateBoost(99))

        // Attack drain: 10% + 2
        val atkDrain = brew.effects.filterIsInstance<StatDrainEffect>()
            .single { it.skill == Skill.ATTACK }
        // At 99 Atk: floor(99 * 0.10) + 2 = 9 + 2 = 11
        assertEquals(11, atkDrain.calculateDrain(99))

        // Strength drain: 10% + 2
        val strDrain = brew.effects.filterIsInstance<StatDrainEffect>()
            .single { it.skill == Skill.STRENGTH }
        assertEquals(11, strDrain.calculateDrain(99))

        // Magic drain: 10% + 2
        val magDrain = brew.effects.filterIsInstance<StatDrainEffect>()
            .single { it.skill == Skill.MAGIC }
        assertEquals(11, magDrain.calculateDrain(99))

        // Ranged drain: 10% + 2
        val rngDrain = brew.effects.filterIsInstance<StatDrainEffect>()
            .single { it.skill == Skill.RANGED }
        assertEquals(11, rngDrain.calculateDrain(99))
    }

    @Test
    fun `all saradomin brew doses exist`() {
        assertNotNull(ConsumableDatabase.get(6685))
        assertNotNull(ConsumableDatabase.get(6687))
        assertNotNull(ConsumableDatabase.get(6689))
        assertNotNull(ConsumableDatabase.get(6691))
    }

    // =========================================================================
    // Energy / Stamina potion tests
    // =========================================================================

    @Test
    fun `energy potion restores 10 run energy`() {
        // Source: OSRS Wiki - Energy potion
        val potion = ConsumableDatabase.get(3008)  // 4-dose
        assertNotNull(potion)
        val effect = potion.effects.filterIsInstance<RunEnergyEffect>().single()
        assertEquals(10, effect.amount)
    }

    @Test
    fun `super energy restores 20 run energy`() {
        // Source: OSRS Wiki - Super energy potion
        val potion = ConsumableDatabase.get(3016)  // 4-dose
        assertNotNull(potion)
        val effect = potion.effects.filterIsInstance<RunEnergyEffect>().single()
        assertEquals(20, effect.amount)
    }

    @Test
    fun `stamina potion restores 20 run energy`() {
        // Source: OSRS Wiki - Stamina potion
        val potion = ConsumableDatabase.get(12625)  // 4-dose
        assertNotNull(potion)
        val effect = potion.effects.filterIsInstance<RunEnergyEffect>().single()
        assertEquals(20, effect.amount)
    }

    // =========================================================================
    // Name lookup tests
    // =========================================================================

    @Test
    fun `lookup by name is case insensitive`() {
        val shark = ConsumableDatabase.getByName("SHARK")
        assertNotNull(shark)
        assertEquals(385, shark.itemId)

        val shark2 = ConsumableDatabase.getByName("shark")
        assertEquals(shark, shark2)
    }

    @Test
    fun `lookup by name returns null for unknown`() {
        assertNull(ConsumableDatabase.getByName("nonexistent food"))
    }

    // =========================================================================
    // Collection query tests
    // =========================================================================

    @Test
    fun `getAllFood returns only food and combo types`() {
        val foods = ConsumableDatabase.getAllFood()
        assertTrue(foods.isNotEmpty())
        assertTrue(foods.all { it.type == ConsumableType.FOOD || it.type == ConsumableType.COMBO })
    }

    @Test
    fun `getAllPotions returns only potion type`() {
        val potions = ConsumableDatabase.getAllPotions()
        assertTrue(potions.isNotEmpty())
        assertTrue(potions.all { it.type == ConsumableType.POTION })
    }

    @Test
    fun `getAllBrews returns only brew type`() {
        val brews = ConsumableDatabase.getAllBrews()
        assertTrue(brews.isNotEmpty())
        assertTrue(brews.all { it.type == ConsumableType.BREW })
    }

    // =========================================================================
    // Effect calculation tests
    // =========================================================================

    @Test
    fun `StatBoostEffect calculates correctly`() {
        val effect = StatBoostEffect(Skill.ATTACK, 0.15, 5)
        // floor(99 * 0.15) + 5 = 14 + 5 = 19
        assertEquals(19, effect.calculateBoost(99))
        // floor(75 * 0.15) + 5 = 11 + 5 = 16
        assertEquals(16, effect.calculateBoost(75))
        // floor(1 * 0.15) + 5 = 0 + 5 = 5
        assertEquals(5, effect.calculateBoost(1))
    }

    @Test
    fun `StatDrainEffect calculates correctly`() {
        val effect = StatDrainEffect(Skill.ATTACK, 0.10, 2)
        // floor(99 * 0.10) + 2 = 9 + 2 = 11
        assertEquals(11, effect.calculateDrain(99))
    }

    @Test
    fun `PrayerRestoreEffect calculates correctly`() {
        val effect = PrayerRestoreEffect(0.25, 7)
        // floor(99 * 0.25) + 7 = 24 + 7 = 31
        assertEquals(31, effect.calculateRestore(99))
        // floor(52 * 0.25) + 7 = 13 + 7 = 20
        assertEquals(20, effect.calculateRestore(52))
    }

    @Test
    fun `PercentHealEffect calculates correctly`() {
        val effect = PercentHealEffect(0.15, 2, overheal = true)
        // floor(99 * 0.15) + 2 = 14 + 2 = 16
        assertEquals(16, effect.calculateHeal(99))
        // floor(80 * 0.15) + 2 = 12 + 2 = 14
        assertEquals(14, effect.calculateHeal(80))
    }

    @Test
    fun `HealEffect description is correct`() {
        assertEquals("Heal 20 HP", HealEffect(20).description)
        assertEquals("Boost 22 HP", HealEffect(22, overheal = true).description)
    }

    // =========================================================================
    // ConsumableHelper tests (with mocked ScriptContext)
    // =========================================================================

    @Test
    fun `eat returns false when item not in inventory`() {
        val inventory = mockk<Inventory>(relaxed = true)
        every { inventory.contains(385) } returns false

        val ctx = fakeContext {
            every { this@fakeContext.inventory } returns inventory
        }

        assertFalse(ConsumableHelper.eat(ctx, 385))
    }

    @Test
    fun `eat clicks item and waits for HP change`() {
        val inventory = mockk<Inventory>(relaxed = true)
        val skills = mockk<Skills>(relaxed = true)
        val waiting = mockk<Waiting>(relaxed = true)

        every { inventory.contains(385) } returns true
        every { inventory.clickItem(385, "Eat") } returns true

        // HP changes from 80 to 99 after eating
        var hpCallCount = 0
        every { skills.getBoostedLevel(Skill.HITPOINTS) } answers {
            if (hpCallCount++ == 0) 80 else 99
        }

        val ctx = fakeContext {
            every { this@fakeContext.inventory } returns inventory
            every { this@fakeContext.skills } returns skills
            every { this@fakeContext.waiting } returns waiting
        }

        val result = ConsumableHelper.eat(ctx, 385)
        assertTrue(result)
        verify { inventory.clickItem(385, "Eat") }
    }

    @Test
    fun `drink returns false when item not in inventory`() {
        val inventory = mockk<Inventory>(relaxed = true)
        every { inventory.contains(2434) } returns false

        val ctx = fakeContext {
            every { this@fakeContext.inventory } returns inventory
        }

        assertFalse(ConsumableHelper.drink(ctx, 2434))
    }

    @Test
    fun `drink clicks item with Drink option`() {
        val inventory = mockk<Inventory>(relaxed = true)
        val waiting = mockk<Waiting>(relaxed = true)

        // First call: item exists; second call (in wait condition): item consumed
        var containsCallCount = 0
        every { inventory.contains(2434) } answers {
            containsCallCount++ == 0
        }
        every { inventory.clickItem(2434, "Drink") } returns true

        val ctx = fakeContext {
            every { this@fakeContext.inventory } returns inventory
            every { this@fakeContext.waiting } returns waiting
        }

        val result = ConsumableHelper.drink(ctx, 2434)
        assertTrue(result)
        verify { inventory.clickItem(2434, "Drink") }
    }

    @Test
    fun `calculateHealing works for flat heal`() {
        val shark = ConsumableDatabase.get(385)!!
        assertEquals(20, ConsumableHelper.calculateHealing(shark, 99))
    }

    @Test
    fun `calculateHealing works for percent heal`() {
        val brew = ConsumableDatabase.get(6685)!!
        // Saradomin brew at 99 HP: floor(99 * 0.15) + 2 = 16
        assertEquals(16, ConsumableHelper.calculateHealing(brew, 99))
    }

    @Test
    fun `calculateHealing works for scaling heal`() {
        val angler = ConsumableDatabase.get(13441)!!
        assertEquals(22, ConsumableHelper.calculateHealing(angler, 99))
        assertEquals(3, ConsumableHelper.calculateHealing(angler, 10))
    }

    @Test
    fun `findBestFood returns highest healing food`() {
        val sharkItem = InventoryItem(385, 1, 0)   // Shark heals 20
        val lobsterItem = InventoryItem(379, 1, 1)  // Lobster heals 12

        val inventory = mockk<Inventory>(relaxed = true)
        every { inventory.getItems() } returns listOf(sharkItem, lobsterItem)

        val ctx = fakeContext {
            every { this@fakeContext.inventory } returns inventory
        }

        val best = ConsumableHelper.findBestFood(ctx)
        assertNotNull(best)
        assertEquals(385, best.itemId)  // Shark
    }
}

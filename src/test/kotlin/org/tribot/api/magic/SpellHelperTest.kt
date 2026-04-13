package org.tribot.api.magic

import kotlin.test.*

/**
 * Tests for [SpellHelper] using fake implementations of the SDK interfaces.
 */
class SpellHelperTest {

    private lateinit var fakeSkills: FakeSkills
    private lateinit var fakeInventory: FakeInventory
    private lateinit var fakeMagic: FakeMagic

    @BeforeTest
    fun setUp() {
        fakeSkills = FakeSkills()
        fakeInventory = FakeInventory()
        fakeMagic = FakeMagic()
    }

    // -------------------------------------------------------------------
    // cast()
    // -------------------------------------------------------------------

    @Test
    fun `cast delegates to SDK magic cast with correct spell name`() {
        val ctx = fakeContext(fakeSkills, fakeInventory, fakeMagic)
        SpellHelper.cast(ctx, Spell.HIGH_LEVEL_ALCHEMY)
        assertEquals("High Level Alchemy", fakeMagic.lastCast)
    }

    @Test
    fun `cast returns true when SDK returns true`() {
        fakeMagic.castResult = true
        val ctx = fakeContext(fakeSkills, fakeInventory, fakeMagic)
        assertTrue(SpellHelper.cast(ctx, Spell.WIND_STRIKE))
    }

    @Test
    fun `cast returns false when SDK returns false`() {
        fakeMagic.castResult = false
        val ctx = fakeContext(fakeSkills, fakeInventory, fakeMagic)
        assertFalse(SpellHelper.cast(ctx, Spell.WIND_STRIKE))
    }

    // -------------------------------------------------------------------
    // canCast()
    // -------------------------------------------------------------------

    @Test
    fun `canCast returns false when level is too low`() {
        fakeSkills.magicLevel = 54  // High Alch needs 55
        // Even with enough runes, level should fail
        fakeInventory.setCount(RuneType.FIRE.itemId, 5)
        fakeInventory.setCount(RuneType.NATURE.itemId, 1)
        val ctx = fakeContext(fakeSkills, fakeInventory, fakeMagic)
        assertFalse(SpellHelper.canCast(ctx, Spell.HIGH_LEVEL_ALCHEMY))
    }

    @Test
    fun `canCast returns false when missing runes`() {
        fakeSkills.magicLevel = 99
        // Only fire runes, no nature rune
        fakeInventory.setCount(RuneType.FIRE.itemId, 5)
        val ctx = fakeContext(fakeSkills, fakeInventory, fakeMagic)
        assertFalse(SpellHelper.canCast(ctx, Spell.HIGH_LEVEL_ALCHEMY))
    }

    @Test
    fun `canCast returns false when not enough runes`() {
        fakeSkills.magicLevel = 99
        fakeInventory.setCount(RuneType.FIRE.itemId, 4)  // need 5
        fakeInventory.setCount(RuneType.NATURE.itemId, 1)
        val ctx = fakeContext(fakeSkills, fakeInventory, fakeMagic)
        assertFalse(SpellHelper.canCast(ctx, Spell.HIGH_LEVEL_ALCHEMY))
    }

    @Test
    fun `canCast returns true when level and runes are sufficient`() {
        fakeSkills.magicLevel = 55
        fakeInventory.setCount(RuneType.FIRE.itemId, 5)
        fakeInventory.setCount(RuneType.NATURE.itemId, 1)
        val ctx = fakeContext(fakeSkills, fakeInventory, fakeMagic)
        assertTrue(SpellHelper.canCast(ctx, Spell.HIGH_LEVEL_ALCHEMY))
    }

    @Test
    fun `canCast returns true with excess runes`() {
        fakeSkills.magicLevel = 99
        fakeInventory.setCount(RuneType.FIRE.itemId, 100)
        fakeInventory.setCount(RuneType.NATURE.itemId, 500)
        val ctx = fakeContext(fakeSkills, fakeInventory, fakeMagic)
        assertTrue(SpellHelper.canCast(ctx, Spell.HIGH_LEVEL_ALCHEMY))
    }

    @Test
    fun `canCast works for multi-rune spells like Ice Barrage`() {
        fakeSkills.magicLevel = 94
        fakeInventory.setCount(RuneType.WATER.itemId, 6)
        fakeInventory.setCount(RuneType.BLOOD.itemId, 2)
        fakeInventory.setCount(RuneType.DEATH.itemId, 4)
        val ctx = fakeContext(fakeSkills, fakeInventory, fakeMagic)
        assertTrue(SpellHelper.canCast(ctx, Spell.ICE_BARRAGE))
    }

    @Test
    fun `canCast for Wind Strike at level 1 with 1 air 1 mind`() {
        fakeSkills.magicLevel = 1
        fakeInventory.setCount(RuneType.AIR.itemId, 1)
        fakeInventory.setCount(RuneType.MIND.itemId, 1)
        val ctx = fakeContext(fakeSkills, fakeInventory, fakeMagic)
        assertTrue(SpellHelper.canCast(ctx, Spell.WIND_STRIKE))
    }

    // -------------------------------------------------------------------
    // getAvailableSpells()
    // -------------------------------------------------------------------

    @Test
    fun `getAvailableSpells returns empty when level is 0`() {
        fakeSkills.magicLevel = 0
        val ctx = fakeContext(fakeSkills, fakeInventory, fakeMagic)
        val spells = SpellHelper.getAvailableSpells(ctx, Spellbook.STANDARD)
        assertTrue(spells.isEmpty())
    }

    @Test
    fun `getAvailableSpells at level 1 includes Wind Strike only from combat`() {
        fakeSkills.magicLevel = 1
        val ctx = fakeContext(fakeSkills, fakeInventory, fakeMagic)
        val spells = SpellHelper.getAvailableSpells(ctx, Spellbook.STANDARD)
        assertTrue(spells.contains(Spell.WIND_STRIKE))
        assertFalse(spells.contains(Spell.WATER_STRIKE)) // needs level 5
    }

    @Test
    fun `getAvailableSpells only returns spells from the requested spellbook`() {
        fakeSkills.magicLevel = 99
        val ctx = fakeContext(fakeSkills, fakeInventory, fakeMagic)
        val ancientSpells = SpellHelper.getAvailableSpells(ctx, Spellbook.ANCIENT)
        ancientSpells.forEach { spell ->
            assertEquals(Spellbook.ANCIENT, spell.spellbook)
        }
    }

    @Test
    fun `getAvailableSpells at level 99 returns all standard spells except level 99+`() {
        fakeSkills.magicLevel = 99
        val ctx = fakeContext(fakeSkills, fakeInventory, fakeMagic)
        val spells = SpellHelper.getAvailableSpells(ctx, Spellbook.STANDARD)
        assertTrue(spells.contains(Spell.FIRE_SURGE)) // level 95
        assertTrue(spells.contains(Spell.WIND_STRIKE)) // level 1
    }

    // -------------------------------------------------------------------
    // getCastableSpells()
    // -------------------------------------------------------------------

    @Test
    fun `getCastableSpells filters by both level and runes`() {
        fakeSkills.magicLevel = 99
        // Only have enough runes for Wind Strike
        fakeInventory.setCount(RuneType.AIR.itemId, 1)
        fakeInventory.setCount(RuneType.MIND.itemId, 1)
        val ctx = fakeContext(fakeSkills, fakeInventory, fakeMagic)
        val castable = SpellHelper.getCastableSpells(ctx, Spellbook.STANDARD)
        assertTrue(castable.contains(Spell.WIND_STRIKE))
        // Should not contain Fire Strike (needs 2 air, 3 fire, 1 mind — we have 0 fire)
        assertFalse(castable.contains(Spell.FIRE_STRIKE))
    }
}

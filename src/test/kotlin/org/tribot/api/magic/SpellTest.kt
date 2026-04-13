package org.tribot.api.magic

import kotlin.test.*

/**
 * Tests for the [Spell] enum data integrity.
 * All expected values are sourced from the OSRS Wiki.
 */
class SpellTest {

    // -------------------------------------------------------------------
    // Spell data correctness (level, runes, spellbook)
    // -------------------------------------------------------------------

    @Test
    fun `High Level Alchemy requires level 55 and 5 fire 1 nature`() {
        val spell = Spell.HIGH_LEVEL_ALCHEMY
        assertEquals(55, spell.level)
        assertEquals(Spellbook.STANDARD, spell.spellbook)
        assertEquals("High Level Alchemy", spell.spellName)
        assertEquals(SpellType.ALCHEMY, spell.type)
        assertContainsRune(spell, RuneType.FIRE, 5)
        assertContainsRune(spell, RuneType.NATURE, 1)
        assertEquals(2, spell.runes.size)
    }

    @Test
    fun `Ice Barrage requires level 94 and 6 water 2 blood 4 death`() {
        val spell = Spell.ICE_BARRAGE
        assertEquals(94, spell.level)
        assertEquals(Spellbook.ANCIENT, spell.spellbook)
        assertEquals("Ice Barrage", spell.spellName)
        assertEquals(SpellType.COMBAT, spell.type)
        assertContainsRune(spell, RuneType.WATER, 6)
        assertContainsRune(spell, RuneType.BLOOD, 2)
        assertContainsRune(spell, RuneType.DEATH, 4)
        assertEquals(3, spell.runes.size)
    }

    @Test
    fun `Vengeance requires level 94 and 10 earth 4 astral 2 death`() {
        val spell = Spell.VENGEANCE
        assertEquals(94, spell.level)
        assertEquals(Spellbook.LUNAR, spell.spellbook)
        assertEquals("Vengeance", spell.spellName)
        assertContainsRune(spell, RuneType.EARTH, 10)
        assertContainsRune(spell, RuneType.ASTRAL, 4)
        assertContainsRune(spell, RuneType.DEATH, 2)
        assertEquals(3, spell.runes.size)
    }

    @Test
    fun `Wind Strike requires level 1 and 1 air 1 mind`() {
        val spell = Spell.WIND_STRIKE
        assertEquals(1, spell.level)
        assertEquals(Spellbook.STANDARD, spell.spellbook)
        assertContainsRune(spell, RuneType.AIR, 1)
        assertContainsRune(spell, RuneType.MIND, 1)
        assertEquals(2, spell.runes.size)
    }

    @Test
    fun `Fire Surge requires level 95 and 7 air 10 fire 1 wrath`() {
        val spell = Spell.FIRE_SURGE
        assertEquals(95, spell.level)
        assertContainsRune(spell, RuneType.AIR, 7)
        assertContainsRune(spell, RuneType.FIRE, 10)
        assertContainsRune(spell, RuneType.WRATH, 1)
        assertEquals(3, spell.runes.size)
    }

    @Test
    fun `Low Level Alchemy requires level 21 and 3 fire 1 nature`() {
        val spell = Spell.LOW_LEVEL_ALCHEMY
        assertEquals(21, spell.level)
        assertContainsRune(spell, RuneType.FIRE, 3)
        assertContainsRune(spell, RuneType.NATURE, 1)
    }

    @Test
    fun `Superheat Item requires level 43 and 4 fire 1 nature`() {
        val spell = Spell.SUPERHEAT_ITEM
        assertEquals(43, spell.level)
        assertContainsRune(spell, RuneType.FIRE, 4)
        assertContainsRune(spell, RuneType.NATURE, 1)
    }

    @Test
    fun `Camelot Teleport requires level 45 and 5 air 1 law`() {
        val spell = Spell.CAMELOT_TELEPORT
        assertEquals(45, spell.level)
        assertEquals(SpellType.TELEPORT, spell.type)
        assertContainsRune(spell, RuneType.AIR, 5)
        assertContainsRune(spell, RuneType.LAW, 1)
    }

    @Test
    fun `Plank Make requires level 86 and 15 earth 2 astral 1 nature`() {
        val spell = Spell.PLANK_MAKE
        assertEquals(86, spell.level)
        assertEquals(Spellbook.LUNAR, spell.spellbook)
        assertContainsRune(spell, RuneType.EARTH, 15)
        assertContainsRune(spell, RuneType.ASTRAL, 2)
        assertContainsRune(spell, RuneType.NATURE, 1)
    }

    @Test
    fun `Tan Leather requires level 78 and 5 fire 2 astral 1 nature`() {
        val spell = Spell.TAN_LEATHER
        assertEquals(78, spell.level)
        assertEquals(Spellbook.LUNAR, spell.spellbook)
        assertContainsRune(spell, RuneType.FIRE, 5)
        assertContainsRune(spell, RuneType.ASTRAL, 2)
        assertContainsRune(spell, RuneType.NATURE, 1)
    }

    @Test
    fun `Blood Barrage requires level 92 and 4 blood 4 death 1 soul`() {
        val spell = Spell.BLOOD_BARRAGE
        assertEquals(92, spell.level)
        assertEquals(Spellbook.ANCIENT, spell.spellbook)
        assertContainsRune(spell, RuneType.BLOOD, 4)
        assertContainsRune(spell, RuneType.DEATH, 4)
        assertContainsRune(spell, RuneType.SOUL, 1)
    }

    @Test
    fun `Smoke Barrage requires level 86 and 4 air 4 fire 2 blood 4 death`() {
        val spell = Spell.SMOKE_BARRAGE
        assertEquals(86, spell.level)
        assertContainsRune(spell, RuneType.AIR, 4)
        assertContainsRune(spell, RuneType.FIRE, 4)
        assertContainsRune(spell, RuneType.BLOOD, 2)
        assertContainsRune(spell, RuneType.DEATH, 4)
    }

    @Test
    fun `Basic Reanimation requires level 16 and 4 body 2 nature`() {
        val spell = Spell.BASIC_REANIMATION
        assertEquals(16, spell.level)
        assertEquals(Spellbook.ARCEUUS, spell.spellbook)
        assertContainsRune(spell, RuneType.BODY, 4)
        assertContainsRune(spell, RuneType.NATURE, 2)
    }

    @Test
    fun `Demonic Offering requires level 84 and 1 soul 1 wrath`() {
        val spell = Spell.DEMONIC_OFFERING
        assertEquals(84, spell.level)
        assertEquals(Spellbook.ARCEUUS, spell.spellbook)
        assertContainsRune(spell, RuneType.SOUL, 1)
        assertContainsRune(spell, RuneType.WRATH, 1)
    }

    @Test
    fun `NPC Contact requires level 67 and 2 air 1 astral 1 cosmic`() {
        val spell = Spell.NPC_CONTACT
        assertEquals(67, spell.level)
        assertEquals(Spellbook.LUNAR, spell.spellbook)
        assertContainsRune(spell, RuneType.AIR, 2)
        assertContainsRune(spell, RuneType.ASTRAL, 1)
        assertContainsRune(spell, RuneType.COSMIC, 1)
    }

    @Test
    fun `Humidify requires level 68 and 1 fire 3 water 1 astral`() {
        val spell = Spell.HUMIDIFY
        assertEquals(68, spell.level)
        assertEquals(Spellbook.LUNAR, spell.spellbook)
        assertContainsRune(spell, RuneType.FIRE, 1)
        assertContainsRune(spell, RuneType.WATER, 3)
        assertContainsRune(spell, RuneType.ASTRAL, 1)
    }

    @Test
    fun `String Jewellery requires level 80 and 10 earth 5 water 2 astral`() {
        val spell = Spell.STRING_JEWELLERY
        assertEquals(80, spell.level)
        assertContainsRune(spell, RuneType.EARTH, 10)
        assertContainsRune(spell, RuneType.WATER, 5)
        assertContainsRune(spell, RuneType.ASTRAL, 2)
    }

    @Test
    fun `Spin Flax requires level 76 and 5 air 1 astral 2 nature`() {
        val spell = Spell.SPIN_FLAX
        assertEquals(76, spell.level)
        assertContainsRune(spell, RuneType.AIR, 5)
        assertContainsRune(spell, RuneType.ASTRAL, 1)
        assertContainsRune(spell, RuneType.NATURE, 2)
    }

    // -------------------------------------------------------------------
    // Spell name format
    // -------------------------------------------------------------------

    @Test
    fun `all spell names are non-blank`() {
        Spell.entries.forEach { spell ->
            assertTrue(spell.spellName.isNotBlank(), "${spell.name} has blank spellName")
        }
    }

    @Test
    fun `no duplicate spell names within the same spellbook`() {
        Spell.entries.groupBy { it.spellbook }.forEach { (book, spells) ->
            val names = spells.map { it.spellName }
            val dupes = names.groupBy { it }.filter { it.value.size > 1 }.keys
            // Teleport to Target exists on all spellbooks — that's the one allowed duplicate
            // Ape Atoll Teleport exists on Standard and Arceuus — allowed
            // We check only truly unexpected duplicates within a single spellbook
            assertTrue(
                dupes.isEmpty(),
                "Duplicate spell names in $book: $dupes"
            )
        }
    }

    @Test
    fun `spell levels are positive`() {
        Spell.entries.forEach { spell ->
            assertTrue(spell.level >= 1, "${spell.name} has level ${spell.level} < 1")
        }
    }

    @Test
    fun `all rune requirements have positive amounts`() {
        Spell.entries.forEach { spell ->
            spell.runes.forEach { req ->
                assertTrue(req.amount > 0, "${spell.name} has ${req.rune} with amount ${req.amount}")
            }
        }
    }

    // -------------------------------------------------------------------
    // Companion helpers
    // -------------------------------------------------------------------

    @Test
    fun `fromName returns spell for valid name case-insensitive`() {
        assertEquals(Spell.ICE_BARRAGE, Spell.fromName("ice barrage"))
        assertEquals(Spell.ICE_BARRAGE, Spell.fromName("Ice Barrage"))
        assertNull(Spell.fromName("Nonexistent Spell"))
    }

    @Test
    fun `forSpellbook returns only spells from that book`() {
        val ancients = Spell.forSpellbook(Spellbook.ANCIENT)
        assertTrue(ancients.isNotEmpty())
        ancients.forEach { assertEquals(Spellbook.ANCIENT, it.spellbook) }
    }

    @Test
    fun `forType returns only spells of that type`() {
        val teleports = Spell.forType(SpellType.TELEPORT)
        assertTrue(teleports.isNotEmpty())
        teleports.forEach { assertEquals(SpellType.TELEPORT, it.type) }
    }

    // -------------------------------------------------------------------
    // Spellbook coverage
    // -------------------------------------------------------------------

    @Test
    fun `standard spellbook has strikes bolts blasts waves and surges`() {
        val std = Spell.forSpellbook(Spellbook.STANDARD)
        val names = std.map { it.spellName }
        // Check one representative from each tier
        assertTrue("Wind Strike" in names)
        assertTrue("Water Bolt" in names)
        assertTrue("Fire Blast" in names)
        assertTrue("Earth Wave" in names)
        assertTrue("Fire Surge" in names)
    }

    @Test
    fun `ancient magicks has all 16 combat spells`() {
        val ancientCombat = Spell.entries.filter {
            it.spellbook == Spellbook.ANCIENT && it.type == SpellType.COMBAT
        }
        assertEquals(16, ancientCombat.size, "Ancient Magicks should have exactly 16 combat spells")
    }

    @Test
    fun `ancient magicks has 8 teleports (excluding home and minigame)`() {
        val ancientTele = Spell.entries.filter {
            it.spellbook == Spellbook.ANCIENT && it.type == SpellType.TELEPORT
        }
        assertEquals(8, ancientTele.size, "Ancient Magicks should have 8 teleport spells")
    }

    // -------------------------------------------------------------------
    // RuneType
    // -------------------------------------------------------------------

    @Test
    fun `RuneType item IDs are unique`() {
        val ids = RuneType.entries.map { it.itemId }
        assertEquals(ids.size, ids.distinct().size, "Duplicate rune item IDs detected")
    }

    @Test
    fun `RuneType fromItemId returns correct type`() {
        assertEquals(RuneType.AIR, RuneType.fromItemId(556))
        assertEquals(RuneType.ASTRAL, RuneType.fromItemId(9075))
        assertEquals(RuneType.WRATH, RuneType.fromItemId(21880))
        assertNull(RuneType.fromItemId(99999))
    }

    @Test
    fun `known rune item IDs are correct`() {
        assertEquals(556, RuneType.AIR.itemId)
        assertEquals(555, RuneType.WATER.itemId)
        assertEquals(557, RuneType.EARTH.itemId)
        assertEquals(554, RuneType.FIRE.itemId)
        assertEquals(558, RuneType.MIND.itemId)
        assertEquals(559, RuneType.BODY.itemId)
        assertEquals(564, RuneType.COSMIC.itemId)
        assertEquals(562, RuneType.CHAOS.itemId)
        assertEquals(561, RuneType.NATURE.itemId)
        assertEquals(563, RuneType.LAW.itemId)
        assertEquals(560, RuneType.DEATH.itemId)
        assertEquals(9075, RuneType.ASTRAL.itemId)
        assertEquals(565, RuneType.BLOOD.itemId)
        assertEquals(566, RuneType.SOUL.itemId)
        assertEquals(21880, RuneType.WRATH.itemId)
    }

    // -------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------

    private fun assertContainsRune(spell: Spell, rune: RuneType, amount: Int) {
        val req = spell.runes.find { it.rune == rune }
        assertNotNull(req, "${spell.spellName} should require ${rune.name} runes")
        assertEquals(amount, req.amount, "${spell.spellName} should require $amount ${rune.name} runes")
    }
}

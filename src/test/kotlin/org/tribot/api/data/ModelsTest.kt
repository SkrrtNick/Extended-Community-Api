package org.tribot.api.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals

class ItemDefinitionTest {

    @Test
    fun `construct with all fields`() {
        val equip = EquipmentStats(
            slot = "weapon",
            attackStab = 82, attackSlash = 82, attackCrush = -2,
            attackMagic = 0, attackRanged = 0,
            defenceStab = 0, defenceSlash = 0, defenceCrush = 0,
            defenceMagic = 0, defenceRanged = 0,
            meleeStrength = 82, rangedStrength = 0,
            magicDamage = 0.0, prayer = 0,
            requirements = mapOf("attack" to 70)
        )
        val weapon = WeaponStats(
            attackSpeed = 4,
            weaponType = "slash_sword",
            stances = listOf(
                WeaponStance("Chop", "Slash", "Accurate", "attack", null),
                WeaponStance("Slash", "Slash", "Aggressive", "strength", null),
                WeaponStance("Lunge", "Stab", "Controlled", "shared", null),
                WeaponStance("Block", "Slash", "Defensive", "defence", null)
            )
        )
        val item = ItemDefinition(
            id = 4151,
            name = "Abyssal whip",
            members = true,
            tradeable = true,
            tradeableOnGe = true,
            stackable = false,
            cost = 120001,
            highAlch = 72000,
            lowAlch = 48000,
            buyLimit = 70,
            weight = 0.453,
            examine = "A weapon from the abyss.",
            questItem = false,
            equipment = equip,
            weapon = weapon
        )
        assertEquals(4151, item.id)
        assertEquals("Abyssal whip", item.name)
        assertTrue(item.members)
        assertTrue(item.tradeable)
        assertTrue(item.tradeableOnGe)
        assertFalse(item.stackable)
        assertEquals(120001, item.cost)
        assertEquals(72000, item.highAlch)
        assertEquals(48000, item.lowAlch)
        assertEquals(70, item.buyLimit)
        assertEquals(0.453, item.weight)
        assertEquals("A weapon from the abyss.", item.examine)
        assertFalse(item.questItem)
        assertEquals(equip, item.equipment)
        assertEquals(weapon, item.weapon)
    }

    @Test
    fun `construct with nullable fields as null`() {
        val item = ItemDefinition(
            id = 995,
            name = "Coins",
            members = false,
            tradeable = true,
            tradeableOnGe = false,
            stackable = true,
            cost = 1,
            highAlch = null,
            lowAlch = null,
            buyLimit = null,
            weight = null,
            examine = null,
            questItem = false,
            equipment = null,
            weapon = null
        )
        assertEquals(995, item.id)
        assertEquals("Coins", item.name)
        assertNull(item.highAlch)
        assertNull(item.lowAlch)
        assertNull(item.buyLimit)
        assertNull(item.weight)
        assertNull(item.examine)
        assertNull(item.equipment)
        assertNull(item.weapon)
    }

    @Test
    fun `copy and equality`() {
        val item = ItemDefinition(
            id = 995, name = "Coins", members = false, tradeable = true,
            tradeableOnGe = false, stackable = true, cost = 1,
            highAlch = null, lowAlch = null, buyLimit = null, weight = null,
            examine = null, questItem = false, equipment = null, weapon = null
        )
        val copy = item.copy(name = "Gold coins")
        assertEquals("Coins", item.name)
        assertEquals("Gold coins", copy.name)
        assertNotEquals(item, copy)
        assertEquals(item, item.copy())
    }
}

class EquipmentStatsTest {

    @Test
    fun `construct and verify fields`() {
        val stats = EquipmentStats(
            slot = "head",
            attackStab = 0, attackSlash = 0, attackCrush = 0,
            attackMagic = -6, attackRanged = -3,
            defenceStab = 30, defenceSlash = 32, defenceCrush = 27,
            defenceMagic = -1, defenceRanged = 30,
            meleeStrength = 0, rangedStrength = 0,
            magicDamage = 0.0, prayer = 1,
            requirements = mapOf("defence" to 40)
        )
        assertEquals("head", stats.slot)
        assertEquals(30, stats.defenceStab)
        assertEquals(1, stats.prayer)
        assertEquals(mapOf("defence" to 40), stats.requirements)
    }

    @Test
    fun `empty requirements map`() {
        val stats = EquipmentStats(
            slot = "ring",
            attackStab = 0, attackSlash = 0, attackCrush = 0,
            attackMagic = 0, attackRanged = 0,
            defenceStab = 0, defenceSlash = 0, defenceCrush = 0,
            defenceMagic = 0, defenceRanged = 0,
            meleeStrength = 0, rangedStrength = 0,
            magicDamage = 0.0, prayer = 0,
            requirements = emptyMap()
        )
        assertTrue(stats.requirements.isEmpty())
    }
}

class WeaponStatsTest {

    @Test
    fun `construct with stances`() {
        val stances = listOf(
            WeaponStance("Accurate", "Ranged", "Accurate", "ranged", null),
            WeaponStance("Rapid", "Ranged", "Rapid", "ranged", null),
            WeaponStance("Longrange", "Ranged", "Longrange", "ranged and defence", null)
        )
        val weapon = WeaponStats(attackSpeed = 4, weaponType = "bow", stances = stances)
        assertEquals(4, weapon.attackSpeed)
        assertEquals("bow", weapon.weaponType)
        assertEquals(3, weapon.stances.size)
    }
}

class WeaponStanceTest {

    @Test
    fun `construct with all fields`() {
        val stance = WeaponStance("Chop", "Slash", "Accurate", "attack", null)
        assertEquals("Chop", stance.combatStyle)
        assertEquals("Slash", stance.attackType)
        assertEquals("Accurate", stance.attackStyle)
        assertEquals("attack", stance.experience)
        assertNull(stance.boosts)
    }

    @Test
    fun `construct with nullable fields`() {
        val stance = WeaponStance("Spell", null, null, null, "Magic XP")
        assertEquals("Spell", stance.combatStyle)
        assertNull(stance.attackType)
        assertNull(stance.attackStyle)
        assertNull(stance.experience)
        assertEquals("Magic XP", stance.boosts)
    }
}

class MonsterDefinitionTest {

    @Test
    fun `construct with all fields`() {
        val monster = MonsterDefinition(
            id = 415,
            name = "Abyssal demon",
            members = true,
            combatLevel = 124,
            hitpoints = 150,
            maxHit = "8",
            attackSpeed = 4,
            size = 1,
            attackLevel = 97, strengthLevel = 67, defenceLevel = 135,
            magicLevel = 1, rangedLevel = 1,
            attackStab = 0, attackSlash = 0, attackCrush = 0,
            attackMagic = 0, attackRanged = 0,
            defenceStab = 20, defenceSlash = 20, defenceCrush = 20,
            defenceMagic = 0, defenceRanged = 20,
            strengthBonus = 0, rangedStrengthBonus = 0, magicDamageBonus = 0,
            slayerLevel = 85, slayerXp = 150.0, slayerCategory = "Abyssal demons",
            assignedBy = "Nieve|Duradel",
            elementalWeakness = null, elementalWeaknessPercent = null,
            poisonous = "No",
            immunePoison = false, immuneVenom = false,
            examine = "A denizen of the Abyss!"
        )
        assertEquals(415, monster.id)
        assertEquals("Abyssal demon", monster.name)
        assertTrue(monster.members)
        assertEquals(124, monster.combatLevel)
        assertEquals(150, monster.hitpoints)
        assertEquals(85, monster.slayerLevel)
        assertEquals(150.0, monster.slayerXp)
        assertNull(monster.elementalWeakness)
        assertNull(monster.elementalWeaknessPercent)
    }

    @Test
    fun `construct with mostly null fields`() {
        val monster = MonsterDefinition(
            id = 1, name = "Chicken", members = false,
            combatLevel = 1, hitpoints = 3, maxHit = "1",
            attackSpeed = null, size = 1,
            attackLevel = null, strengthLevel = null, defenceLevel = null,
            magicLevel = null, rangedLevel = null,
            attackStab = null, attackSlash = null, attackCrush = null,
            attackMagic = null, attackRanged = null,
            defenceStab = null, defenceSlash = null, defenceCrush = null,
            defenceMagic = null, defenceRanged = null,
            strengthBonus = null, rangedStrengthBonus = null, magicDamageBonus = null,
            slayerLevel = null, slayerXp = null, slayerCategory = null,
            assignedBy = null,
            elementalWeakness = null, elementalWeaknessPercent = null,
            poisonous = null,
            immunePoison = null, immuneVenom = null,
            examine = null
        )
        assertEquals(1, monster.id)
        assertEquals("Chicken", monster.name)
        assertFalse(monster.members)
        assertNull(monster.attackSpeed)
        assertNull(monster.slayerLevel)
        assertNull(monster.examine)
    }

    @Test
    fun `copy and equality`() {
        val monster = MonsterDefinition(
            id = 1, name = "Chicken", members = false,
            combatLevel = 1, hitpoints = 3, maxHit = "1",
            attackSpeed = null, size = 1,
            attackLevel = null, strengthLevel = null, defenceLevel = null,
            magicLevel = null, rangedLevel = null,
            attackStab = null, attackSlash = null, attackCrush = null,
            attackMagic = null, attackRanged = null,
            defenceStab = null, defenceSlash = null, defenceCrush = null,
            defenceMagic = null, defenceRanged = null,
            strengthBonus = null, rangedStrengthBonus = null, magicDamageBonus = null,
            slayerLevel = null, slayerXp = null, slayerCategory = null,
            assignedBy = null,
            elementalWeakness = null, elementalWeaknessPercent = null,
            poisonous = null,
            immunePoison = null, immuneVenom = null,
            examine = null
        )
        val copy = monster.copy(name = "Big chicken")
        assertNotEquals(monster, copy)
        assertEquals(monster, monster.copy())
    }
}

class DropEntryTest {

    @Test
    fun `construct with all fields`() {
        val drop = DropEntry(
            monsterName = "Abyssal demon",
            itemName = "Abyssal whip",
            itemId = 4151,
            quantity = "1",
            rarity = 0.001953125,
            noted = false,
            rolls = 1
        )
        assertEquals("Abyssal demon", drop.monsterName)
        assertEquals("Abyssal whip", drop.itemName)
        assertEquals(4151, drop.itemId)
        assertEquals("1", drop.quantity)
        assertEquals(0.001953125, drop.rarity)
        assertFalse(drop.noted)
        assertEquals(1, drop.rolls)
    }

    @Test
    fun `construct with null itemId`() {
        val drop = DropEntry(
            monsterName = "Goblin",
            itemName = "Bones",
            itemId = null,
            quantity = "1",
            rarity = 1.0,
            noted = false,
            rolls = 1
        )
        assertNull(drop.itemId)
    }

    @Test
    fun `copy and equality`() {
        val drop = DropEntry("Goblin", "Bones", 526, "1", 1.0, false, 1)
        val noted = drop.copy(noted = true)
        assertTrue(noted.noted)
        assertFalse(drop.noted)
        assertNotEquals(drop, noted)
    }
}

class QuestDefinitionTest {

    @Test
    fun `construct with all fields`() {
        val quest = QuestDefinition(
            name = "Dragon Slayer I",
            difficulty = "Experienced",
            length = "Long",
            requirements = "32 Quest points",
            startPoint = "Talk to the Guildmaster",
            itemsRequired = "Unfired bowl, Wizard's mind bomb",
            enemiesToDefeat = "Elvarg (level 83)",
            ironmanConcerns = "Must craft your own shield"
        )
        assertEquals("Dragon Slayer I", quest.name)
        assertEquals("Experienced", quest.difficulty)
        assertEquals("Long", quest.length)
    }

    @Test
    fun `construct with nullable fields as null`() {
        val quest = QuestDefinition(
            name = "Cook's Assistant",
            difficulty = null,
            length = null,
            requirements = null,
            startPoint = null,
            itemsRequired = null,
            enemiesToDefeat = null,
            ironmanConcerns = null
        )
        assertEquals("Cook's Assistant", quest.name)
        assertNull(quest.difficulty)
        assertNull(quest.length)
        assertNull(quest.requirements)
        assertNull(quest.startPoint)
        assertNull(quest.itemsRequired)
        assertNull(quest.enemiesToDefeat)
        assertNull(quest.ironmanConcerns)
    }

    @Test
    fun `copy and equality`() {
        val quest = QuestDefinition("Cook's Assistant", null, null, null, null, null, null, null)
        assertEquals(quest, quest.copy())
        assertNotEquals(quest, quest.copy(name = "Other"))
    }
}

class RecipeDefinitionTest {

    @Test
    fun `construct with all fields`() {
        val recipe = RecipeDefinition(
            name = "Shark",
            members = true,
            skill = "Cooking",
            level = 80,
            experience = 210.0,
            materials = listOf(RecipeMaterial("Raw shark", 1)),
            tools = listOf("Cooking gauntlets"),
            facilities = listOf("Range", "Fire"),
            boostable = true
        )
        assertEquals("Shark", recipe.name)
        assertTrue(recipe.members)
        assertEquals("Cooking", recipe.skill)
        assertEquals(80, recipe.level)
        assertEquals(210.0, recipe.experience)
        assertEquals(1, recipe.materials.size)
        assertEquals("Raw shark", recipe.materials[0].name)
        assertEquals(1, recipe.materials[0].quantity)
        assertEquals(listOf("Cooking gauntlets"), recipe.tools)
        assertEquals(listOf("Range", "Fire"), recipe.facilities)
        assertTrue(recipe.boostable)
    }

    @Test
    fun `construct with nullable fields as null`() {
        val recipe = RecipeDefinition(
            name = "Unknown recipe",
            members = false,
            skill = null,
            level = null,
            experience = null,
            materials = emptyList(),
            tools = emptyList(),
            facilities = emptyList(),
            boostable = false
        )
        assertNull(recipe.skill)
        assertNull(recipe.level)
        assertNull(recipe.experience)
        assertTrue(recipe.materials.isEmpty())
    }

    @Test
    fun `copy and equality`() {
        val recipe = RecipeDefinition("Shark", true, "Cooking", 80, 210.0,
            listOf(RecipeMaterial("Raw shark", 1)), emptyList(), emptyList(), true)
        assertEquals(recipe, recipe.copy())
        assertNotEquals(recipe, recipe.copy(level = 90))
    }
}

class RecipeMaterialTest {

    @Test
    fun `construct and verify`() {
        val mat = RecipeMaterial("Iron ore", 2)
        assertEquals("Iron ore", mat.name)
        assertEquals(2, mat.quantity)
    }
}

class ShopDefinitionTest {

    @Test
    fun `construct with all fields`() {
        val shop = ShopDefinition(
            name = "Aubury's Rune Shop",
            owner = "Aubury",
            location = "Varrock",
            members = false,
            items = listOf(
                ShopItem("Fire rune", 5000, "5", "1", "Coins", null),
                ShopItem("Air rune", 5000, "5", "1", "Coins", "30s")
            )
        )
        assertEquals("Aubury's Rune Shop", shop.name)
        assertEquals("Aubury", shop.owner)
        assertEquals("Varrock", shop.location)
        assertFalse(shop.members)
        assertEquals(2, shop.items.size)
        assertEquals("Fire rune", shop.items[0].itemName)
        assertEquals(5000, shop.items[0].stock)
    }

    @Test
    fun `construct with nullable fields as null`() {
        val shop = ShopDefinition(
            name = "Unknown shop",
            owner = null,
            location = null,
            members = false,
            items = emptyList()
        )
        assertNull(shop.owner)
        assertNull(shop.location)
        assertTrue(shop.items.isEmpty())
    }

    @Test
    fun `copy and equality`() {
        val shop = ShopDefinition("Shop", null, null, false, emptyList())
        assertEquals(shop, shop.copy())
        assertNotEquals(shop, shop.copy(name = "Other"))
    }
}

class ShopItemTest {

    @Test
    fun `construct with all fields`() {
        val item = ShopItem("Fire rune", 5000, "5", "1", "Coins", "30s")
        assertEquals("Fire rune", item.itemName)
        assertEquals(5000, item.stock)
        assertEquals("5", item.buyPrice)
        assertEquals("1", item.sellPrice)
        assertEquals("Coins", item.currency)
        assertEquals("30s", item.restockTime)
    }

    @Test
    fun `construct with nullable fields as null`() {
        val item = ShopItem("Mystery item", null, null, null, null, null)
        assertNull(item.stock)
        assertNull(item.buyPrice)
        assertNull(item.sellPrice)
        assertNull(item.currency)
        assertNull(item.restockTime)
    }
}

class SpellDefinitionTest {

    @Test
    fun `construct with all fields`() {
        val spell = SpellDefinition(
            name = "Fire Strike",
            spellbook = "Standard",
            level = 13,
            experience = 11.5,
            type = "Combat",
            runes = mapOf("Fire rune" to 3, "Air rune" to 2, "Mind rune" to 1),
            members = false
        )
        assertEquals("Fire Strike", spell.name)
        assertEquals("Standard", spell.spellbook)
        assertEquals(13, spell.level)
        assertEquals(11.5, spell.experience)
        assertEquals("Combat", spell.type)
        assertEquals(3, spell.runes["Fire rune"])
        assertEquals(2, spell.runes["Air rune"])
        assertEquals(1, spell.runes["Mind rune"])
        assertFalse(spell.members)
    }

    @Test
    fun `copy and equality`() {
        val spell = SpellDefinition("Fire Strike", "Standard", 13, 11.5, "Combat",
            mapOf("Fire rune" to 3), false)
        assertEquals(spell, spell.copy())
        assertNotEquals(spell, spell.copy(level = 20))
    }
}

class VarbitDefinitionTest {

    @Test
    fun `construct with all fields`() {
        val varbit = VarbitDefinition(index = 3278, name = "QUEST_DRAGON_SLAYER_I", content = "0-7")
        assertEquals(3278, varbit.index)
        assertEquals("QUEST_DRAGON_SLAYER_I", varbit.name)
        assertEquals("0-7", varbit.content)
    }

    @Test
    fun `construct with null content`() {
        val varbit = VarbitDefinition(index = 1, name = "UNKNOWN", content = null)
        assertNull(varbit.content)
    }

    @Test
    fun `copy and equality`() {
        val varbit = VarbitDefinition(1, "TEST", "val")
        assertEquals(varbit, varbit.copy())
        assertNotEquals(varbit, varbit.copy(index = 2))
    }
}

class MetadataTest {

    @Test
    fun `construct with all fields`() {
        val meta = Metadata(
            version = 3,
            scrapedAt = "2025-01-15T12:00:00Z",
            files = mapOf(
                "items.json" to FileInfo(hash = "abc123", entries = 25000),
                "monsters.json" to FileInfo(hash = "def456", entries = 3500)
            )
        )
        assertEquals(3, meta.version)
        assertEquals("2025-01-15T12:00:00Z", meta.scrapedAt)
        assertEquals(2, meta.files.size)
        assertEquals("abc123", meta.files["items.json"]?.hash)
        assertEquals(25000, meta.files["items.json"]?.entries)
    }

    @Test
    fun `empty files map`() {
        val meta = Metadata(version = 1, scrapedAt = "2025-01-01", files = emptyMap())
        assertTrue(meta.files.isEmpty())
    }

    @Test
    fun `copy and equality`() {
        val meta = Metadata(1, "2025-01-01", emptyMap())
        assertEquals(meta, meta.copy())
        assertNotEquals(meta, meta.copy(version = 2))
    }
}

class FileInfoTest {

    @Test
    fun `construct and verify`() {
        val info = FileInfo(hash = "sha256abc", entries = 12345)
        assertEquals("sha256abc", info.hash)
        assertEquals(12345, info.entries)
    }
}

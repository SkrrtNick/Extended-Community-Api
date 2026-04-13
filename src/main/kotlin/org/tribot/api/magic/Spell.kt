package org.tribot.api.magic

import org.tribot.api.magic.RuneType.*
import org.tribot.api.magic.Spellbook.*
import org.tribot.api.magic.SpellType.ALCHEMY
import org.tribot.api.magic.SpellType.COMBAT
import org.tribot.api.magic.SpellType.ENCHANT
import org.tribot.api.magic.SpellType.TELEPORT
import org.tribot.api.magic.SpellType.UTILITY

/**
 * Every spell across all four OSRS spellbooks, with exact in-game names, level
 * requirements, rune costs, and categorical metadata.
 *
 * All data validated against the Old School RuneScape Wiki (https://oldschool.runescape.wiki):
 * - Standard spellbook: https://oldschool.runescape.wiki/w/Standard_spellbook
 * - Ancient Magicks:    https://oldschool.runescape.wiki/w/Ancient_Magicks
 * - Lunar spellbook:    https://oldschool.runescape.wiki/w/Lunar_spellbook
 * - Arceuus spellbook:  https://oldschool.runescape.wiki/w/Arceuus_spellbook
 *
 * @property spellName  exact in-game name, suitable for [org.tribot.automation.script.core.tabs.Magic.cast]
 * @property spellbook  which spellbook this spell belongs to
 * @property level      Magic level required to cast
 * @property type       broad category (combat, teleport, utility, etc.)
 * @property runes      rune cost per cast
 */
@Suppress("unused")
enum class Spell(
    val spellName: String,
    val spellbook: Spellbook,
    val level: Int,
    val type: SpellType,
    val runes: List<RuneRequirement>
) {

    // ========================================================================================
    // STANDARD SPELLBOOK — Combat (Strikes)
    // ========================================================================================

    WIND_STRIKE(
        "Wind Strike", STANDARD, 1, COMBAT,
        listOf(RuneRequirement(AIR, 1), RuneRequirement(MIND, 1))
    ),
    WATER_STRIKE(
        "Water Strike", STANDARD, 5, COMBAT,
        listOf(RuneRequirement(AIR, 1), RuneRequirement(WATER, 1), RuneRequirement(MIND, 1))
    ),
    EARTH_STRIKE(
        "Earth Strike", STANDARD, 9, COMBAT,
        listOf(RuneRequirement(AIR, 1), RuneRequirement(EARTH, 2), RuneRequirement(MIND, 1))
    ),
    FIRE_STRIKE(
        "Fire Strike", STANDARD, 13, COMBAT,
        listOf(RuneRequirement(AIR, 2), RuneRequirement(FIRE, 3), RuneRequirement(MIND, 1))
    ),

    // ========================================================================================
    // STANDARD SPELLBOOK — Combat (Bolts)
    // ========================================================================================

    WIND_BOLT(
        "Wind Bolt", STANDARD, 17, COMBAT,
        listOf(RuneRequirement(AIR, 2), RuneRequirement(CHAOS, 1))
    ),
    WATER_BOLT(
        "Water Bolt", STANDARD, 23, COMBAT,
        listOf(RuneRequirement(AIR, 2), RuneRequirement(WATER, 2), RuneRequirement(CHAOS, 1))
    ),
    EARTH_BOLT(
        "Earth Bolt", STANDARD, 29, COMBAT,
        listOf(RuneRequirement(AIR, 2), RuneRequirement(EARTH, 3), RuneRequirement(CHAOS, 1))
    ),
    FIRE_BOLT(
        "Fire Bolt", STANDARD, 35, COMBAT,
        listOf(RuneRequirement(AIR, 3), RuneRequirement(FIRE, 4), RuneRequirement(CHAOS, 1))
    ),

    // ========================================================================================
    // STANDARD SPELLBOOK — Combat (Blasts)
    // ========================================================================================

    WIND_BLAST(
        "Wind Blast", STANDARD, 41, COMBAT,
        listOf(RuneRequirement(AIR, 3), RuneRequirement(DEATH, 1))
    ),
    WATER_BLAST(
        "Water Blast", STANDARD, 47, COMBAT,
        listOf(RuneRequirement(AIR, 3), RuneRequirement(WATER, 3), RuneRequirement(DEATH, 1))
    ),
    EARTH_BLAST(
        "Earth Blast", STANDARD, 53, COMBAT,
        listOf(RuneRequirement(AIR, 3), RuneRequirement(EARTH, 4), RuneRequirement(DEATH, 1))
    ),
    FIRE_BLAST(
        "Fire Blast", STANDARD, 59, COMBAT,
        listOf(RuneRequirement(AIR, 4), RuneRequirement(FIRE, 5), RuneRequirement(DEATH, 1))
    ),

    // ========================================================================================
    // STANDARD SPELLBOOK — Combat (Waves)
    // ========================================================================================

    WIND_WAVE(
        "Wind Wave", STANDARD, 62, COMBAT,
        listOf(RuneRequirement(AIR, 5), RuneRequirement(BLOOD, 1))
    ),
    WATER_WAVE(
        "Water Wave", STANDARD, 65, COMBAT,
        listOf(RuneRequirement(AIR, 5), RuneRequirement(WATER, 7), RuneRequirement(BLOOD, 1))
    ),
    EARTH_WAVE(
        "Earth Wave", STANDARD, 70, COMBAT,
        listOf(RuneRequirement(AIR, 5), RuneRequirement(EARTH, 7), RuneRequirement(BLOOD, 1))
    ),
    FIRE_WAVE(
        "Fire Wave", STANDARD, 75, COMBAT,
        listOf(RuneRequirement(AIR, 5), RuneRequirement(FIRE, 7), RuneRequirement(BLOOD, 1))
    ),

    // ========================================================================================
    // STANDARD SPELLBOOK — Combat (Surges)
    // ========================================================================================

    WIND_SURGE(
        "Wind Surge", STANDARD, 81, COMBAT,
        listOf(RuneRequirement(AIR, 7), RuneRequirement(WRATH, 1))
    ),
    WATER_SURGE(
        "Water Surge", STANDARD, 85, COMBAT,
        listOf(RuneRequirement(AIR, 7), RuneRequirement(WATER, 10), RuneRequirement(WRATH, 1))
    ),
    EARTH_SURGE(
        "Earth Surge", STANDARD, 90, COMBAT,
        listOf(RuneRequirement(AIR, 7), RuneRequirement(EARTH, 10), RuneRequirement(WRATH, 1))
    ),
    FIRE_SURGE(
        "Fire Surge", STANDARD, 95, COMBAT,
        listOf(RuneRequirement(AIR, 7), RuneRequirement(FIRE, 10), RuneRequirement(WRATH, 1))
    ),

    // ========================================================================================
    // STANDARD SPELLBOOK — Combat (Special)
    // ========================================================================================

    CRUMBLE_UNDEAD(
        "Crumble Undead", STANDARD, 39, COMBAT,
        listOf(RuneRequirement(AIR, 2), RuneRequirement(EARTH, 2), RuneRequirement(CHAOS, 1))
    ),
    IBAN_BLAST(
        "Iban Blast", STANDARD, 50, COMBAT,
        listOf(RuneRequirement(FIRE, 5), RuneRequirement(DEATH, 1))
    ),
    MAGIC_DART(
        "Magic Dart", STANDARD, 50, COMBAT,
        listOf(RuneRequirement(DEATH, 1), RuneRequirement(MIND, 4))
    ),
    SARADOMIN_STRIKE(
        "Saradomin Strike", STANDARD, 60, COMBAT,
        listOf(RuneRequirement(AIR, 4), RuneRequirement(FIRE, 2), RuneRequirement(BLOOD, 2))
    ),
    CLAWS_OF_GUTHIX(
        "Claws of Guthix", STANDARD, 60, COMBAT,
        listOf(RuneRequirement(AIR, 4), RuneRequirement(FIRE, 1), RuneRequirement(BLOOD, 2))
    ),
    FLAMES_OF_ZAMORAK(
        "Flames of Zamorak", STANDARD, 60, COMBAT,
        listOf(RuneRequirement(AIR, 1), RuneRequirement(FIRE, 4), RuneRequirement(BLOOD, 2))
    ),

    // Charge (empowers god spells)
    CHARGE(
        "Charge", STANDARD, 80, COMBAT,
        listOf(RuneRequirement(AIR, 3), RuneRequirement(FIRE, 3), RuneRequirement(BLOOD, 3))
    ),

    // ========================================================================================
    // STANDARD SPELLBOOK — Curses
    // ========================================================================================

    CONFUSE(
        "Confuse", STANDARD, 3, SpellType.CURSE,
        listOf(RuneRequirement(EARTH, 2), RuneRequirement(WATER, 3), RuneRequirement(BODY, 1))
    ),
    WEAKEN(
        "Weaken", STANDARD, 11, SpellType.CURSE,
        listOf(RuneRequirement(EARTH, 2), RuneRequirement(WATER, 3), RuneRequirement(BODY, 1))
    ),
    CURSE(
        "Curse", STANDARD, 19, SpellType.CURSE,
        listOf(RuneRequirement(EARTH, 3), RuneRequirement(WATER, 2), RuneRequirement(BODY, 1))
    ),
    BIND(
        "Bind", STANDARD, 20, SpellType.CURSE,
        listOf(RuneRequirement(EARTH, 3), RuneRequirement(WATER, 3), RuneRequirement(NATURE, 2))
    ),
    SNARE(
        "Snare", STANDARD, 50, SpellType.CURSE,
        listOf(RuneRequirement(EARTH, 4), RuneRequirement(WATER, 4), RuneRequirement(NATURE, 3))
    ),
    VULNERABILITY(
        "Vulnerability", STANDARD, 66, SpellType.CURSE,
        listOf(RuneRequirement(EARTH, 5), RuneRequirement(WATER, 5), RuneRequirement(SOUL, 1))
    ),
    ENFEEBLE(
        "Enfeeble", STANDARD, 73, SpellType.CURSE,
        listOf(RuneRequirement(EARTH, 8), RuneRequirement(WATER, 8), RuneRequirement(SOUL, 1))
    ),
    ENTANGLE(
        "Entangle", STANDARD, 79, SpellType.CURSE,
        listOf(RuneRequirement(EARTH, 5), RuneRequirement(WATER, 5), RuneRequirement(NATURE, 4))
    ),
    STUN(
        "Stun", STANDARD, 80, SpellType.CURSE,
        listOf(RuneRequirement(EARTH, 12), RuneRequirement(WATER, 12), RuneRequirement(SOUL, 1))
    ),
    TELE_BLOCK(
        "Tele Block", STANDARD, 85, SpellType.CURSE,
        listOf(RuneRequirement(CHAOS, 1), RuneRequirement(DEATH, 1), RuneRequirement(LAW, 1))
    ),

    // ========================================================================================
    // STANDARD SPELLBOOK — Teleports
    // ========================================================================================

    VARROCK_TELEPORT(
        "Varrock Teleport", STANDARD, 25, TELEPORT,
        listOf(RuneRequirement(AIR, 3), RuneRequirement(FIRE, 1), RuneRequirement(LAW, 1))
    ),
    LUMBRIDGE_TELEPORT(
        "Lumbridge Teleport", STANDARD, 31, TELEPORT,
        listOf(RuneRequirement(AIR, 3), RuneRequirement(EARTH, 1), RuneRequirement(LAW, 1))
    ),
    FALADOR_TELEPORT(
        "Falador Teleport", STANDARD, 37, TELEPORT,
        listOf(RuneRequirement(AIR, 3), RuneRequirement(WATER, 1), RuneRequirement(LAW, 1))
    ),
    TELEPORT_TO_HOUSE(
        "Teleport to House", STANDARD, 40, TELEPORT,
        listOf(RuneRequirement(AIR, 1), RuneRequirement(EARTH, 1), RuneRequirement(LAW, 1))
    ),
    CAMELOT_TELEPORT(
        "Camelot Teleport", STANDARD, 45, TELEPORT,
        listOf(RuneRequirement(AIR, 5), RuneRequirement(LAW, 1))
    ),
    KOUREND_CASTLE_TELEPORT(
        "Kourend Castle Teleport", STANDARD, 48, TELEPORT,
        listOf(RuneRequirement(FIRE, 1), RuneRequirement(WATER, 1), RuneRequirement(LAW, 2))
    ),
    ARDOUGNE_TELEPORT(
        "Ardougne Teleport", STANDARD, 51, TELEPORT,
        listOf(RuneRequirement(WATER, 2), RuneRequirement(LAW, 2))
    ),
    CIVITAS_ILLA_FORTIS_TELEPORT(
        "Civitas illa Fortis Teleport", STANDARD, 54, TELEPORT,
        listOf(RuneRequirement(EARTH, 1), RuneRequirement(FIRE, 1), RuneRequirement(LAW, 2))
    ),
    WATCHTOWER_TELEPORT(
        "Watchtower Teleport", STANDARD, 58, TELEPORT,
        listOf(RuneRequirement(EARTH, 2), RuneRequirement(LAW, 2))
    ),
    TROLLHEIM_TELEPORT(
        "Trollheim Teleport", STANDARD, 61, TELEPORT,
        listOf(RuneRequirement(FIRE, 2), RuneRequirement(LAW, 2))
    ),
    APE_ATOLL_TELEPORT_STANDARD(
        "Ape Atoll Teleport", STANDARD, 64, TELEPORT,
        listOf(RuneRequirement(FIRE, 2), RuneRequirement(WATER, 2), RuneRequirement(LAW, 2))
    ),
    TELEPORT_TO_BOAT(
        "Teleport to Boat", STANDARD, 67, TELEPORT,
        listOf(RuneRequirement(EARTH, 2), RuneRequirement(WATER, 2), RuneRequirement(LAW, 2))
    ),
    TELEOTHER_LUMBRIDGE(
        "Teleother Lumbridge", STANDARD, 74, TELEPORT,
        listOf(RuneRequirement(EARTH, 1), RuneRequirement(LAW, 1), RuneRequirement(SOUL, 1))
    ),
    TELEOTHER_FALADOR(
        "Teleother Falador", STANDARD, 82, TELEPORT,
        listOf(RuneRequirement(WATER, 1), RuneRequirement(LAW, 1), RuneRequirement(SOUL, 1))
    ),
    TELEOTHER_CAMELOT(
        "Teleother Camelot", STANDARD, 90, TELEPORT,
        listOf(RuneRequirement(LAW, 1), RuneRequirement(SOUL, 2))
    ),

    // ========================================================================================
    // STANDARD SPELLBOOK — Enchantments (Jewellery)
    // ========================================================================================

    LVL_1_ENCHANT(
        "Lvl-1 Enchant", STANDARD, 7, ENCHANT,
        listOf(RuneRequirement(WATER, 1), RuneRequirement(COSMIC, 1))
    ),
    LVL_2_ENCHANT(
        "Lvl-2 Enchant", STANDARD, 27, ENCHANT,
        listOf(RuneRequirement(AIR, 3), RuneRequirement(COSMIC, 1))
    ),
    LVL_3_ENCHANT(
        "Lvl-3 Enchant", STANDARD, 49, ENCHANT,
        listOf(RuneRequirement(FIRE, 5), RuneRequirement(COSMIC, 1))
    ),
    LVL_4_ENCHANT(
        "Lvl-4 Enchant", STANDARD, 57, ENCHANT,
        listOf(RuneRequirement(EARTH, 10), RuneRequirement(COSMIC, 1))
    ),
    LVL_5_ENCHANT(
        "Lvl-5 Enchant", STANDARD, 68, ENCHANT,
        listOf(RuneRequirement(EARTH, 15), RuneRequirement(WATER, 15), RuneRequirement(COSMIC, 1))
    ),
    LVL_6_ENCHANT(
        "Lvl-6 Enchant", STANDARD, 87, ENCHANT,
        listOf(RuneRequirement(EARTH, 20), RuneRequirement(FIRE, 20), RuneRequirement(COSMIC, 1))
    ),
    LVL_7_ENCHANT(
        "Lvl-7 Enchant", STANDARD, 93, ENCHANT,
        listOf(RuneRequirement(BLOOD, 20), RuneRequirement(COSMIC, 1), RuneRequirement(SOUL, 20))
    ),

    // ========================================================================================
    // STANDARD SPELLBOOK — Enchantments (Crossbow Bolts)
    // ========================================================================================

    ENCHANT_CROSSBOW_BOLT_OPAL(
        "Enchant Crossbow Bolt", STANDARD, 4, ENCHANT,
        listOf(RuneRequirement(AIR, 2), RuneRequirement(COSMIC, 1))
    ),

    // ========================================================================================
    // STANDARD SPELLBOOK — Alchemy & Conversions
    // ========================================================================================

    BONES_TO_BANANAS(
        "Bones to Bananas", STANDARD, 15, ALCHEMY,
        listOf(RuneRequirement(EARTH, 2), RuneRequirement(WATER, 2), RuneRequirement(NATURE, 1))
    ),
    LOW_LEVEL_ALCHEMY(
        "Low Level Alchemy", STANDARD, 21, ALCHEMY,
        listOf(RuneRequirement(FIRE, 3), RuneRequirement(NATURE, 1))
    ),
    SUPERHEAT_ITEM(
        "Superheat Item", STANDARD, 43, UTILITY,
        listOf(RuneRequirement(FIRE, 4), RuneRequirement(NATURE, 1))
    ),
    HIGH_LEVEL_ALCHEMY(
        "High Level Alchemy", STANDARD, 55, ALCHEMY,
        listOf(RuneRequirement(FIRE, 5), RuneRequirement(NATURE, 1))
    ),
    BONES_TO_PEACHES(
        "Bones to Peaches", STANDARD, 60, ALCHEMY,
        listOf(RuneRequirement(EARTH, 2), RuneRequirement(WATER, 4), RuneRequirement(NATURE, 2))
    ),

    // ========================================================================================
    // STANDARD SPELLBOOK — Utility
    // ========================================================================================

    TELEKINETIC_GRAB(
        "Telekinetic Grab", STANDARD, 33, UTILITY,
        listOf(RuneRequirement(AIR, 1), RuneRequirement(LAW, 1))
    ),
    MONSTER_INSPECT(
        "Monster Inspect", STANDARD, 42, UTILITY,
        listOf(RuneRequirement(BODY, 2), RuneRequirement(MIND, 2))
    ),
    SUMMON_BOAT(
        "Summon Boat", STANDARD, 56, UTILITY,
        listOf(RuneRequirement(EARTH, 1), RuneRequirement(WATER, 1), RuneRequirement(LAW, 2))
    ),

    // ========================================================================================
    // STANDARD SPELLBOOK — Charge Orb
    // ========================================================================================

    CHARGE_WATER_ORB(
        "Charge Water Orb", STANDARD, 56, UTILITY,
        listOf(RuneRequirement(WATER, 30), RuneRequirement(COSMIC, 3))
    ),
    CHARGE_EARTH_ORB(
        "Charge Earth Orb", STANDARD, 60, UTILITY,
        listOf(RuneRequirement(EARTH, 30), RuneRequirement(COSMIC, 3))
    ),
    CHARGE_FIRE_ORB(
        "Charge Fire Orb", STANDARD, 63, UTILITY,
        listOf(RuneRequirement(FIRE, 30), RuneRequirement(COSMIC, 3))
    ),
    CHARGE_AIR_ORB(
        "Charge Air Orb", STANDARD, 66, UTILITY,
        listOf(RuneRequirement(AIR, 30), RuneRequirement(COSMIC, 3))
    ),

    // ========================================================================================
    // ANCIENT MAGICKS — Smoke spells
    // ========================================================================================

    SMOKE_RUSH(
        "Smoke Rush", ANCIENT, 50, COMBAT,
        listOf(RuneRequirement(AIR, 1), RuneRequirement(FIRE, 1), RuneRequirement(CHAOS, 2), RuneRequirement(DEATH, 2))
    ),
    SMOKE_BURST(
        "Smoke Burst", ANCIENT, 62, COMBAT,
        listOf(RuneRequirement(AIR, 2), RuneRequirement(FIRE, 2), RuneRequirement(CHAOS, 4), RuneRequirement(DEATH, 2))
    ),
    SMOKE_BLITZ(
        "Smoke Blitz", ANCIENT, 74, COMBAT,
        listOf(RuneRequirement(AIR, 2), RuneRequirement(FIRE, 2), RuneRequirement(BLOOD, 2), RuneRequirement(DEATH, 2))
    ),
    SMOKE_BARRAGE(
        "Smoke Barrage", ANCIENT, 86, COMBAT,
        listOf(RuneRequirement(AIR, 4), RuneRequirement(FIRE, 4), RuneRequirement(BLOOD, 2), RuneRequirement(DEATH, 4))
    ),

    // ========================================================================================
    // ANCIENT MAGICKS — Shadow spells
    // ========================================================================================

    SHADOW_RUSH(
        "Shadow Rush", ANCIENT, 52, COMBAT,
        listOf(RuneRequirement(AIR, 1), RuneRequirement(CHAOS, 2), RuneRequirement(DEATH, 2), RuneRequirement(SOUL, 1))
    ),
    SHADOW_BURST(
        "Shadow Burst", ANCIENT, 64, COMBAT,
        listOf(RuneRequirement(AIR, 1), RuneRequirement(CHAOS, 4), RuneRequirement(DEATH, 2), RuneRequirement(SOUL, 2))
    ),
    SHADOW_BLITZ(
        "Shadow Blitz", ANCIENT, 76, COMBAT,
        listOf(RuneRequirement(AIR, 2), RuneRequirement(BLOOD, 2), RuneRequirement(DEATH, 2), RuneRequirement(SOUL, 2))
    ),
    SHADOW_BARRAGE(
        "Shadow Barrage", ANCIENT, 88, COMBAT,
        listOf(RuneRequirement(AIR, 4), RuneRequirement(BLOOD, 2), RuneRequirement(DEATH, 4), RuneRequirement(SOUL, 3))
    ),

    // ========================================================================================
    // ANCIENT MAGICKS — Blood spells
    // ========================================================================================

    BLOOD_RUSH(
        "Blood Rush", ANCIENT, 56, COMBAT,
        listOf(RuneRequirement(BLOOD, 1), RuneRequirement(CHAOS, 2), RuneRequirement(DEATH, 2))
    ),
    BLOOD_BURST(
        "Blood Burst", ANCIENT, 68, COMBAT,
        listOf(RuneRequirement(BLOOD, 2), RuneRequirement(CHAOS, 4), RuneRequirement(DEATH, 2))
    ),
    BLOOD_BLITZ(
        "Blood Blitz", ANCIENT, 80, COMBAT,
        listOf(RuneRequirement(BLOOD, 4), RuneRequirement(DEATH, 2))
    ),
    BLOOD_BARRAGE(
        "Blood Barrage", ANCIENT, 92, COMBAT,
        listOf(RuneRequirement(BLOOD, 4), RuneRequirement(DEATH, 4), RuneRequirement(SOUL, 1))
    ),

    // ========================================================================================
    // ANCIENT MAGICKS — Ice spells
    // ========================================================================================

    ICE_RUSH(
        "Ice Rush", ANCIENT, 58, COMBAT,
        listOf(RuneRequirement(WATER, 2), RuneRequirement(CHAOS, 2), RuneRequirement(DEATH, 2))
    ),
    ICE_BURST(
        "Ice Burst", ANCIENT, 70, COMBAT,
        listOf(RuneRequirement(WATER, 4), RuneRequirement(CHAOS, 4), RuneRequirement(DEATH, 2))
    ),
    ICE_BLITZ(
        "Ice Blitz", ANCIENT, 82, COMBAT,
        listOf(RuneRequirement(WATER, 3), RuneRequirement(BLOOD, 2), RuneRequirement(DEATH, 2))
    ),
    ICE_BARRAGE(
        "Ice Barrage", ANCIENT, 94, COMBAT,
        listOf(RuneRequirement(WATER, 6), RuneRequirement(BLOOD, 2), RuneRequirement(DEATH, 4))
    ),

    // ========================================================================================
    // ANCIENT MAGICKS — Teleports
    // ========================================================================================

    PADDEWWA_TELEPORT(
        "Paddewwa Teleport", ANCIENT, 54, TELEPORT,
        listOf(RuneRequirement(AIR, 1), RuneRequirement(FIRE, 1), RuneRequirement(LAW, 2))
    ),
    SENNTISTEN_TELEPORT(
        "Senntisten Teleport", ANCIENT, 60, TELEPORT,
        listOf(RuneRequirement(LAW, 2), RuneRequirement(SOUL, 1))
    ),
    KHARYRLL_TELEPORT(
        "Kharyrll Teleport", ANCIENT, 66, TELEPORT,
        listOf(RuneRequirement(BLOOD, 1), RuneRequirement(LAW, 2))
    ),
    LASSAR_TELEPORT(
        "Lassar Teleport", ANCIENT, 72, TELEPORT,
        listOf(RuneRequirement(WATER, 4), RuneRequirement(LAW, 2))
    ),
    DAREEYAK_TELEPORT(
        "Dareeyak Teleport", ANCIENT, 78, TELEPORT,
        listOf(RuneRequirement(AIR, 2), RuneRequirement(FIRE, 3), RuneRequirement(LAW, 2))
    ),
    CARRALLANGER_TELEPORT(
        "Carrallanger Teleport", ANCIENT, 84, TELEPORT,
        listOf(RuneRequirement(LAW, 2), RuneRequirement(SOUL, 2))
    ),
    ANNAKARL_TELEPORT(
        "Annakarl Teleport", ANCIENT, 90, TELEPORT,
        listOf(RuneRequirement(BLOOD, 2), RuneRequirement(LAW, 2))
    ),
    GHORROCK_TELEPORT(
        "Ghorrock Teleport", ANCIENT, 96, TELEPORT,
        listOf(RuneRequirement(WATER, 8), RuneRequirement(LAW, 2))
    ),

    // ========================================================================================
    // LUNAR SPELLBOOK — Utility
    // ========================================================================================

    BAKE_PIE(
        "Bake Pie", LUNAR, 65, UTILITY,
        listOf(RuneRequirement(FIRE, 5), RuneRequirement(WATER, 4), RuneRequirement(ASTRAL, 1))
    ),
    GEOMANCY(
        "Geomancy", LUNAR, 65, UTILITY,
        listOf(RuneRequirement(EARTH, 8), RuneRequirement(ASTRAL, 3), RuneRequirement(NATURE, 3))
    ),
    CURE_PLANT(
        "Cure Plant", LUNAR, 66, UTILITY,
        listOf(RuneRequirement(EARTH, 8), RuneRequirement(ASTRAL, 1))
    ),
    MONSTER_EXAMINE(
        "Monster Examine", LUNAR, 66, UTILITY,
        listOf(RuneRequirement(ASTRAL, 1), RuneRequirement(COSMIC, 1), RuneRequirement(MIND, 1))
    ),
    NPC_CONTACT(
        "NPC Contact", LUNAR, 67, UTILITY,
        listOf(RuneRequirement(AIR, 2), RuneRequirement(ASTRAL, 1), RuneRequirement(COSMIC, 1))
    ),
    HUMIDIFY(
        "Humidify", LUNAR, 68, UTILITY,
        listOf(RuneRequirement(FIRE, 1), RuneRequirement(WATER, 3), RuneRequirement(ASTRAL, 1))
    ),
    CURE_OTHER(
        "Cure Other", LUNAR, 68, UTILITY,
        listOf(RuneRequirement(EARTH, 10), RuneRequirement(ASTRAL, 1), RuneRequirement(LAW, 1))
    ),
    CURE_ME(
        "Cure Me", LUNAR, 71, UTILITY,
        listOf(RuneRequirement(ASTRAL, 2), RuneRequirement(COSMIC, 2), RuneRequirement(LAW, 1))
    ),
    HUNTER_KIT(
        "Hunter Kit", LUNAR, 71, UTILITY,
        listOf(RuneRequirement(EARTH, 2), RuneRequirement(ASTRAL, 2))
    ),
    CURE_GROUP(
        "Cure Group", LUNAR, 74, UTILITY,
        listOf(RuneRequirement(ASTRAL, 2), RuneRequirement(COSMIC, 2), RuneRequirement(LAW, 2))
    ),
    STAT_SPY(
        "Stat Spy", LUNAR, 75, UTILITY,
        listOf(RuneRequirement(ASTRAL, 2), RuneRequirement(BODY, 5), RuneRequirement(COSMIC, 1))
    ),
    SPIN_FLAX(
        "Spin Flax", LUNAR, 76, UTILITY,
        listOf(RuneRequirement(AIR, 5), RuneRequirement(ASTRAL, 1), RuneRequirement(NATURE, 2))
    ),
    SUPERGLASS_MAKE(
        "Superglass Make", LUNAR, 77, UTILITY,
        listOf(RuneRequirement(AIR, 10), RuneRequirement(FIRE, 6), RuneRequirement(ASTRAL, 2))
    ),
    TAN_LEATHER(
        "Tan Leather", LUNAR, 78, UTILITY,
        listOf(RuneRequirement(FIRE, 5), RuneRequirement(ASTRAL, 2), RuneRequirement(NATURE, 1))
    ),
    DREAM(
        "Dream", LUNAR, 79, UTILITY,
        listOf(RuneRequirement(ASTRAL, 2), RuneRequirement(BODY, 5), RuneRequirement(COSMIC, 1))
    ),
    STRING_JEWELLERY(
        "String Jewellery", LUNAR, 80, UTILITY,
        listOf(RuneRequirement(EARTH, 10), RuneRequirement(WATER, 5), RuneRequirement(ASTRAL, 2))
    ),
    STAT_RESTORE_POT_SHARE(
        "Stat Restore Pot Share", LUNAR, 81, UTILITY,
        listOf(RuneRequirement(EARTH, 10), RuneRequirement(WATER, 10), RuneRequirement(ASTRAL, 2))
    ),
    MAGIC_IMBUE(
        "Magic Imbue", LUNAR, 82, UTILITY,
        listOf(RuneRequirement(FIRE, 7), RuneRequirement(WATER, 7), RuneRequirement(ASTRAL, 2))
    ),
    FERTILE_SOIL(
        "Fertile Soil", LUNAR, 83, UTILITY,
        listOf(RuneRequirement(EARTH, 15), RuneRequirement(ASTRAL, 3), RuneRequirement(NATURE, 2))
    ),
    BOOST_POTION_SHARE(
        "Boost Potion Share", LUNAR, 84, UTILITY,
        listOf(RuneRequirement(EARTH, 12), RuneRequirement(WATER, 10), RuneRequirement(ASTRAL, 3))
    ),
    PLANK_MAKE(
        "Plank Make", LUNAR, 86, UTILITY,
        listOf(RuneRequirement(EARTH, 15), RuneRequirement(ASTRAL, 2), RuneRequirement(NATURE, 1))
    ),
    RECHARGE_DRAGONSTONE(
        "Recharge Dragonstone", LUNAR, 89, UTILITY,
        listOf(RuneRequirement(WATER, 4), RuneRequirement(ASTRAL, 1), RuneRequirement(SOUL, 1))
    ),
    ENERGY_TRANSFER(
        "Energy Transfer", LUNAR, 91, UTILITY,
        listOf(RuneRequirement(ASTRAL, 3), RuneRequirement(LAW, 2), RuneRequirement(NATURE, 1))
    ),
    HEAL_OTHER(
        "Heal Other", LUNAR, 92, UTILITY,
        listOf(RuneRequirement(ASTRAL, 3), RuneRequirement(BLOOD, 1), RuneRequirement(LAW, 3))
    ),
    VENGEANCE_OTHER(
        "Vengeance Other", LUNAR, 93, COMBAT,
        listOf(RuneRequirement(EARTH, 10), RuneRequirement(ASTRAL, 3), RuneRequirement(DEATH, 2))
    ),
    VENGEANCE(
        "Vengeance", LUNAR, 94, COMBAT,
        listOf(RuneRequirement(EARTH, 10), RuneRequirement(ASTRAL, 4), RuneRequirement(DEATH, 2))
    ),
    HEAL_GROUP(
        "Heal Group", LUNAR, 95, UTILITY,
        listOf(RuneRequirement(ASTRAL, 4), RuneRequirement(BLOOD, 3), RuneRequirement(LAW, 6))
    ),
    SPELLBOOK_SWAP(
        "Spellbook Swap", LUNAR, 96, UTILITY,
        listOf(RuneRequirement(ASTRAL, 3), RuneRequirement(COSMIC, 2), RuneRequirement(LAW, 1))
    ),

    // ========================================================================================
    // LUNAR SPELLBOOK — Teleports
    // ========================================================================================

    MOONCLAN_TELEPORT(
        "Moonclan Teleport", LUNAR, 69, TELEPORT,
        listOf(RuneRequirement(EARTH, 2), RuneRequirement(ASTRAL, 2), RuneRequirement(LAW, 1))
    ),
    TELE_GROUP_MOONCLAN(
        "Tele Group Moonclan", LUNAR, 70, TELEPORT,
        listOf(RuneRequirement(EARTH, 4), RuneRequirement(ASTRAL, 2), RuneRequirement(LAW, 1))
    ),
    OURANIA_TELEPORT(
        "Ourania Teleport", LUNAR, 71, TELEPORT,
        listOf(RuneRequirement(EARTH, 6), RuneRequirement(ASTRAL, 2), RuneRequirement(LAW, 1))
    ),
    WATERBIRTH_TELEPORT(
        "Waterbirth Teleport", LUNAR, 72, TELEPORT,
        listOf(RuneRequirement(WATER, 1), RuneRequirement(ASTRAL, 2), RuneRequirement(LAW, 1))
    ),
    TELE_GROUP_WATERBIRTH(
        "Tele Group Waterbirth", LUNAR, 73, TELEPORT,
        listOf(RuneRequirement(WATER, 5), RuneRequirement(ASTRAL, 2), RuneRequirement(LAW, 1))
    ),
    BARBARIAN_TELEPORT(
        "Barbarian Teleport", LUNAR, 75, TELEPORT,
        listOf(RuneRequirement(FIRE, 3), RuneRequirement(ASTRAL, 2), RuneRequirement(LAW, 2))
    ),
    TELE_GROUP_BARBARIAN(
        "Tele Group Barbarian", LUNAR, 76, TELEPORT,
        listOf(RuneRequirement(FIRE, 6), RuneRequirement(ASTRAL, 2), RuneRequirement(LAW, 2))
    ),
    KHAZARD_TELEPORT(
        "Khazard Teleport", LUNAR, 78, TELEPORT,
        listOf(RuneRequirement(WATER, 4), RuneRequirement(ASTRAL, 2), RuneRequirement(LAW, 2))
    ),
    TELE_GROUP_KHAZARD(
        "Tele Group Khazard", LUNAR, 79, TELEPORT,
        listOf(RuneRequirement(WATER, 8), RuneRequirement(ASTRAL, 2), RuneRequirement(LAW, 2))
    ),
    FISHING_GUILD_TELEPORT(
        "Fishing Guild Teleport", LUNAR, 85, TELEPORT,
        listOf(RuneRequirement(WATER, 10), RuneRequirement(ASTRAL, 3), RuneRequirement(LAW, 3))
    ),
    TELE_GROUP_FISHING_GUILD(
        "Tele Group Fishing Guild", LUNAR, 86, TELEPORT,
        listOf(RuneRequirement(WATER, 14), RuneRequirement(ASTRAL, 3), RuneRequirement(LAW, 3))
    ),
    CATHERBY_TELEPORT(
        "Catherby Teleport", LUNAR, 87, TELEPORT,
        listOf(RuneRequirement(WATER, 10), RuneRequirement(ASTRAL, 3), RuneRequirement(LAW, 3))
    ),
    TELE_GROUP_CATHERBY(
        "Tele Group Catherby", LUNAR, 88, TELEPORT,
        listOf(RuneRequirement(WATER, 15), RuneRequirement(ASTRAL, 3), RuneRequirement(LAW, 3))
    ),
    ICE_PLATEAU_TELEPORT(
        "Ice Plateau Teleport", LUNAR, 89, TELEPORT,
        listOf(RuneRequirement(WATER, 8), RuneRequirement(ASTRAL, 3), RuneRequirement(LAW, 3))
    ),
    TELE_GROUP_ICE_PLATEAU(
        "Tele Group Ice Plateau", LUNAR, 90, TELEPORT,
        listOf(RuneRequirement(WATER, 16), RuneRequirement(ASTRAL, 3), RuneRequirement(LAW, 3))
    ),

    // ========================================================================================
    // ARCEUUS SPELLBOOK — Teleports
    // ========================================================================================

    ARCEUUS_LIBRARY_TELEPORT(
        "Arceuus Library Teleport", ARCEUUS, 6, TELEPORT,
        listOf(RuneRequirement(EARTH, 2), RuneRequirement(LAW, 1))
    ),
    DRAYNOR_MANOR_TELEPORT(
        "Draynor Manor Teleport", ARCEUUS, 17, TELEPORT,
        listOf(RuneRequirement(EARTH, 1), RuneRequirement(WATER, 1), RuneRequirement(LAW, 1))
    ),
    BATTLEFRONT_TELEPORT(
        "Battlefront Teleport", ARCEUUS, 23, TELEPORT,
        listOf(RuneRequirement(EARTH, 1), RuneRequirement(FIRE, 1), RuneRequirement(LAW, 1))
    ),
    MIND_ALTAR_TELEPORT(
        "Mind Altar Teleport", ARCEUUS, 28, TELEPORT,
        listOf(RuneRequirement(LAW, 1), RuneRequirement(MIND, 2))
    ),
    RESPAWN_TELEPORT(
        "Respawn Teleport", ARCEUUS, 34, TELEPORT,
        listOf(RuneRequirement(LAW, 1), RuneRequirement(SOUL, 1))
    ),
    SALVE_GRAVEYARD_TELEPORT(
        "Salve Graveyard Teleport", ARCEUUS, 40, TELEPORT,
        listOf(RuneRequirement(LAW, 1), RuneRequirement(SOUL, 2))
    ),
    FENKENSTRAINS_CASTLE_TELEPORT(
        "Fenkenstrain's Castle Teleport", ARCEUUS, 48, TELEPORT,
        listOf(RuneRequirement(EARTH, 1), RuneRequirement(LAW, 1), RuneRequirement(SOUL, 1))
    ),
    WEST_ARDOUGNE_TELEPORT(
        "West Ardougne Teleport", ARCEUUS, 61, TELEPORT,
        listOf(RuneRequirement(LAW, 2), RuneRequirement(SOUL, 2))
    ),
    HARMONY_ISLAND_TELEPORT(
        "Harmony Island Teleport", ARCEUUS, 65, TELEPORT,
        listOf(RuneRequirement(LAW, 1), RuneRequirement(NATURE, 1), RuneRequirement(SOUL, 1))
    ),
    CEMETERY_TELEPORT(
        "Cemetery Teleport", ARCEUUS, 71, TELEPORT,
        listOf(RuneRequirement(BLOOD, 1), RuneRequirement(LAW, 1), RuneRequirement(SOUL, 1))
    ),
    BARROWS_TELEPORT(
        "Barrows Teleport", ARCEUUS, 83, TELEPORT,
        listOf(RuneRequirement(BLOOD, 1), RuneRequirement(LAW, 2), RuneRequirement(SOUL, 2))
    ),
    APE_ATOLL_TELEPORT_ARCEUUS(
        "Ape Atoll Teleport", ARCEUUS, 90, TELEPORT,
        listOf(RuneRequirement(BLOOD, 2), RuneRequirement(LAW, 2), RuneRequirement(SOUL, 2))
    ),

    // ========================================================================================
    // ARCEUUS SPELLBOOK — Reanimation
    // ========================================================================================

    BASIC_REANIMATION(
        "Basic Reanimation", ARCEUUS, 16, UTILITY,
        listOf(RuneRequirement(BODY, 4), RuneRequirement(NATURE, 2))
    ),
    ADEPT_REANIMATION(
        "Adept Reanimation", ARCEUUS, 41, UTILITY,
        listOf(RuneRequirement(BODY, 4), RuneRequirement(NATURE, 3), RuneRequirement(SOUL, 1))
    ),
    EXPERT_REANIMATION(
        "Expert Reanimation", ARCEUUS, 72, UTILITY,
        listOf(RuneRequirement(BLOOD, 1), RuneRequirement(NATURE, 3), RuneRequirement(SOUL, 2))
    ),
    MASTER_REANIMATION(
        "Master Reanimation", ARCEUUS, 90, UTILITY,
        listOf(RuneRequirement(BLOOD, 2), RuneRequirement(NATURE, 4), RuneRequirement(SOUL, 4))
    ),

    // ========================================================================================
    // ARCEUUS SPELLBOOK — Grasp spells (combat)
    // ========================================================================================

    GHOSTLY_GRASP(
        "Ghostly Grasp", ARCEUUS, 35, COMBAT,
        listOf(RuneRequirement(AIR, 4), RuneRequirement(CHAOS, 1))
    ),
    SKELETAL_GRASP(
        "Skeletal Grasp", ARCEUUS, 56, COMBAT,
        listOf(RuneRequirement(EARTH, 8), RuneRequirement(DEATH, 1))
    ),
    UNDEAD_GRASP(
        "Undead Grasp", ARCEUUS, 79, COMBAT,
        listOf(RuneRequirement(FIRE, 12), RuneRequirement(BLOOD, 1))
    ),

    // ========================================================================================
    // ARCEUUS SPELLBOOK — Demonbane spells (combat)
    // ========================================================================================

    INFERIOR_DEMONBANE(
        "Inferior Demonbane", ARCEUUS, 44, COMBAT,
        listOf(RuneRequirement(FIRE, 3), RuneRequirement(CHAOS, 1))
    ),
    SUPERIOR_DEMONBANE(
        "Superior Demonbane", ARCEUUS, 62, COMBAT,
        listOf(RuneRequirement(FIRE, 5), RuneRequirement(SOUL, 1))
    ),
    DARK_DEMONBANE(
        "Dark Demonbane", ARCEUUS, 82, COMBAT,
        listOf(RuneRequirement(FIRE, 7), RuneRequirement(SOUL, 2))
    ),

    // ========================================================================================
    // ARCEUUS SPELLBOOK — Other combat / curse
    // ========================================================================================

    DARK_LURE(
        "Dark Lure", ARCEUUS, 50, SpellType.CURSE,
        listOf(RuneRequirement(DEATH, 1), RuneRequirement(NATURE, 1))
    ),
    MARK_OF_DARKNESS(
        "Mark of Darkness", ARCEUUS, 59, SpellType.CURSE,
        listOf(RuneRequirement(COSMIC, 1), RuneRequirement(SOUL, 1))
    ),
    LESSER_CORRUPTION(
        "Lesser Corruption", ARCEUUS, 64, COMBAT,
        listOf(RuneRequirement(DEATH, 1), RuneRequirement(SOUL, 2))
    ),
    WARD_OF_ARCEUUS(
        "Ward of Arceuus", ARCEUUS, 73, UTILITY,
        listOf(RuneRequirement(COSMIC, 1), RuneRequirement(NATURE, 2), RuneRequirement(SOUL, 4))
    ),
    GREATER_CORRUPTION(
        "Greater Corruption", ARCEUUS, 85, COMBAT,
        listOf(RuneRequirement(BLOOD, 1), RuneRequirement(SOUL, 3))
    ),

    // ========================================================================================
    // ARCEUUS SPELLBOOK — Resurrection spells
    // ========================================================================================

    RESURRECT_LESSER_GHOST(
        "Resurrect Lesser Ghost", ARCEUUS, 38, COMBAT,
        listOf(RuneRequirement(AIR, 10), RuneRequirement(COSMIC, 1), RuneRequirement(MIND, 5))
    ),
    RESURRECT_LESSER_SKELETON(
        "Resurrect Lesser Skeleton", ARCEUUS, 38, COMBAT,
        listOf(RuneRequirement(AIR, 10), RuneRequirement(COSMIC, 1), RuneRequirement(MIND, 5))
    ),
    RESURRECT_LESSER_ZOMBIE(
        "Resurrect Lesser Zombie", ARCEUUS, 38, COMBAT,
        listOf(RuneRequirement(AIR, 10), RuneRequirement(COSMIC, 1), RuneRequirement(MIND, 5))
    ),
    RESURRECT_SUPERIOR_GHOST(
        "Resurrect Superior Ghost", ARCEUUS, 57, COMBAT,
        listOf(RuneRequirement(EARTH, 10), RuneRequirement(COSMIC, 1), RuneRequirement(DEATH, 5))
    ),
    RESURRECT_SUPERIOR_SKELETON(
        "Resurrect Superior Skeleton", ARCEUUS, 57, COMBAT,
        listOf(RuneRequirement(EARTH, 10), RuneRequirement(COSMIC, 1), RuneRequirement(DEATH, 5))
    ),
    RESURRECT_SUPERIOR_ZOMBIE(
        "Resurrect Superior Zombie", ARCEUUS, 57, COMBAT,
        listOf(RuneRequirement(EARTH, 10), RuneRequirement(COSMIC, 1), RuneRequirement(DEATH, 5))
    ),
    RESURRECT_GREATER_GHOST(
        "Resurrect Greater Ghost", ARCEUUS, 76, COMBAT,
        listOf(RuneRequirement(FIRE, 10), RuneRequirement(BLOOD, 5), RuneRequirement(COSMIC, 1))
    ),
    RESURRECT_GREATER_SKELETON(
        "Resurrect Greater Skeleton", ARCEUUS, 76, COMBAT,
        listOf(RuneRequirement(FIRE, 10), RuneRequirement(BLOOD, 5), RuneRequirement(COSMIC, 1))
    ),
    RESURRECT_GREATER_ZOMBIE(
        "Resurrect Greater Zombie", ARCEUUS, 76, COMBAT,
        listOf(RuneRequirement(FIRE, 10), RuneRequirement(BLOOD, 5), RuneRequirement(COSMIC, 1))
    ),

    // ========================================================================================
    // ARCEUUS SPELLBOOK — Utility
    // ========================================================================================

    SHADOW_VEIL(
        "Shadow Veil", ARCEUUS, 47, UTILITY,
        listOf(RuneRequirement(EARTH, 5), RuneRequirement(FIRE, 5), RuneRequirement(COSMIC, 5))
    ),
    VILE_VIGOUR(
        "Vile Vigour", ARCEUUS, 66, UTILITY,
        listOf(RuneRequirement(AIR, 3), RuneRequirement(SOUL, 1))
    ),
    DEGRIME(
        "Degrime", ARCEUUS, 70, UTILITY,
        listOf(RuneRequirement(EARTH, 4), RuneRequirement(NATURE, 2))
    ),
    RESURRECT_CROPS(
        "Resurrect Crops", ARCEUUS, 78, UTILITY,
        listOf(RuneRequirement(EARTH, 25), RuneRequirement(BLOOD, 8), RuneRequirement(NATURE, 12), RuneRequirement(SOUL, 8))
    ),
    DEATH_CHARGE(
        "Death Charge", ARCEUUS, 80, UTILITY,
        listOf(RuneRequirement(BLOOD, 1), RuneRequirement(DEATH, 1), RuneRequirement(SOUL, 1))
    ),

    // ========================================================================================
    // ARCEUUS SPELLBOOK — Offering spells
    // ========================================================================================

    DEMONIC_OFFERING(
        "Demonic Offering", ARCEUUS, 84, UTILITY,
        listOf(RuneRequirement(SOUL, 1), RuneRequirement(WRATH, 1))
    ),
    SINISTER_OFFERING(
        "Sinister Offering", ARCEUUS, 92, UTILITY,
        listOf(RuneRequirement(BLOOD, 1), RuneRequirement(WRATH, 1))
    );

    companion object {
        /**
         * Finds a spell by its exact in-game name (case-insensitive).
         */
        fun fromName(name: String): Spell? =
            entries.find { it.spellName.equals(name, ignoreCase = true) }

        /**
         * Returns all spells belonging to the given [spellbook].
         */
        fun forSpellbook(spellbook: Spellbook): List<Spell> =
            entries.filter { it.spellbook == spellbook }

        /**
         * Returns all spells of a given [type].
         */
        fun forType(type: SpellType): List<Spell> =
            entries.filter { it.type == type }
    }
}

package org.tribot.api.consumable

import net.runelite.api.Skill

/**
 * Database of consumable items with externally validated healing values and stat effects.
 *
 * All values have been verified against:
 * - OSRS Wiki (https://oldschool.runescape.wiki)
 * - RuneLite ItemStatChanges.java (https://github.com/runelite/runelite)
 *
 * Each entry includes a source comment for traceability.
 */
object ConsumableDatabase {
    private val consumables = mutableMapOf<Int, Consumable>()
    private val consumablesByName = mutableMapOf<String, Consumable>()

    fun get(itemId: Int): Consumable? = consumables[itemId]

    fun getByName(name: String): Consumable? =
        consumablesByName[name.lowercase()]

    fun getAllFood(): List<Consumable> =
        consumables.values.filter { it.type == ConsumableType.FOOD || it.type == ConsumableType.COMBO }

    fun getAllPotions(): List<Consumable> =
        consumables.values.filter { it.type == ConsumableType.POTION }

    fun getAllBrews(): List<Consumable> =
        consumables.values.filter { it.type == ConsumableType.BREW }

    fun getAllPies(): List<Consumable> =
        consumables.values.filter { it.type == ConsumableType.PIE }

    fun getAll(): List<Consumable> = consumables.values.toList()

    private fun register(consumable: Consumable) {
        consumables[consumable.itemId] = consumable
        consumablesByName[consumable.name.lowercase()] = consumable
    }

    private fun registerAll(vararg items: Consumable) {
        items.forEach { register(it) }
    }

    // =========================================================================
    // Helper to create a simple food consumable
    // =========================================================================
    private fun food(itemId: Int, name: String, heal: Int) = Consumable(
        itemId = itemId,
        name = name,
        effects = listOf(HealEffect(heal)),
        type = ConsumableType.FOOD
    )

    private fun pie(itemId: Int, name: String, healPerSlice: Int) = Consumable(
        itemId = itemId,
        name = name,
        effects = listOf(HealEffect(healPerSlice)),
        type = ConsumableType.PIE
    )

    // =========================================================================
    // Anglerfish scaling formula
    // Source: OSRS Wiki - Anglerfish, RuneLite Anglerfish.java
    // Formula: heal = floor(maxHP / 10) + C
    //   where C = 2 if maxHP <= 24, 4 if <= 49, 6 if <= 74, 8 if <= 92, 13 if > 92
    // At 99 HP: floor(99/10) + 13 = 9 + 13 = 22, can overheal to 121
    // =========================================================================
    private val anglerFishFormula: (Int) -> Int = { maxHp ->
        val c = when {
            maxHp <= 24 -> 2
            maxHp <= 49 -> 4
            maxHp <= 74 -> 6
            maxHp <= 92 -> 8
            else -> 13
        }
        maxHp / 10 + c
    }

    init {
        // =================================================================
        // FOOD — Standard healing foods
        // Source: OSRS Wiki - Food, RuneLite ItemStatChanges.java
        // =================================================================

        // Source: OSRS Wiki - Shrimps (heal 3), ItemID 315
        register(food(315, "Shrimps", 3))

        // Source: OSRS Wiki - Sardine (heal 4), ItemID 325
        register(food(325, "Sardine", 4))

        // Source: OSRS Wiki - Herring (heal 5), ItemID 347
        register(food(347, "Herring", 5))

        // Source: OSRS Wiki - Mackerel (heal 6), ItemID 355
        register(food(355, "Mackerel", 6))

        // Source: OSRS Wiki - Trout (heal 7), ItemID 333
        register(food(333, "Trout", 7))

        // Source: OSRS Wiki - Cod (heal 7), ItemID 339
        register(food(339, "Cod", 7))

        // Source: OSRS Wiki - Pike (heal 8), ItemID 351
        register(food(351, "Pike", 8))

        // Source: OSRS Wiki - Salmon (heal 9), ItemID 329
        register(food(329, "Salmon", 9))

        // Source: OSRS Wiki - Tuna (heal 10), ItemID 361
        register(food(361, "Tuna", 10))

        // Source: OSRS Wiki - Lobster (heal 12), ItemID 379
        register(food(379, "Lobster", 12))

        // Source: OSRS Wiki - Bass (heal 13), ItemID 365
        register(food(365, "Bass", 13))

        // Source: OSRS Wiki - Swordfish (heal 14), ItemID 373
        register(food(373, "Swordfish", 14))

        // Source: OSRS Wiki - Monkfish (heal 16), ItemID 7946
        register(food(7946, "Monkfish", 16))

        // Source: OSRS Wiki - Karambwan (heal 18, combo food), ItemID 3144
        register(Consumable(
            itemId = 3144,
            name = "Cooked karambwan",
            effects = listOf(HealEffect(18)),
            type = ConsumableType.COMBO
        ))

        // Source: OSRS Wiki - Shark (heal 20), ItemID 385
        register(food(385, "Shark", 20))

        // Source: OSRS Wiki - Sea turtle (heal 21), ItemID 397
        register(food(397, "Sea turtle", 21))

        // Source: OSRS Wiki - Manta ray (heal 22), ItemID 391
        register(food(391, "Manta ray", 22))

        // Source: OSRS Wiki - Tuna potato (heal 22), ItemID 7060
        register(food(7060, "Tuna potato", 22))

        // Source: OSRS Wiki - Dark crab (heal 22), ItemID 11936
        register(food(11936, "Dark crab", 22))

        // Source: OSRS Wiki - Anglerfish (scaling heal, overheal), ItemID 13441
        // RuneLite Anglerfish.java: heal = floor(maxHP/10) + C
        register(Consumable(
            itemId = 13441,
            name = "Anglerfish",
            effects = listOf(ScalingHealEffect(
                formula = anglerFishFormula,
                overheal = true,
                formulaDescription = "Heal floor(HP/10) + C (max 22 at 99 HP, overheal to 121)"
            )),
            type = ConsumableType.FOOD
        ))

        // =================================================================
        // PIES — 2 slices per pie
        // Source: OSRS Wiki - Pizza, RuneLite ItemStatChanges.java
        // =================================================================

        // Source: OSRS Wiki - Plain pizza (7 per slice), ItemID 2289 (full), 2291 (half)
        register(pie(2289, "Plain pizza", 7))
        register(pie(2291, "1/2 plain pizza", 7))

        // Source: OSRS Wiki - Meat pizza (8 per slice), ItemID 2293 (full), 2295 (half)
        register(pie(2293, "Meat pizza", 8))
        register(pie(2295, "1/2 meat pizza", 8))

        // Source: OSRS Wiki - Anchovy pizza (9 per slice), ItemID 2297 (full), 2299 (half)
        register(pie(2297, "Anchovy pizza", 9))
        register(pie(2299, "1/2 anchovy pizza", 9))

        // Source: OSRS Wiki - Pineapple pizza (11 per slice), ItemID 2301 (full), 2303 (half)
        register(pie(2301, "Pineapple pizza", 11))
        register(pie(2303, "1/2 pineapple pizza", 11))

        // =================================================================
        // POTIONS — Attack
        // Source: RuneLite ItemStatChanges.java, OSRS Wiki - Attack potion
        // Formula: floor(level * 0.10) + 3
        // IDs: 4-dose=2428, 3-dose=121, 2-dose=123, 1-dose=125
        // =================================================================
        val attackPotionEffect = listOf(StatBoostEffect(Skill.ATTACK, 0.10, 3))
        registerAll(
            Consumable(2428, "Attack potion(4)", attackPotionEffect, ConsumableType.POTION),
            Consumable(121, "Attack potion(3)", attackPotionEffect, ConsumableType.POTION),
            Consumable(123, "Attack potion(2)", attackPotionEffect, ConsumableType.POTION),
            Consumable(125, "Attack potion(1)", attackPotionEffect, ConsumableType.POTION)
        )

        // =================================================================
        // POTIONS — Super Attack
        // Source: RuneLite ItemStatChanges.java, OSRS Wiki - Super attack
        // Formula: floor(level * 0.15) + 5
        // IDs: 4-dose=2436, 3-dose=145, 2-dose=147, 1-dose=149
        // =================================================================
        val superAttackEffect = listOf(StatBoostEffect(Skill.ATTACK, 0.15, 5))
        registerAll(
            Consumable(2436, "Super attack(4)", superAttackEffect, ConsumableType.POTION),
            Consumable(145, "Super attack(3)", superAttackEffect, ConsumableType.POTION),
            Consumable(147, "Super attack(2)", superAttackEffect, ConsumableType.POTION),
            Consumable(149, "Super attack(1)", superAttackEffect, ConsumableType.POTION)
        )

        // =================================================================
        // POTIONS — Strength
        // Source: RuneLite ItemStatChanges.java, OSRS Wiki - Strength potion
        // Formula: floor(level * 0.10) + 3
        // IDs: 4-dose=113, 3-dose=115, 2-dose=117, 1-dose=119
        // =================================================================
        val strengthPotionEffect = listOf(StatBoostEffect(Skill.STRENGTH, 0.10, 3))
        registerAll(
            Consumable(113, "Strength potion(4)", strengthPotionEffect, ConsumableType.POTION),
            Consumable(115, "Strength potion(3)", strengthPotionEffect, ConsumableType.POTION),
            Consumable(117, "Strength potion(2)", strengthPotionEffect, ConsumableType.POTION),
            Consumable(119, "Strength potion(1)", strengthPotionEffect, ConsumableType.POTION)
        )

        // =================================================================
        // POTIONS — Super Strength
        // Source: RuneLite ItemStatChanges.java, OSRS Wiki - Super strength
        // Formula: floor(level * 0.15) + 5
        // IDs: 4-dose=2440, 3-dose=157, 2-dose=159, 1-dose=161
        // =================================================================
        val superStrengthEffect = listOf(StatBoostEffect(Skill.STRENGTH, 0.15, 5))
        registerAll(
            Consumable(2440, "Super strength(4)", superStrengthEffect, ConsumableType.POTION),
            Consumable(157, "Super strength(3)", superStrengthEffect, ConsumableType.POTION),
            Consumable(159, "Super strength(2)", superStrengthEffect, ConsumableType.POTION),
            Consumable(161, "Super strength(1)", superStrengthEffect, ConsumableType.POTION)
        )

        // =================================================================
        // POTIONS — Defence
        // Source: RuneLite ItemStatChanges.java, OSRS Wiki - Defence potion
        // Formula: floor(level * 0.10) + 3
        // IDs: 4-dose=2432, 3-dose=133, 2-dose=135, 1-dose=137
        // =================================================================
        val defencePotionEffect = listOf(StatBoostEffect(Skill.DEFENCE, 0.10, 3))
        registerAll(
            Consumable(2432, "Defence potion(4)", defencePotionEffect, ConsumableType.POTION),
            Consumable(133, "Defence potion(3)", defencePotionEffect, ConsumableType.POTION),
            Consumable(135, "Defence potion(2)", defencePotionEffect, ConsumableType.POTION),
            Consumable(137, "Defence potion(1)", defencePotionEffect, ConsumableType.POTION)
        )

        // =================================================================
        // POTIONS — Super Defence
        // Source: RuneLite ItemStatChanges.java, OSRS Wiki - Super defence
        // Formula: floor(level * 0.15) + 5
        // IDs: 4-dose=2442, 3-dose=163, 2-dose=165, 1-dose=167
        // =================================================================
        val superDefenceEffect = listOf(StatBoostEffect(Skill.DEFENCE, 0.15, 5))
        registerAll(
            Consumable(2442, "Super defence(4)", superDefenceEffect, ConsumableType.POTION),
            Consumable(163, "Super defence(3)", superDefenceEffect, ConsumableType.POTION),
            Consumable(165, "Super defence(2)", superDefenceEffect, ConsumableType.POTION),
            Consumable(167, "Super defence(1)", superDefenceEffect, ConsumableType.POTION)
        )

        // =================================================================
        // POTIONS — Ranging
        // Source: RuneLite ItemStatChanges.java, OSRS Wiki - Ranging potion
        // Formula: floor(level * 0.10) + 4
        // IDs: 4-dose=2444, 3-dose=169, 2-dose=171, 1-dose=173
        // =================================================================
        val rangingPotionEffect = listOf(StatBoostEffect(Skill.RANGED, 0.10, 4))
        registerAll(
            Consumable(2444, "Ranging potion(4)", rangingPotionEffect, ConsumableType.POTION),
            Consumable(169, "Ranging potion(3)", rangingPotionEffect, ConsumableType.POTION),
            Consumable(171, "Ranging potion(2)", rangingPotionEffect, ConsumableType.POTION),
            Consumable(173, "Ranging potion(1)", rangingPotionEffect, ConsumableType.POTION)
        )

        // =================================================================
        // POTIONS — Magic
        // Source: RuneLite ItemStatChanges.java, OSRS Wiki - Magic potion
        // Formula: flat +4
        // IDs: 4-dose=3040, 3-dose=3042, 2-dose=3044, 1-dose=3046
        // =================================================================
        val magicPotionEffect = listOf(StatBoostEffect(Skill.MAGIC, 0.0, 4))
        registerAll(
            Consumable(3040, "Magic potion(4)", magicPotionEffect, ConsumableType.POTION),
            Consumable(3042, "Magic potion(3)", magicPotionEffect, ConsumableType.POTION),
            Consumable(3044, "Magic potion(2)", magicPotionEffect, ConsumableType.POTION),
            Consumable(3046, "Magic potion(1)", magicPotionEffect, ConsumableType.POTION)
        )

        // =================================================================
        // POTIONS — Prayer
        // Source: OSRS Wiki - Prayer potion, RuneLite ItemStatChanges.java
        // Formula: floor(level * 0.25) + 7
        // IDs: 4-dose=2434, 3-dose=139, 2-dose=141, 1-dose=143
        // =================================================================
        val prayerPotionEffect = listOf(PrayerRestoreEffect(0.25, 7))
        registerAll(
            Consumable(2434, "Prayer potion(4)", prayerPotionEffect, ConsumableType.POTION),
            Consumable(139, "Prayer potion(3)", prayerPotionEffect, ConsumableType.POTION),
            Consumable(141, "Prayer potion(2)", prayerPotionEffect, ConsumableType.POTION),
            Consumable(143, "Prayer potion(1)", prayerPotionEffect, ConsumableType.POTION)
        )

        // =================================================================
        // POTIONS — Super Restore
        // Source: OSRS Wiki - Super restore, RuneLite ItemStatChanges.java
        // Formula: floor(level * 0.25) + 8 (for all stats including prayer)
        // IDs: 4-dose=3024, 3-dose=3026, 2-dose=3028, 1-dose=3030
        // =================================================================
        val superRestoreEffect = listOf(PrayerRestoreEffect(0.25, 8))
        registerAll(
            Consumable(3024, "Super restore(4)", superRestoreEffect, ConsumableType.POTION),
            Consumable(3026, "Super restore(3)", superRestoreEffect, ConsumableType.POTION),
            Consumable(3028, "Super restore(2)", superRestoreEffect, ConsumableType.POTION),
            Consumable(3030, "Super restore(1)", superRestoreEffect, ConsumableType.POTION)
        )

        // =================================================================
        // BREWS — Saradomin Brew
        // Source: OSRS Wiki - Saradomin brew, RuneLite ItemStatChanges.java
        // HP:  floor(maxHP * 0.15) + 2 (overheal)
        // Def: floor(level * 0.20) + 2
        // Atk drain: floor(current * 0.10) + 2
        // Str drain: floor(current * 0.10) + 2
        // Mag drain: floor(current * 0.10) + 2
        // Rng drain: floor(current * 0.10) + 2
        // IDs: 4-dose=6685, 3-dose=6687, 2-dose=6689, 1-dose=6691
        // =================================================================
        val saradominBrewEffects = listOf(
            PercentHealEffect(0.15, 2, overheal = true),
            StatBoostEffect(Skill.DEFENCE, 0.20, 2),
            StatDrainEffect(Skill.ATTACK, 0.10, 2),
            StatDrainEffect(Skill.STRENGTH, 0.10, 2),
            StatDrainEffect(Skill.MAGIC, 0.10, 2),
            StatDrainEffect(Skill.RANGED, 0.10, 2)
        )
        registerAll(
            Consumable(6685, "Saradomin brew(4)", saradominBrewEffects, ConsumableType.BREW),
            Consumable(6687, "Saradomin brew(3)", saradominBrewEffects, ConsumableType.BREW),
            Consumable(6689, "Saradomin brew(2)", saradominBrewEffects, ConsumableType.BREW),
            Consumable(6691, "Saradomin brew(1)", saradominBrewEffects, ConsumableType.BREW)
        )

        // =================================================================
        // POTIONS — Energy
        // Source: OSRS Wiki - Energy potion, RuneLite ItemStatChanges.java
        // Restores 10% run energy per dose
        // IDs: 4-dose=3008, 3-dose=3010, 2-dose=3012, 1-dose=3014
        // =================================================================
        val energyPotionEffect = listOf(RunEnergyEffect(10))
        registerAll(
            Consumable(3008, "Energy potion(4)", energyPotionEffect, ConsumableType.POTION),
            Consumable(3010, "Energy potion(3)", energyPotionEffect, ConsumableType.POTION),
            Consumable(3012, "Energy potion(2)", energyPotionEffect, ConsumableType.POTION),
            Consumable(3014, "Energy potion(1)", energyPotionEffect, ConsumableType.POTION)
        )

        // =================================================================
        // POTIONS — Super Energy
        // Source: OSRS Wiki - Super energy potion, RuneLite ItemStatChanges.java
        // Restores 20% run energy per dose
        // IDs: 4-dose=3016, 3-dose=3018, 2-dose=3020, 1-dose=3022
        // =================================================================
        val superEnergyEffect = listOf(RunEnergyEffect(20))
        registerAll(
            Consumable(3016, "Super energy(4)", superEnergyEffect, ConsumableType.POTION),
            Consumable(3018, "Super energy(3)", superEnergyEffect, ConsumableType.POTION),
            Consumable(3020, "Super energy(2)", superEnergyEffect, ConsumableType.POTION),
            Consumable(3022, "Super energy(1)", superEnergyEffect, ConsumableType.POTION)
        )

        // =================================================================
        // POTIONS — Stamina
        // Source: OSRS Wiki - Stamina potion, RuneLite ItemStatChanges.java
        // Restores 20% run energy per dose + 70% reduced depletion for 2 minutes
        // IDs: 4-dose=12625, 3-dose=12627, 2-dose=12629, 1-dose=12631
        // =================================================================
        val staminaPotionEffect = listOf(RunEnergyEffect(20))
        registerAll(
            Consumable(12625, "Stamina potion(4)", staminaPotionEffect, ConsumableType.POTION),
            Consumable(12627, "Stamina potion(3)", staminaPotionEffect, ConsumableType.POTION),
            Consumable(12629, "Stamina potion(2)", staminaPotionEffect, ConsumableType.POTION),
            Consumable(12631, "Stamina potion(1)", staminaPotionEffect, ConsumableType.POTION)
        )
    }
}

package org.tribot.api

import org.tribot.api.data.*

fun main() {
    println("=== OSRS Data Service - Standalone Test ===\n")

    // Items
    println("--- Items ---")
    val whip = ItemDatabase.get(4151)
    println("Abyssal whip: name=${whip?.name}, alch=${whip?.highAlch}, weight=${whip?.weight}")
    println("  Equipment: slot=${whip?.equipment?.slot}, slash=${whip?.equipment?.attackSlash}, str=${whip?.equipment?.meleeStrength}")
    println("  Weapon: speed=${whip?.weapon?.attackSpeed}, type=${whip?.weapon?.weaponType}")
    println("  Requirements: ${whip?.equipment?.requirements}")

    val bandos = ItemDatabase.getByName("Bandos chestplate")
    println("Bandos chestplate: id=${bandos?.id}, defSlash=${bandos?.equipment?.defenceSlash}, prayer=${bandos?.equipment?.prayer}")

    println("Total items: ${ItemDatabase.getAll().size}")

    // Monsters
    println("\n--- Monsters ---")
    val zulrah = MonsterDatabase.getByName("Zulrah")
    println("Zulrah: combat=${zulrah?.combatLevel}, hp=${zulrah?.hitpoints}, maxHit=${zulrah?.maxHit}")
    println("  Weakness: ${zulrah?.elementalWeakness} (${zulrah?.elementalWeaknessPercent}%)")
    println("  Poison: ${zulrah?.poisonous}, immuneVenom=${zulrah?.immuneVenom}")

    val vorkath = MonsterDatabase.getByName("Vorkath")
    println("Vorkath: combat=${vorkath?.combatLevel}, hp=${vorkath?.hitpoints}, slayer=${vorkath?.slayerLevel}")

    println("Total monsters: ${MonsterDatabase.getAll().size}")

    // Drops
    println("\n--- Drops ---")
    val zulrahDrops = DropDatabase.getDropsForMonster("Zulrah")
    println("Zulrah drops: ${zulrahDrops.size} entries")
    zulrahDrops.filter { it.rarity > 0 && it.rarity < 0.01 }
        .sortedBy { it.rarity }
        .take(5)
        .forEach { println("  ${it.itemName}: 1/${(1.0 / it.rarity).toInt()} (qty: ${it.quantity})") }

    val whipSources = DropDatabase.getDropsForItem("Abyssal whip")
    println("Abyssal whip dropped by: ${whipSources.map { "${it.monsterName} (1/${(1.0 / it.rarity).toInt()})" }}")

    println("Total drops: ${DropDatabase.getAll().size}")

    // Quests
    println("\n--- Quests ---")
    val ds2 = QuestDatabase.getByName("Dragon Slayer II")
    println("Dragon Slayer II: difficulty=${ds2?.difficulty}, length=${ds2?.length}")
    println("  Requirements: ${ds2?.requirements?.take(100)}...")
    println("Total quests: ${QuestDatabase.getAll().size}")

    // Recipes
    println("\n--- Recipes ---")
    val cookingRecipes = RecipeDatabase.getBySkill("Cooking")
    println("Cooking recipes: ${cookingRecipes.size}")
    cookingRecipes.sortedByDescending { it.level ?: 0 }.take(3).forEach {
        println("  ${it.name}: lvl ${it.level}, xp ${it.experience}")
    }
    println("Total recipes: ${RecipeDatabase.getAll().size}")

    // Shops
    println("\n--- Shops ---")
    val allShops = ShopDatabase.getAll()
    println("Total shops: ${allShops.size}")
    allShops.take(3).forEach { println("  ${it.name} (${it.location}) - ${it.items.size} items") }

    // Spells
    println("\n--- Spells ---")
    val iceBarrage = SpellDatabase.getByName("Ice Barrage")
    println("Ice Barrage: lvl=${iceBarrage?.level}, xp=${iceBarrage?.experience}, book=${iceBarrage?.spellbook}")
    println("  Runes: ${iceBarrage?.runes}")

    val ancients = SpellDatabase.getBySpellbook("Ancient")
    println("Ancient spells: ${ancients.size}")
    println("Total spells: ${SpellDatabase.getAll().size}")

    // Varbits
    println("\n--- Varbits ---")
    println("Total varbits: ${VarbitDatabase.getAll().size}")
    VarbitDatabase.getAll().take(3).forEach { println("  ${it.index}: ${it.name} - ${it.content}") }

    println("\n=== All databases loaded successfully ===")
}

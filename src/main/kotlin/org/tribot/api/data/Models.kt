package org.tribot.api.data

data class ItemDefinition(
    val id: Int,
    val name: String,
    val members: Boolean,
    val tradeable: Boolean,
    val tradeableOnGe: Boolean,
    val stackable: Boolean,
    val cost: Int,
    val highAlch: Int?,
    val lowAlch: Int?,
    val buyLimit: Int?,
    val weight: Double?,
    val examine: String?,
    val questItem: Boolean,
    val equipment: EquipmentStats?,
    val weapon: WeaponStats?
)

data class EquipmentStats(
    val slot: String,
    val attackStab: Int,
    val attackSlash: Int,
    val attackCrush: Int,
    val attackMagic: Int,
    val attackRanged: Int,
    val defenceStab: Int,
    val defenceSlash: Int,
    val defenceCrush: Int,
    val defenceMagic: Int,
    val defenceRanged: Int,
    val meleeStrength: Int,
    val rangedStrength: Int,
    val magicDamage: Double,
    val prayer: Int,
    val requirements: Map<String, Int>
)

data class WeaponStats(
    val attackSpeed: Int,
    val weaponType: String,
    val stances: List<WeaponStance>
)

data class WeaponStance(
    val combatStyle: String,
    val attackType: String?,
    val attackStyle: String?,
    val experience: String?,
    val boosts: String?
)

data class MonsterDefinition(
    val id: Int,
    val name: String,
    val members: Boolean,
    val combatLevel: Int?,
    val hitpoints: Int?,
    val maxHit: String?,
    val attackSpeed: Int?,
    val size: Int?,
    val attackLevel: Int?,
    val strengthLevel: Int?,
    val defenceLevel: Int?,
    val magicLevel: Int?,
    val rangedLevel: Int?,
    val attackStab: Int?,
    val attackSlash: Int?,
    val attackCrush: Int?,
    val attackMagic: Int?,
    val attackRanged: Int?,
    val defenceStab: Int?,
    val defenceSlash: Int?,
    val defenceCrush: Int?,
    val defenceMagic: Int?,
    val defenceRanged: Int?,
    val strengthBonus: Int?,
    val rangedStrengthBonus: Int?,
    val magicDamageBonus: Int?,
    val slayerLevel: Int?,
    val slayerXp: Double?,
    val slayerCategory: String?,
    val assignedBy: String?,
    val elementalWeakness: String?,
    val elementalWeaknessPercent: Int?,
    val poisonous: String?,
    val immunePoison: Boolean?,
    val immuneVenom: Boolean?,
    val examine: String?
)

data class DropEntry(
    val monsterName: String,
    val itemName: String,
    val itemId: Int?,
    val quantity: String,
    val rarity: Double,
    val noted: Boolean,
    val rolls: Int
)

data class QuestDefinition(
    val name: String,
    val difficulty: String?,
    val length: String?,
    val requirements: String?,
    val startPoint: String?,
    val itemsRequired: String?,
    val enemiesToDefeat: String?,
    val ironmanConcerns: String?
)

data class RecipeDefinition(
    val name: String,
    val members: Boolean,
    val skill: String?,
    val level: Int?,
    val experience: Double?,
    val materials: List<RecipeMaterial>,
    val tools: List<String>,
    val facilities: List<String>,
    val boostable: Boolean
)

data class RecipeMaterial(
    val name: String,
    val quantity: Int
)

data class ShopDefinition(
    val name: String,
    val owner: String?,
    val location: String?,
    val members: Boolean,
    val items: List<ShopItem>
)

data class ShopItem(
    val itemName: String,
    val stock: Int?,
    val buyPrice: String?,
    val sellPrice: String?,
    val currency: String?,
    val restockTime: String?
)

data class SpellDefinition(
    val name: String,
    val spellbook: String,
    val level: Int,
    val experience: Double,
    val type: String,
    val runes: Map<String, Int>,
    val members: Boolean
)

data class VarbitDefinition(
    val index: Int,
    val name: String,
    val content: String?
)

data class Metadata(
    val version: Int,
    val scrapedAt: String,
    val files: Map<String, FileInfo>
)

data class FileInfo(
    val hash: String,
    val entries: Int
)

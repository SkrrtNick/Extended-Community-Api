package org.tribot.api.requirements

import net.runelite.api.Skill
import org.tribot.api.ApiContext
import org.tribot.api.data.ItemDatabase
import org.tribot.automation.script.core.tabs.EquipmentSlot
import java.util.logging.Logger

/**
 * Requires a specific item to be equipped in a given [EquipmentSlot].
 *
 * Optionally resolves skill requirements from the [ItemDatabase] so callers
 * can check whether the player meets the level requirements to equip the item
 * via [canEquip].
 */
class EquipmentRequirement(
    val itemId: Int,
    val slot: EquipmentSlot,
    val alternateIds: List<Int> = emptyList(),
    val displayName: String = "Item #$itemId",
    private val itemDatabase: ItemDatabase = ItemDatabase()
) : Requirement {

    private val allIds: Set<Int> get() = setOf(itemId) + alternateIds

    /**
     * Skill requirements needed to equip this item, resolved lazily from the
     * [ItemDatabase]. Returns an empty list when the item is not found in the
     * database, has no equipment stats, or has no skill requirements.
     */
    val skillRequirements: List<SkillRequirement> by lazy {
        val item = itemDatabase.get(itemId)
        if (item == null) {
            logger.warning("Item $itemId ($displayName) not found in ItemDatabase — cannot resolve equip requirements")
            return@lazy emptyList()
        }

        val requirements = item.equipment?.requirements ?: return@lazy emptyList()

        requirements.mapNotNull { (skillName, level) ->
            val skill = Skill.entries.find { it.getName().equals(skillName, ignoreCase = true) }
            if (skill == null) {
                logger.warning("Unknown skill '$skillName' in equip requirements for item $itemId ($displayName)")
                null
            } else {
                SkillRequirement(skill, level)
            }
        }
    }

    /**
     * Returns `true` if the player meets all skill requirements to equip this item.
     *
     * Fails open: if the item is not in the database or has no requirements,
     * this returns `true`.
     */
    fun canEquip(): Boolean = skillRequirements.all { it.check() }

    override fun check(): Boolean {
        val equipped = ApiContext.get().equipment.getItemIn(slot) ?: return false
        return equipped.id in allIds
    }

    override val displayText: String get() = "$displayName in ${slot.name}"

    companion object {
        private val logger = Logger.getLogger(EquipmentRequirement::class.java.name)
    }
}

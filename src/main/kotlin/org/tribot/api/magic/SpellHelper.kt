package org.tribot.api.magic

import net.runelite.api.Skill
import org.tribot.automation.script.ScriptContext

/**
 * Convenience helpers for casting spells and querying availability using the
 * typed [Spell] enum and the TRiBot automation SDK.
 */
object SpellHelper {

    /**
     * Casts (or selects) the given [spell] via the SDK's [ScriptContext.magic] API.
     *
     * For self-cast spells the cast happens immediately. For targeted spells the
     * spell is selected and the caller must click the target afterwards.
     *
     * @return true if the SDK reports the click was issued.
     */
    fun cast(ctx: ScriptContext, spell: Spell): Boolean =
        ctx.magic.cast(spell.spellName)

    /**
     * Checks whether the player currently meets the requirements to cast [spell]:
     * sufficient Magic level **and** enough of every required rune in inventory.
     *
     * Note: this does *not* account for equipped staves that provide unlimited
     * elemental runes, or other rune-saving effects. A future version could
     * inspect equipment for elemental staves.
     */
    fun canCast(ctx: ScriptContext, spell: Spell): Boolean {
        if (ctx.skills.getLevel(Skill.MAGIC) < spell.level) return false
        return spell.runes.all { req ->
            ctx.inventory.getCount(req.rune.itemId) >= req.amount
        }
    }

    /**
     * Returns every spell on the given [spellbook] whose level requirement is
     * at or below the player's current Magic level.
     */
    fun getAvailableSpells(ctx: ScriptContext, spellbook: Spellbook): List<Spell> {
        val magicLevel = ctx.skills.getLevel(Skill.MAGIC)
        return Spell.entries.filter { it.spellbook == spellbook && it.level <= magicLevel }
    }

    /**
     * Returns every spell that the player can currently cast (correct spellbook,
     * level, and runes) from the given [spellbook].
     */
    fun getCastableSpells(ctx: ScriptContext, spellbook: Spellbook): List<Spell> {
        val magicLevel = ctx.skills.getLevel(Skill.MAGIC)
        return Spell.entries.filter { spell ->
            spell.spellbook == spellbook &&
                spell.level <= magicLevel &&
                spell.runes.all { req -> ctx.inventory.getCount(req.rune.itemId) >= req.amount }
        }
    }
}

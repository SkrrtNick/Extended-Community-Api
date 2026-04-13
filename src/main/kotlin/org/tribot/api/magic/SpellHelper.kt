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
     * Checks whether the player currently meets all requirements to cast [spell]:
     * - Correct spellbook is active
     * - Sufficient Magic level
     * - Enough of every required rune (accounting for equipped staffs, tomes,
     *   and combination runes)
     */
    fun canCast(ctx: ScriptContext, spell: Spell): Boolean {
        if (Spellbook.getCurrent(ctx) != spell.spellbook) return false
        if (ctx.skills.getLevel(Skill.MAGIC) < spell.level) return false
        return spell.runes.all { req ->
            req.rune.getAvailableCount(ctx) >= req.amount
        }
    }

    /**
     * Returns every spell on the given [spellbook] whose level requirement is
     * at or below the player's current Magic level.
     *
     * Does not check rune availability — use [getCastableSpells] for that.
     */
    fun getAvailableSpells(ctx: ScriptContext, spellbook: Spellbook): List<Spell> {
        val magicLevel = ctx.skills.getLevel(Skill.MAGIC)
        return Spell.entries.filter { it.spellbook == spellbook && it.level <= magicLevel }
    }

    /**
     * Returns every spell that the player can currently cast from the given [spellbook]:
     * correct spellbook active, sufficient level, and enough runes (including
     * staff/tome/combo rune support).
     */
    fun getCastableSpells(ctx: ScriptContext, spellbook: Spellbook): List<Spell> =
        Spell.entries.filter { it.spellbook == spellbook && canCast(ctx, it) }
}

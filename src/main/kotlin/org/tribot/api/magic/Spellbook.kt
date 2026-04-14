package org.tribot.api.magic

import org.tribot.api.ApiContext

/**
 * The four spellbooks available in Old School RuneScape.
 *
 * The active spellbook is determined by varbit 4070:
 * 0 = Standard, 1 = Ancient, 2 = Lunar, 3 = Arceuus
 */
enum class Spellbook(val varbitValue: Int) {
    STANDARD(0),
    ANCIENT(1),
    LUNAR(2),
    ARCEUUS(3);

    companion object {
        private const val SPELLBOOK_VARBIT = 4070

        /**
         * Returns the player's currently active spellbook.
         */
        fun getCurrent(): Spellbook {
            val value = ApiContext.get().client.getVarbitValue(SPELLBOOK_VARBIT)
            return entries.find { it.varbitValue == value } ?: STANDARD
        }
    }
}

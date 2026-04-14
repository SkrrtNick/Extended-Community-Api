package org.tribot.api.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ConcurrentHashMap

/**
 * Database of all OSRS spell definitions, lazily loaded from `spells.json`
 * via [OsrsDataManager].
 *
 * Thread-safe: uses [ConcurrentHashMap] for storage and `synchronized`
 * with double-check on [loaded] for one-time initialization.
 *
 * Instances can be created with a custom [OsrsDataManager] (useful for
 * testing). The [companion object][Companion] exposes static convenience
 * methods that delegate to a default singleton.
 */
class SpellDatabase(private val manager: OsrsDataManager = OsrsDataManager()) {

    private val byName = ConcurrentHashMap<String, SpellDefinition>()
    private val bySpellbook = ConcurrentHashMap<String, List<SpellDefinition>>()

    @Volatile
    private var loaded = false

    private val gson = Gson()

    /**
     * Returns the [SpellDefinition] matching the given [name] (case-insensitive),
     * or null if not found.
     */
    fun getByName(name: String): SpellDefinition? {
        ensureLoaded()
        return byName[name.lowercase()]
    }

    /**
     * Returns all [SpellDefinition] instances for the given [spellbook]
     * (case-insensitive), or an empty list if not found.
     */
    fun getBySpellbook(spellbook: String): List<SpellDefinition> {
        ensureLoaded()
        return bySpellbook[spellbook.lowercase()] ?: emptyList()
    }

    /**
     * Returns all loaded [SpellDefinition] instances.
     */
    fun getAll(): Collection<SpellDefinition> {
        ensureLoaded()
        return byName.values
    }

    private fun ensureLoaded() {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            val json = manager.getFileContent("spells.json") ?: return
            val type = object : TypeToken<Map<String, SpellDefinition>>() {}.type
            val spells: Map<String, SpellDefinition> = gson.fromJson(json, type)
            val spellbookMap = mutableMapOf<String, MutableList<SpellDefinition>>()
            for ((_, spell) in spells) {
                byName[spell.name.lowercase()] = spell
                spellbookMap.getOrPut(spell.spellbook.lowercase()) { mutableListOf() }.add(spell)
            }
            bySpellbook.putAll(spellbookMap)
            loaded = true
        }
    }

    companion object {
        private val instance by lazy { SpellDatabase() }

        /**
         * Returns the [SpellDefinition] matching the given [name] (case-insensitive),
         * or null if not found.
         */
        @JvmStatic
        @JvmName("getBySpellName")
        fun getByName(name: String): SpellDefinition? = instance.getByName(name)

        /**
         * Returns all [SpellDefinition] instances for the given [spellbook]
         * (case-insensitive), or an empty list if not found.
         */
        @JvmStatic
        @JvmName("getBySpellbookName")
        fun getBySpellbook(spellbook: String): List<SpellDefinition> = instance.getBySpellbook(spellbook)

        /**
         * Returns all loaded [SpellDefinition] instances.
         */
        @JvmStatic
        @JvmName("getAllSpells")
        fun getAll(): Collection<SpellDefinition> = instance.getAll()
    }
}

package org.tribot.api.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ConcurrentHashMap

/**
 * Database of all OSRS quest definitions, lazily loaded from `quests.json`
 * via [OsrsDataManager].
 *
 * Thread-safe: uses [ConcurrentHashMap] for storage and `synchronized`
 * with double-check on [loaded] for one-time initialization.
 *
 * Instances can be created with a custom [OsrsDataManager] (useful for
 * testing). The [companion object][Companion] exposes static convenience
 * methods that delegate to a default singleton.
 */
class QuestDatabase(private val manager: OsrsDataManager = OsrsDataManager()) {

    private val byName = ConcurrentHashMap<String, QuestDefinition>()

    @Volatile
    private var loaded = false

    private val gson = Gson()

    /**
     * Returns the [QuestDefinition] matching the given [name] (case-insensitive),
     * or null if not found.
     */
    fun getByName(name: String): QuestDefinition? {
        ensureLoaded()
        return byName[name.lowercase()]
    }

    /**
     * Returns all loaded [QuestDefinition] instances.
     */
    fun getAll(): Collection<QuestDefinition> {
        ensureLoaded()
        return byName.values
    }

    private fun ensureLoaded() {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            val json = manager.getFileContent("quests.json") ?: return
            val type = object : TypeToken<Map<String, QuestDefinition>>() {}.type
            val quests: Map<String, QuestDefinition> = gson.fromJson(json, type)
            for ((_, quest) in quests) {
                byName[quest.name.lowercase()] = quest
            }
            loaded = true
        }
    }

    companion object {
        private val instance by lazy { QuestDatabase() }

        /**
         * Returns the [QuestDefinition] matching the given [name] (case-insensitive),
         * or null if not found.
         */
        @JvmStatic
        @JvmName("getByQuestName")
        fun getByName(name: String): QuestDefinition? = instance.getByName(name)

        /**
         * Returns all loaded [QuestDefinition] instances.
         */
        @JvmStatic
        @JvmName("getAllQuests")
        fun getAll(): Collection<QuestDefinition> = instance.getAll()
    }
}

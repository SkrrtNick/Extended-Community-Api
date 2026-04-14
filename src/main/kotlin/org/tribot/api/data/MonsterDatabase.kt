package org.tribot.api.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ConcurrentHashMap

/**
 * Database of all OSRS monster definitions, lazily loaded from `monsters.json`
 * via [OsrsDataManager].
 *
 * Thread-safe: uses [ConcurrentHashMap] for storage and `synchronized`
 * with double-check on [loaded] for one-time initialization.
 *
 * Instances can be created with a custom [OsrsDataManager] (useful for
 * testing). The [companion object][Companion] exposes static convenience
 * methods that delegate to a default singleton.
 */
class MonsterDatabase(private val manager: OsrsDataManager = OsrsDataManager()) {

    private val byId = ConcurrentHashMap<Int, MonsterDefinition>()
    private val byName = ConcurrentHashMap<String, MonsterDefinition>()

    @Volatile
    private var loaded = false

    private val gson = Gson()

    /**
     * Returns the [MonsterDefinition] for the given [id], or null if not found.
     */
    fun get(id: Int): MonsterDefinition? {
        ensureLoaded()
        return byId[id]
    }

    /**
     * Returns the [MonsterDefinition] matching the given [name] (case-insensitive),
     * or null if not found.
     */
    fun getByName(name: String): MonsterDefinition? {
        ensureLoaded()
        return byName[name.lowercase()]
    }

    /**
     * Returns all loaded [MonsterDefinition] instances.
     */
    fun getAll(): Collection<MonsterDefinition> {
        ensureLoaded()
        return byId.values
    }

    private fun ensureLoaded() {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            val json = manager.getFileContent("monsters.json") ?: return
            val type = object : TypeToken<Map<String, MonsterDefinition>>() {}.type
            val monsters: Map<String, MonsterDefinition> = gson.fromJson(json, type)
            for ((_, monster) in monsters) {
                byId[monster.id] = monster
                byName[monster.name.lowercase()] = monster
            }
            loaded = true
        }
    }

    companion object {
        private val instance by lazy { MonsterDatabase() }

        /**
         * Returns the [MonsterDefinition] for the given [id], or null if not found.
         */
        @JvmStatic
        @JvmName("getById")
        fun get(id: Int): MonsterDefinition? = instance.get(id)

        /**
         * Returns the [MonsterDefinition] matching the given [name] (case-insensitive),
         * or null if not found.
         */
        @JvmStatic
        @JvmName("getByMonsterName")
        fun getByName(name: String): MonsterDefinition? = instance.getByName(name)

        /**
         * Returns all loaded [MonsterDefinition] instances.
         */
        @JvmStatic
        @JvmName("getAllMonsters")
        fun getAll(): Collection<MonsterDefinition> = instance.getAll()
    }
}

package org.tribot.api.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ConcurrentHashMap

/**
 * Database of all OSRS item definitions, lazily loaded from `items.json`
 * via [OsrsDataManager].
 *
 * Thread-safe: uses [ConcurrentHashMap] for storage and `synchronized`
 * with double-check on [loaded] for one-time initialization.
 *
 * Instances can be created with a custom [OsrsDataManager] (useful for
 * testing). The [companion object][Companion] exposes static convenience
 * methods that delegate to a default singleton.
 */
class ItemDatabase(private val manager: OsrsDataManager = OsrsDataManager()) {

    private val byId = ConcurrentHashMap<Int, ItemDefinition>()
    private val byName = ConcurrentHashMap<String, ItemDefinition>()

    @Volatile
    private var loaded = false

    private val gson = Gson()

    /**
     * Returns the [ItemDefinition] for the given [id], or null if not found.
     */
    fun get(id: Int): ItemDefinition? {
        ensureLoaded()
        return byId[id]
    }

    /**
     * Returns the [ItemDefinition] matching the given [name] (case-insensitive),
     * or null if not found.
     */
    fun getByName(name: String): ItemDefinition? {
        ensureLoaded()
        return byName[name.lowercase()]
    }

    /**
     * Returns all loaded [ItemDefinition] instances.
     */
    fun getAll(): Collection<ItemDefinition> {
        ensureLoaded()
        return byId.values
    }

    private fun ensureLoaded() {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            val json = manager.getFileContent("items.json") ?: return
            val type = object : TypeToken<Map<String, ItemDefinition>>() {}.type
            val items: Map<String, ItemDefinition> = gson.fromJson(json, type)
            for ((_, item) in items) {
                byId[item.id] = item
                byName[item.name.lowercase()] = item
            }
            loaded = true
        }
    }

    companion object {
        private val instance by lazy { ItemDatabase() }

        /**
         * Returns the [ItemDefinition] for the given [id], or null if not found.
         */
        @JvmStatic
        @JvmName("getById")
        fun get(id: Int): ItemDefinition? = instance.get(id)

        /**
         * Returns the [ItemDefinition] matching the given [name] (case-insensitive),
         * or null if not found.
         */
        @JvmStatic
        @JvmName("getByItemName")
        fun getByName(name: String): ItemDefinition? = instance.getByName(name)

        /**
         * Returns all loaded [ItemDefinition] instances.
         */
        @JvmStatic
        @JvmName("getAllItems")
        fun getAll(): Collection<ItemDefinition> = instance.getAll()
    }
}

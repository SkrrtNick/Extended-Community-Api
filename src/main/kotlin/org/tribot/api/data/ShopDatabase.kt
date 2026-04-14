package org.tribot.api.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ConcurrentHashMap

/**
 * Database of all OSRS shop definitions, lazily loaded from `shops.json`
 * via [OsrsDataManager].
 *
 * Thread-safe: uses [ConcurrentHashMap] for storage and `synchronized`
 * with double-check on [loaded] for one-time initialization.
 *
 * Instances can be created with a custom [OsrsDataManager] (useful for
 * testing). The [companion object][Companion] exposes static convenience
 * methods that delegate to a default singleton.
 */
class ShopDatabase(private val manager: OsrsDataManager = OsrsDataManager.shared) {

    private val byName = ConcurrentHashMap<String, ShopDefinition>()

    @Volatile
    private var loaded = false

    private val gson = Gson()

    /**
     * Returns the [ShopDefinition] matching the given [name] (case-insensitive),
     * or null if not found.
     */
    fun getByName(name: String): ShopDefinition? {
        ensureLoaded()
        return byName[name.lowercase()]
    }

    /**
     * Returns all loaded [ShopDefinition] instances.
     */
    fun getAll(): Collection<ShopDefinition> {
        ensureLoaded()
        return byName.values
    }

    private fun ensureLoaded() {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            val json = manager.getFileContent("shops.json") ?: return
            val type = object : TypeToken<Map<String, ShopDefinition>>() {}.type
            val shops: Map<String, ShopDefinition> = gson.fromJson(json, type)
            for ((_, shop) in shops) {
                byName[shop.name.lowercase()] = shop
            }
            loaded = true
        }
    }

    companion object {
        private val instance by lazy { ShopDatabase() }

        /**
         * Returns the [ShopDefinition] matching the given [name] (case-insensitive),
         * or null if not found.
         */
        @JvmStatic
        @JvmName("getByShopName")
        fun getByName(name: String): ShopDefinition? = instance.getByName(name)

        /**
         * Returns all loaded [ShopDefinition] instances.
         */
        @JvmStatic
        @JvmName("getAllShops")
        fun getAll(): Collection<ShopDefinition> = instance.getAll()
    }
}

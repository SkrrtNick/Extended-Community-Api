package org.tribot.api.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ConcurrentHashMap

/**
 * Database of all OSRS monster drop entries, lazily loaded from `drops.json`
 * via [OsrsDataManager].
 *
 * Thread-safe: uses [ConcurrentHashMap] for storage and `synchronized`
 * with double-check on [loaded] for one-time initialization.
 *
 * Instances can be created with a custom [OsrsDataManager] (useful for
 * testing). The [companion object][Companion] exposes static convenience
 * methods that delegate to a default singleton.
 */
class DropDatabase(private val manager: OsrsDataManager = OsrsDataManager()) {

    private val byMonster = ConcurrentHashMap<String, List<DropEntry>>()
    private val byItem = ConcurrentHashMap<String, List<DropEntry>>()
    private val allDrops = mutableListOf<DropEntry>()

    @Volatile
    private var loaded = false

    private val gson = Gson()

    /**
     * Returns all [DropEntry] instances for the given monster [name]
     * (case-insensitive), or an empty list if not found.
     */
    fun getDropsForMonster(name: String): List<DropEntry> {
        ensureLoaded()
        return byMonster[name.lowercase()] ?: emptyList()
    }

    /**
     * Returns all [DropEntry] instances for the given item [name]
     * (case-insensitive), or an empty list if not found.
     */
    fun getDropsForItem(name: String): List<DropEntry> {
        ensureLoaded()
        return byItem[name.lowercase()] ?: emptyList()
    }

    /**
     * Returns all loaded [DropEntry] instances.
     */
    fun getAll(): List<DropEntry> {
        ensureLoaded()
        return allDrops
    }

    private fun ensureLoaded() {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            val json = manager.getFileContent("drops.json") ?: return
            val type = object : TypeToken<List<DropEntry>>() {}.type
            val drops: List<DropEntry> = gson.fromJson(json, type)
            allDrops.addAll(drops)
            val monsterMap = mutableMapOf<String, MutableList<DropEntry>>()
            val itemMap = mutableMapOf<String, MutableList<DropEntry>>()
            for (drop in drops) {
                monsterMap.getOrPut(drop.monsterName.lowercase()) { mutableListOf() }.add(drop)
                itemMap.getOrPut(drop.itemName.lowercase()) { mutableListOf() }.add(drop)
            }
            byMonster.putAll(monsterMap)
            byItem.putAll(itemMap)
            loaded = true
        }
    }

    companion object {
        private val instance by lazy { DropDatabase() }

        /**
         * Returns all [DropEntry] instances for the given monster [name]
         * (case-insensitive), or an empty list if not found.
         */
        @JvmStatic
        @JvmName("getDropsForMonsterName")
        fun getDropsForMonster(name: String): List<DropEntry> = instance.getDropsForMonster(name)

        /**
         * Returns all [DropEntry] instances for the given item [name]
         * (case-insensitive), or an empty list if not found.
         */
        @JvmStatic
        @JvmName("getDropsForItemName")
        fun getDropsForItem(name: String): List<DropEntry> = instance.getDropsForItem(name)

        /**
         * Returns all loaded [DropEntry] instances.
         */
        @JvmStatic
        @JvmName("getAllDrops")
        fun getAll(): List<DropEntry> = instance.getAll()
    }
}

package org.tribot.api.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ConcurrentHashMap

/**
 * Database of all OSRS varbit definitions, lazily loaded from `varbits.json`
 * via [OsrsDataManager].
 *
 * Thread-safe: uses [ConcurrentHashMap] for storage and `synchronized`
 * with double-check on [loaded] for one-time initialization.
 *
 * Instances can be created with a custom [OsrsDataManager] (useful for
 * testing). The [companion object][Companion] exposes static convenience
 * methods that delegate to a default singleton.
 */
class VarbitDatabase(private val manager: OsrsDataManager = OsrsDataManager()) {

    private val byIndex = ConcurrentHashMap<Int, VarbitDefinition>()
    private val byName = ConcurrentHashMap<String, VarbitDefinition>()

    @Volatile
    private var loaded = false

    private val gson = Gson()

    /**
     * Returns the [VarbitDefinition] for the given [index], or null if not found.
     */
    fun get(index: Int): VarbitDefinition? {
        ensureLoaded()
        return byIndex[index]
    }

    /**
     * Returns the [VarbitDefinition] matching the given [name] (case-insensitive),
     * or null if not found.
     */
    fun getByName(name: String): VarbitDefinition? {
        ensureLoaded()
        return byName[name.lowercase()]
    }

    /**
     * Returns all loaded [VarbitDefinition] instances.
     */
    fun getAll(): Collection<VarbitDefinition> {
        ensureLoaded()
        return byIndex.values
    }

    private fun ensureLoaded() {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            val json = manager.getFileContent("varbits.json") ?: return
            val type = object : TypeToken<Map<String, VarbitDefinition>>() {}.type
            val varbits: Map<String, VarbitDefinition> = gson.fromJson(json, type)
            for ((_, varbit) in varbits) {
                byIndex[varbit.index] = varbit
                byName[varbit.name.lowercase()] = varbit
            }
            loaded = true
        }
    }

    companion object {
        private val instance by lazy { VarbitDatabase() }

        /**
         * Returns the [VarbitDefinition] for the given [index], or null if not found.
         */
        @JvmStatic
        @JvmName("getByIndex")
        fun get(index: Int): VarbitDefinition? = instance.get(index)

        /**
         * Returns the [VarbitDefinition] matching the given [name] (case-insensitive),
         * or null if not found.
         */
        @JvmStatic
        @JvmName("getByVarbitName")
        fun getByName(name: String): VarbitDefinition? = instance.getByName(name)

        /**
         * Returns all loaded [VarbitDefinition] instances.
         */
        @JvmStatic
        @JvmName("getAllVarbits")
        fun getAll(): Collection<VarbitDefinition> = instance.getAll()
    }
}

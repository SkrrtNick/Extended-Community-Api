package org.tribot.api.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ConcurrentHashMap

/**
 * Database of all OSRS recipe definitions, lazily loaded from `recipes.json`
 * via [OsrsDataManager].
 *
 * Thread-safe: uses [ConcurrentHashMap] for storage and `synchronized`
 * with double-check on [loaded] for one-time initialization.
 *
 * Instances can be created with a custom [OsrsDataManager] (useful for
 * testing). The [companion object][Companion] exposes static convenience
 * methods that delegate to a default singleton.
 */
class RecipeDatabase(private val manager: OsrsDataManager = OsrsDataManager()) {

    private val byName = ConcurrentHashMap<String, RecipeDefinition>()
    private val bySkill = ConcurrentHashMap<String, List<RecipeDefinition>>()

    @Volatile
    private var loaded = false

    private val gson = Gson()

    /**
     * Returns the [RecipeDefinition] matching the given [name] (case-insensitive),
     * or null if not found.
     */
    fun getByName(name: String): RecipeDefinition? {
        ensureLoaded()
        return byName[name.lowercase()]
    }

    /**
     * Returns all [RecipeDefinition] instances for the given [skill]
     * (case-insensitive), or an empty list if not found.
     */
    fun getBySkill(skill: String): List<RecipeDefinition> {
        ensureLoaded()
        return bySkill[skill.lowercase()] ?: emptyList()
    }

    /**
     * Returns all loaded [RecipeDefinition] instances.
     */
    fun getAll(): Collection<RecipeDefinition> {
        ensureLoaded()
        return byName.values
    }

    private fun ensureLoaded() {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            val json = manager.getFileContent("recipes.json") ?: return
            val type = object : TypeToken<Map<String, RecipeDefinition>>() {}.type
            val recipes: Map<String, RecipeDefinition> = gson.fromJson(json, type)
            val skillMap = mutableMapOf<String, MutableList<RecipeDefinition>>()
            for ((_, recipe) in recipes) {
                byName[recipe.name.lowercase()] = recipe
                val skill = recipe.skill
                if (skill != null) {
                    skillMap.getOrPut(skill.lowercase()) { mutableListOf() }.add(recipe)
                }
            }
            bySkill.putAll(skillMap)
            loaded = true
        }
    }

    companion object {
        private val instance by lazy { RecipeDatabase() }

        /**
         * Returns the [RecipeDefinition] matching the given [name] (case-insensitive),
         * or null if not found.
         */
        @JvmStatic
        @JvmName("getByRecipeName")
        fun getByName(name: String): RecipeDefinition? = instance.getByName(name)

        /**
         * Returns all [RecipeDefinition] instances for the given [skill]
         * (case-insensitive), or an empty list if not found.
         */
        @JvmStatic
        @JvmName("getByRecipeSkill")
        fun getBySkill(skill: String): List<RecipeDefinition> = instance.getBySkill(skill)

        /**
         * Returns all loaded [RecipeDefinition] instances.
         */
        @JvmStatic
        @JvmName("getAllRecipes")
        fun getAll(): Collection<RecipeDefinition> = instance.getAll()
    }
}

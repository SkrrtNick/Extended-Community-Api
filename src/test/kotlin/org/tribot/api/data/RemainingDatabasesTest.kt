package org.tribot.api.data

import com.google.gson.Gson
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RemainingDatabasesTest {

    private val gson = Gson()
    private lateinit var tempDir: Path
    private lateinit var manager: OsrsDataManager

    @BeforeTest
    fun setup() {
        tempDir = Files.createTempDirectory("remaining-db-test")

        // --- quests.json ---
        val quests = mapOf(
            "Dragon Slayer" to QuestDefinition(
                name = "Dragon Slayer",
                difficulty = "Experienced",
                length = "Long",
                requirements = "32 Quest Points",
                startPoint = "Champions' Guild",
                itemsRequired = "Unfired bowl, Wizard's mind bomb, Silk, Lobster pot",
                enemiesToDefeat = "Elvarg (level 83)",
                ironmanConcerns = null
            )
        )
        writeDataFile("quests.json", quests)

        // --- recipes.json ---
        val recipes = mapOf(
            "Bronze bar" to RecipeDefinition(
                name = "Bronze bar",
                members = false,
                skill = "Smithing",
                level = 1,
                experience = 6.2,
                materials = listOf(
                    RecipeMaterial(name = "Copper ore", quantity = 1),
                    RecipeMaterial(name = "Tin ore", quantity = 1)
                ),
                tools = emptyList(),
                facilities = listOf("Furnace"),
                boostable = false
            ),
            "Iron bar" to RecipeDefinition(
                name = "Iron bar",
                members = false,
                skill = "Smithing",
                level = 15,
                experience = 12.5,
                materials = listOf(
                    RecipeMaterial(name = "Iron ore", quantity = 1)
                ),
                tools = emptyList(),
                facilities = listOf("Furnace"),
                boostable = false
            ),
            "Oak longbow" to RecipeDefinition(
                name = "Oak longbow",
                members = false,
                skill = "Fletching",
                level = 25,
                experience = 25.0,
                materials = listOf(
                    RecipeMaterial(name = "Oak logs", quantity = 1),
                    RecipeMaterial(name = "Bow string", quantity = 1)
                ),
                tools = listOf("Knife"),
                facilities = emptyList(),
                boostable = false
            )
        )
        writeDataFile("recipes.json", recipes)

        // --- shops.json ---
        val shops = mapOf(
            "Bob's Brilliant Axes" to ShopDefinition(
                name = "Bob's Brilliant Axes",
                owner = "Bob",
                location = "Lumbridge",
                members = false,
                items = listOf(
                    ShopItem(
                        itemName = "Bronze axe",
                        stock = 5,
                        buyPrice = "16",
                        sellPrice = "4",
                        currency = "Coins",
                        restockTime = null
                    )
                )
            )
        )
        writeDataFile("shops.json", shops)

        // --- spells.json ---
        val spells = mapOf(
            "Wind Strike" to SpellDefinition(
                name = "Wind Strike",
                spellbook = "Standard",
                level = 1,
                experience = 5.5,
                type = "Combat",
                runes = mapOf("Air rune" to 1, "Mind rune" to 1),
                members = false
            ),
            "Ice Barrage" to SpellDefinition(
                name = "Ice Barrage",
                spellbook = "Ancient",
                level = 94,
                experience = 52.0,
                type = "Combat",
                runes = mapOf("Death rune" to 4, "Blood rune" to 2, "Water rune" to 6),
                members = true
            ),
            "Confuse" to SpellDefinition(
                name = "Confuse",
                spellbook = "Standard",
                level = 3,
                experience = 13.0,
                type = "Combat",
                runes = mapOf("Body rune" to 1, "Earth rune" to 2, "Water rune" to 3),
                members = false
            )
        )
        writeDataFile("spells.json", spells)

        // --- varbits.json ---
        val varbits = mapOf(
            "3534" to VarbitDefinition(
                index = 3534,
                name = "Diary: Varrock Easy",
                content = "0-1"
            ),
            "4070" to VarbitDefinition(
                index = 4070,
                name = "Slayer Task Count",
                content = null
            )
        )
        writeDataFile("varbits.json", varbits)

        manager = OsrsDataManager(
            cacheDir = tempDir,
            baseUrl = "http://localhost:1/nonexistent"
        )
    }

    private fun writeDataFile(fileName: String, data: Any) {
        val json = gson.toJson(data)
        Files.writeString(tempDir.resolve(fileName), json)
        val hash = OsrsDataManager.sha256(json)

        // Append to metadata — we rebuild each time for simplicity
        val metaFile = tempDir.resolve("metadata.json")
        val existingMeta = if (Files.exists(metaFile)) {
            gson.fromJson(Files.readString(metaFile), Metadata::class.java)
        } else {
            Metadata(version = 1, scrapedAt = "2025-01-15T12:00:00Z", files = emptyMap())
        }
        val updatedMeta = existingMeta.copy(
            files = existingMeta.files + (fileName to FileInfo(hash = hash, entries = 0))
        )
        Files.writeString(metaFile, gson.toJson(updatedMeta))
    }

    @AfterTest
    fun cleanup() {
        if (::tempDir.isInitialized) {
            tempDir.toFile().deleteRecursively()
        }
    }

    // --- QuestDatabase ---

    @Test
    fun `QuestDatabase getByName returns quest`() {
        val db = QuestDatabase(manager)
        val quest = db.getByName("Dragon Slayer")
        assertNotNull(quest)
        assertEquals("Dragon Slayer", quest.name)
        assertEquals("Experienced", quest.difficulty)
    }

    // --- RecipeDatabase ---

    @Test
    fun `RecipeDatabase getByName returns recipe`() {
        val db = RecipeDatabase(manager)
        val recipe = db.getByName("Bronze bar")
        assertNotNull(recipe)
        assertEquals("Bronze bar", recipe.name)
        assertEquals("Smithing", recipe.skill)
        assertEquals(1, recipe.level)
    }

    @Test
    fun `RecipeDatabase getBySkill returns matching recipes`() {
        val db = RecipeDatabase(manager)
        val smithing = db.getBySkill("Smithing")
        assertEquals(2, smithing.size)
        assertTrue(smithing.any { it.name == "Bronze bar" })
        assertTrue(smithing.any { it.name == "Iron bar" })

        val fletching = db.getBySkill("Fletching")
        assertEquals(1, fletching.size)
        assertEquals("Oak longbow", fletching[0].name)
    }

    // --- ShopDatabase ---

    @Test
    fun `ShopDatabase getByName returns shop`() {
        val db = ShopDatabase(manager)
        val shop = db.getByName("Bob's Brilliant Axes")
        assertNotNull(shop)
        assertEquals("Bob's Brilliant Axes", shop.name)
        assertEquals("Bob", shop.owner)
        assertEquals("Lumbridge", shop.location)
    }

    // --- SpellDatabase ---

    @Test
    fun `SpellDatabase getByName returns spell`() {
        val db = SpellDatabase(manager)
        val spell = db.getByName("Wind Strike")
        assertNotNull(spell)
        assertEquals("Wind Strike", spell.name)
        assertEquals("Standard", spell.spellbook)
        assertEquals(1, spell.level)
    }

    @Test
    fun `SpellDatabase getBySpellbook returns matching spells`() {
        val db = SpellDatabase(manager)
        val standard = db.getBySpellbook("Standard")
        assertEquals(2, standard.size)
        assertTrue(standard.any { it.name == "Wind Strike" })
        assertTrue(standard.any { it.name == "Confuse" })

        val ancient = db.getBySpellbook("Ancient")
        assertEquals(1, ancient.size)
        assertEquals("Ice Barrage", ancient[0].name)
    }

    // --- VarbitDatabase ---

    @Test
    fun `VarbitDatabase get returns varbit by index`() {
        val db = VarbitDatabase(manager)
        val varbit = db.get(3534)
        assertNotNull(varbit)
        assertEquals(3534, varbit.index)
        assertEquals("Diary: Varrock Easy", varbit.name)
        assertEquals("0-1", varbit.content)
    }

    @Test
    fun `VarbitDatabase getByName returns varbit`() {
        val db = VarbitDatabase(manager)
        val varbit = db.getByName("Slayer Task Count")
        assertNotNull(varbit)
        assertEquals(4070, varbit.index)
        assertEquals("Slayer Task Count", varbit.name)
    }
}

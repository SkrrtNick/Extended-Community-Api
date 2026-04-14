package org.tribot.api.data

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration test that hits the real osrs-data GitHub repo.
 * Run with: ./gradlew test --tests "org.tribot.api.data.OsrsDataIntegrationTest"
 */
class OsrsDataIntegrationTest {

    @Test
    fun `fetch real items and verify well-known items`() {
        val manager = OsrsDataManager()
        val db = ItemDatabase(manager)

        val whip = db.get(4151)
        assertNotNull(whip, "Abyssal whip (4151) should exist")
        assertTrue(whip.name.contains("whip", ignoreCase = true))
        println("Abyssal whip: ${whip.name}, alch=${whip.highAlch}, equip=${whip.equipment?.slot}")

        val bandos = db.getByName("Bandos chestplate")
        assertNotNull(bandos, "Bandos chestplate should exist")
        println("Bandos chestplate: id=${bandos.id}, def=${bandos.equipment?.defenceSlash}")

        assertTrue(db.getAll().size > 10000, "Should have >10k items, got ${db.getAll().size}")
        println("Total items loaded: ${db.getAll().size}")
    }

    @Test
    fun `fetch real monsters and verify well-known monsters`() {
        val manager = OsrsDataManager()
        val db = MonsterDatabase(manager)

        val zulrah = db.getByName("Zulrah")
        assertNotNull(zulrah, "Zulrah should exist")
        println("Zulrah: combat=${zulrah.combatLevel}, hp=${zulrah.hitpoints}")

        assertTrue(db.getAll().size > 1000, "Should have >1k monsters, got ${db.getAll().size}")
        println("Total monsters loaded: ${db.getAll().size}")
    }

    @Test
    fun `fetch real drops and verify well-known drops`() {
        val manager = OsrsDataManager()
        val db = DropDatabase(manager)

        val zulrahDrops = db.getDropsForMonster("Zulrah")
        assertTrue(zulrahDrops.isNotEmpty(), "Zulrah should have drops")
        println("Zulrah drops: ${zulrahDrops.size} entries")

        assertTrue(db.getAll().size > 10000, "Should have >10k drops, got ${db.getAll().size}")
        println("Total drops loaded: ${db.getAll().size}")
    }
}

package org.tribot.api.magic

import net.runelite.api.Client
import net.runelite.api.Skill
import org.tribot.automation.script.*
import org.tribot.automation.script.addon.AddonLibraries
import org.tribot.automation.script.client.Runtime
import org.tribot.automation.script.client.Scripts
import org.tribot.automation.script.client.Window
import org.tribot.automation.script.client.sidecars.BreakHandler
import org.tribot.automation.script.client.sidecars.LoginHandler
import org.tribot.automation.script.client.sidecars.Sidecars
import org.tribot.automation.script.core.*
import org.tribot.automation.script.core.definition.Definitions
import org.tribot.automation.script.core.tabs.*
import org.tribot.automation.script.core.widgets.*
import org.tribot.automation.script.core.world.World
import org.tribot.automation.script.core.world.WorldCache
import org.tribot.automation.script.event.Events
import org.tribot.automation.script.input.*
import org.tribot.automation.script.logging.ScriptLogger
import org.tribot.automation.script.util.Waiting

// ---------------------------------------------------------------------------
// Minimal fakes used exclusively for unit-testing SpellHelper / Staff / Spellbook.
// Only the subset of ScriptContext actually exercised by the production code
// is implemented; every other member throws if accidentally accessed.
// ---------------------------------------------------------------------------

/**
 * A fake [Skills] that returns a configurable level for [Skill.MAGIC].
 */
class FakeSkills(var magicLevel: Int = 1) : Skills {
    override fun getLevel(skill: Skill): Int = if (skill == Skill.MAGIC) magicLevel else 1
    override fun getBoostedLevel(skill: Skill): Int = getLevel(skill)
    override fun getXp(skill: Skill): Int = 0
    override fun getXpToNextLevel(skill: Skill): Int = 0
    override fun getXpToLevel(skill: Skill, targetLevel: Int): Int = 0
    override fun getPercentToNextLevel(skill: Skill): Int = 0
    override fun getPercentToLevel(skill: Skill, targetLevel: Int): Int = 0
    override fun getXpForLevel(level: Int): Int = 0
    override fun getLevelForXp(xp: Int): Int = 0
    override fun hover(skill: Skill): Boolean = false
    override fun click(skill: Skill): Boolean = false
}

/**
 * A fake [Inventory] backed by a simple map of itemId -> count.
 */
class FakeInventory(private val items: MutableMap<Int, Int> = mutableMapOf()) : Inventory {
    fun setCount(itemId: Int, count: Int) { items[itemId] = count }
    override fun getCount(itemId: Int): Int = items.getOrDefault(itemId, 0)
    override fun contains(itemId: Int): Boolean = getCount(itemId) > 0
    override fun containsAll(vararg itemIds: Int): Boolean = itemIds.all { contains(it) }
    override fun getItems(): List<InventoryItem> = items.map { InventoryItem(it.key, it.value, 0) }
    override fun isFull(): Boolean = false
    override fun isEmpty(): Boolean = items.isEmpty()
    override fun emptySlots(): Int = 28 - items.size
    override fun clickSlot(slotIndex: Int, option: String?): Boolean = false
    override fun clickItem(itemId: Int, option: String?): Boolean = false
}

/**
 * A fake [Magic] that records calls for assertion.
 */
class FakeMagic : Magic {
    var lastCast: String? = null
        private set
    var castResult: Boolean = true

    override fun cast(name: String): Boolean {
        lastCast = name
        return castResult
    }
    override fun isSpellSelected(): Boolean = false
    override fun getSelectedSpellName(): String? = null
}

/**
 * A fake [Equipment] backed by a mutable list.
 */
class FakeEquipment(private val equipped: MutableList<EquippedItem> = mutableListOf()) : Equipment {
    fun equip(item: EquippedItem) { equipped.add(item) }
    fun clear() { equipped.clear() }
    override fun getItems(): List<EquippedItem> = equipped.toList()
    override fun getItemIn(slot: EquipmentSlot): EquippedItem? = equipped.find { it.slot == slot }
    override fun isEquipped(itemId: Int): Boolean = equipped.any { it.id == itemId }
    override fun clickSlot(slot: EquipmentSlot, option: String?): Boolean = false
}

/**
 * A fake [Client] that only supports varbit lookups (for spellbook detection).
 */
class FakeClient : Client by io.mockk.mockk(relaxed = true) {
    private val varbits = mutableMapOf<Int, Int>()
    fun setFakeVarbit(id: Int, value: Int) { varbits[id] = value }
    override fun getVarbitValue(varbitId: Int): Int = varbits.getOrDefault(varbitId, 0)
}

// A stub that throws on every member; individual test fixtures override the
// properties they need with the fakes above.
private fun stub(name: String): Nothing = throw UnsupportedOperationException("$name not faked")

fun fakeContext(
    skills: FakeSkills = FakeSkills(),
    inventory: FakeInventory = FakeInventory(),
    magic: FakeMagic = FakeMagic(),
    equipment: FakeEquipment = FakeEquipment(),
    client: FakeClient = FakeClient()
): ScriptContext = object : ScriptContext {
    override val client: Client = client
    override val clientThread: ClientThread get() = stub("clientThread")
    override val mouse: Mouse get() = stub("mouse")
    override val keyboard: Keyboard get() = stub("keyboard")
    override val interaction: Interaction get() = stub("interaction")
    override val definitions: Definitions get() = stub("definitions")
    override val banking: Banking get() = stub("banking")
    override val pinScreen: PinScreen get() = stub("pinScreen")
    override val enterAmount: EnterAmount get() = stub("enterAmount")
    override val login: Login get() = stub("login")
    override val worldCache: WorldCache get() = stub("worldCache")
    override val inventory: Inventory = inventory
    override val equipment: Equipment = equipment
    override val tabs: Tabs get() = stub("tabs")
    override val skills: Skills = skills
    override val prayer: Prayer get() = stub("prayer")
    override val combat: Combat get() = stub("combat")
    override val magic: Magic = magic
    override val logout: Logout get() = stub("logout")
    override val camera: Camera get() = stub("camera")
    override val minimap: Minimap get() = stub("minimap")
    override val worldViews: WorldViews get() = stub("worldViews")
    override val chooseOption: ChooseOption get() = stub("chooseOption")
    override val window: Window get() = stub("window")
    override val screen: Screen get() = stub("screen")
    override val screenPrediction: ScreenPrediction get() = stub("screenPrediction")
    override val runtime: Runtime get() = stub("runtime")
    override val scripts: Scripts get() = stub("scripts")
    override val sidecars: Sidecars get() = stub("sidecars")
    override val loginHandler: LoginHandler get() = stub("loginHandler")
    override val events: Events get() = stub("events")
    override val waiting: Waiting get() = stub("waiting")
    override val permissions: Permissions get() = stub("permissions")
    override val addonLibraries: AddonLibraries get() = stub("addonLibraries")
    override val automation: Automation get() = stub("automation")
    override val logger: ScriptLogger get() = stub("logger")
}

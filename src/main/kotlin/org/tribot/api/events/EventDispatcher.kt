package org.tribot.api.events

import net.runelite.api.*
import org.tribot.automation.script.ScriptContext
import org.tribot.automation.script.core.GroundItem
import org.tribot.automation.script.core.tabs.EquippedItem
import org.tribot.automation.script.core.tabs.InventoryItem
import org.tribot.automation.script.event.ListenerRegistration

/**
 * Derives RuneLite-equivalent events by polling game state each tick/frame.
 *
 * Create one per script, call [start] to begin listening, [stop] to clean up.
 * Register callbacks before calling start.
 *
 * ```
 * val dispatcher = EventDispatcher(ctx)
 * dispatcher.onStatChanged { skill, oldXp, newXp, _, _, _, _ -> println("Gained ${newXp - oldXp} xp") }
 * dispatcher.onNpcSpawned { npc -> println("${npc.name} appeared") }
 * dispatcher.onVarbitChanged(4070) { old, new -> println("Spellbook: $old -> $new") }
 * dispatcher.onWidgetOpened(465) { println("GE opened") }
 * dispatcher.start()
 * ```
 */
class EventDispatcher(private val ctx: ScriptContext) {

    private var tickRegistration: ListenerRegistration? = null
    private var renderRegistration: ListenerRegistration? = null

    // --- Callback lists ---

    // Stat events
    private val statChangedListeners = mutableListOf<(skill: Skill, oldXp: Int, newXp: Int, oldLevel: Int, newLevel: Int, oldBoosted: Int, newBoosted: Int) -> Unit>()

    // Inventory/equipment
    private val inventoryChangedListeners = mutableListOf<(old: List<InventoryItem>, new: List<InventoryItem>) -> Unit>()
    private val equipmentChangedListeners = mutableListOf<(old: List<EquippedItem>, new: List<EquippedItem>) -> Unit>()

    // NPC events
    private val npcSpawnedListeners = mutableListOf<(NPC) -> Unit>()
    private val npcDespawnedListeners = mutableListOf<(NPC) -> Unit>()

    // Player events
    private val playerSpawnedListeners = mutableListOf<(Player) -> Unit>()
    private val playerDespawnedListeners = mutableListOf<(Player) -> Unit>()

    // Object events
    private val objectSpawnedListeners = mutableListOf<(TileObject) -> Unit>()
    private val objectDespawnedListeners = mutableListOf<(TileObject) -> Unit>()

    // Ground item events
    private val groundItemSpawnedListeners = mutableListOf<(GroundItem) -> Unit>()
    private val groundItemDespawnedListeners = mutableListOf<(GroundItem) -> Unit>()

    // Varbit events
    private val watchedVarbits = mutableSetOf<Int>()
    private val varbitChangedListeners = mutableListOf<(varbitId: Int, oldValue: Int, newValue: Int) -> Unit>()

    // Setting (varp) events
    private val watchedSettings = mutableSetOf<Int>()
    private val settingChangedListeners = mutableListOf<(settingId: Int, oldValue: Int, newValue: Int) -> Unit>()

    // VarClient events
    private val watchedVarcs = mutableSetOf<Int>()
    private val varcChangedListeners = mutableListOf<(varcId: Int, oldValue: Int, newValue: Int) -> Unit>()

    // GE events
    private val geOfferChangedListeners = mutableListOf<(slotIndex: Int, oldState: GrandExchangeOfferState?, newState: GrandExchangeOfferState) -> Unit>()

    // Widget events (caller registers which group IDs to watch)
    private val watchedWidgets = mutableSetOf<Int>()
    private val widgetOpenedListeners = mutableListOf<(groupId: Int) -> Unit>()
    private val widgetClosedListeners = mutableListOf<(groupId: Int) -> Unit>()

    // Animation events (per-frame)
    private val animationChangedListeners = mutableListOf<(actor: Actor, oldAnim: Int, newAnim: Int) -> Unit>()

    // Interacting events (per-frame)
    private val interactingChangedListeners = mutableListOf<(source: Actor, oldTarget: Actor?, newTarget: Actor?) -> Unit>()

    // Health events (per-frame, proxy for hitsplats)
    private val healthChangedListeners = mutableListOf<(actor: Actor, oldRatio: Int, newRatio: Int) -> Unit>()

    // --- Previous state snapshots ---

    // Tick-rate snapshots
    private var prevXp = IntArray(Skill.entries.size)
    private var prevLevel = IntArray(Skill.entries.size)
    private var prevBoosted = IntArray(Skill.entries.size)
    private var prevInventory: List<InventoryItem> = emptyList()
    private var prevEquipment: List<EquippedItem> = emptyList()
    private var prevNpcIndices: Map<Int, NPC> = emptyMap()
    private var prevPlayerNames: Map<String, Player> = emptyMap()
    private var prevObjectKeys: Map<Long, TileObject> = emptyMap()
    private var prevGroundItemKeys: Map<Long, GroundItem> = emptyMap()
    private var prevVarbitValues: Map<Int, Int> = emptyMap()
    private var prevSettingValues: Map<Int, Int> = emptyMap()
    private var prevVarcValues: Map<Int, Int> = emptyMap()
    private var prevGeStates: Array<GrandExchangeOfferState?> = arrayOfNulls(8)
    private var prevWidgetVisible: Map<Int, Boolean> = emptyMap()

    // Frame-rate snapshots (tracked actors)
    private var prevAnimations: MutableMap<Actor, Int> = mutableMapOf()
    private var prevInteracting: MutableMap<Actor, Actor?> = mutableMapOf()
    private var prevHealthRatios: MutableMap<Actor, Int> = mutableMapOf()

    private var initialized = false

    // --- Registration methods ---

    fun onStatChanged(listener: (skill: Skill, oldXp: Int, newXp: Int, oldLevel: Int, newLevel: Int, oldBoosted: Int, newBoosted: Int) -> Unit) { statChangedListeners.add(listener) }
    fun onInventoryChanged(listener: (old: List<InventoryItem>, new: List<InventoryItem>) -> Unit) { inventoryChangedListeners.add(listener) }
    fun onEquipmentChanged(listener: (old: List<EquippedItem>, new: List<EquippedItem>) -> Unit) { equipmentChangedListeners.add(listener) }
    fun onNpcSpawned(listener: (NPC) -> Unit) { npcSpawnedListeners.add(listener) }
    fun onNpcDespawned(listener: (NPC) -> Unit) { npcDespawnedListeners.add(listener) }
    fun onPlayerSpawned(listener: (Player) -> Unit) { playerSpawnedListeners.add(listener) }
    fun onPlayerDespawned(listener: (Player) -> Unit) { playerDespawnedListeners.add(listener) }
    fun onObjectSpawned(listener: (TileObject) -> Unit) { objectSpawnedListeners.add(listener) }
    fun onObjectDespawned(listener: (TileObject) -> Unit) { objectDespawnedListeners.add(listener) }
    fun onGroundItemSpawned(listener: (GroundItem) -> Unit) { groundItemSpawnedListeners.add(listener) }
    fun onGroundItemDespawned(listener: (GroundItem) -> Unit) { groundItemDespawnedListeners.add(listener) }
    fun onVarbitChanged(listener: (varbitId: Int, oldValue: Int, newValue: Int) -> Unit) { varbitChangedListeners.add(listener) }

    /** Watches the given varbit and registers a change listener in one call. */
    fun onVarbitChanged(varbitId: Int, listener: (oldValue: Int, newValue: Int) -> Unit) {
        watchVarbit(varbitId)
        varbitChangedListeners.add { id, old, new -> if (id == varbitId) listener(old, new) }
    }

    /** Watches multiple varbits and registers a change listener in one call. */
    fun onVarbitChanged(vararg varbitIds: Int, listener: (varbitId: Int, oldValue: Int, newValue: Int) -> Unit) {
        watchVarbits(*varbitIds)
        varbitChangedListeners.add(listener)
    }
    fun onSettingChanged(listener: (settingId: Int, oldValue: Int, newValue: Int) -> Unit) { settingChangedListeners.add(listener) }

    /** Watches the given setting (varp) and registers a change listener in one call. */
    fun onSettingChanged(settingId: Int, listener: (oldValue: Int, newValue: Int) -> Unit) {
        watchSetting(settingId)
        settingChangedListeners.add { id, old, new -> if (id == settingId) listener(old, new) }
    }

    /** Watches multiple settings and registers a change listener in one call. */
    fun onSettingChanged(vararg settingIds: Int, listener: (settingId: Int, oldValue: Int, newValue: Int) -> Unit) {
        watchSettings(*settingIds)
        settingChangedListeners.add(listener)
    }

    fun onVarcChanged(listener: (varcId: Int, oldValue: Int, newValue: Int) -> Unit) { varcChangedListeners.add(listener) }

    /** Watches the given VarClient int and registers a change listener in one call. */
    fun onVarcChanged(varcId: Int, listener: (oldValue: Int, newValue: Int) -> Unit) {
        watchVarc(varcId)
        varcChangedListeners.add { id, old, new -> if (id == varcId) listener(old, new) }
    }

    /** Watches multiple VarClient ints and registers a change listener in one call. */
    fun onVarcChanged(vararg varcIds: Int, listener: (varcId: Int, oldValue: Int, newValue: Int) -> Unit) {
        watchVarcs(*varcIds)
        varcChangedListeners.add(listener)
    }

    fun onGrandExchangeOfferChanged(listener: (slotIndex: Int, oldState: GrandExchangeOfferState?, newState: GrandExchangeOfferState) -> Unit) { geOfferChangedListeners.add(listener) }
    fun onWidgetOpened(listener: (groupId: Int) -> Unit) { widgetOpenedListeners.add(listener) }
    fun onWidgetClosed(listener: (groupId: Int) -> Unit) { widgetClosedListeners.add(listener) }

    /** Watches the given widget group and registers an opened listener in one call. */
    fun onWidgetOpened(groupId: Int, listener: () -> Unit) {
        watchWidget(groupId)
        widgetOpenedListeners.add { id -> if (id == groupId) listener() }
    }

    /** Watches the given widget group and registers a closed listener in one call. */
    fun onWidgetClosed(groupId: Int, listener: () -> Unit) {
        watchWidget(groupId)
        widgetClosedListeners.add { id -> if (id == groupId) listener() }
    }
    fun onAnimationChanged(listener: (actor: Actor, oldAnim: Int, newAnim: Int) -> Unit) { animationChangedListeners.add(listener) }
    fun onInteractingChanged(listener: (source: Actor, oldTarget: Actor?, newTarget: Actor?) -> Unit) { interactingChangedListeners.add(listener) }
    fun onHealthChanged(listener: (actor: Actor, oldRatio: Int, newRatio: Int) -> Unit) { healthChangedListeners.add(listener) }

    /** Register a varbit ID to watch for changes. */
    fun watchVarbit(varbitId: Int) { watchedVarbits.add(varbitId) }
    fun watchVarbits(vararg varbitIds: Int) { watchedVarbits.addAll(varbitIds.toSet()) }

    /** Register a setting (varp) ID to watch for changes. */
    fun watchSetting(settingId: Int) { watchedSettings.add(settingId) }
    fun watchSettings(vararg settingIds: Int) { watchedSettings.addAll(settingIds.toSet()) }

    /** Register a VarClient int ID to watch for changes. */
    fun watchVarc(varcId: Int) { watchedVarcs.add(varcId) }
    fun watchVarcs(vararg varcIds: Int) { watchedVarcs.addAll(varcIds.toSet()) }

    /** Register a widget group ID to watch for open/close. */
    fun watchWidget(groupId: Int) { watchedWidgets.add(groupId) }
    fun watchWidgets(vararg groupIds: Int) { watchedWidgets.addAll(groupIds.toSet()) }

    // --- Lifecycle ---

    fun start() {
        snapshot()
        initialized = true

        tickRegistration = ctx.events.onGameTick {
            pollTickEvents()
        }

        // Only register frame polling if there are frame-rate listeners
        if (animationChangedListeners.isNotEmpty() ||
            interactingChangedListeners.isNotEmpty() ||
            healthChangedListeners.isNotEmpty()) {
            renderRegistration = ctx.events.onBeforeRender {
                pollFrameEvents()
            }
        }
    }

    fun stop() {
        tickRegistration?.remove()
        renderRegistration?.remove()
        tickRegistration = null
        renderRegistration = null
        initialized = false
    }

    // --- Snapshot (capture state without firing) ---

    private fun snapshot() {
        snapshotStats()
        prevInventory = ctx.inventory.getItems().toList()
        prevEquipment = ctx.equipment.getItems().toList()
        prevNpcIndices = ctx.worldViews.getTopLevelNpcs().associateBy { it.index }
        val localPlayer = ctx.worldViews.getLocalPlayer()
        prevPlayerNames = ctx.worldViews.getTopLevelPlayers()
            .filter { it != localPlayer && it.name != null }
            .associateBy { it.name!! }
        prevObjectKeys = ctx.worldViews.getTopLevelObjects().associateBy { objectKey(it) }
        prevGroundItemKeys = ctx.worldViews.getTopLevelGroundItems().associateBy { groundItemKey(it) }
        prevVarbitValues = watchedVarbits.associateWith { ctx.client.getVarbitValue(it) }
        prevSettingValues = watchedSettings.associateWith { ctx.client.getVarpValue(it) }
        prevVarcValues = watchedVarcs.associateWith { ctx.client.getVarcIntValue(it) }
        snapshotGe()
        snapshotWidgets()
        snapshotActorsForFrame()
    }

    private fun snapshotStats() {
        for (skill in Skill.entries) {
            val idx = skill.ordinal
            prevXp[idx] = ctx.skills.getXp(skill)
            prevLevel[idx] = ctx.skills.getLevel(skill)
            prevBoosted[idx] = ctx.skills.getBoostedLevel(skill)
        }
    }

    private fun snapshotGe() {
        val offers = ctx.client.grandExchangeOffers
        if (offers != null) {
            for (i in offers.indices) {
                prevGeStates[i] = offers[i].state
            }
        }
    }

    private fun snapshotWidgets() {
        prevWidgetVisible = watchedWidgets.associateWith { groupId ->
            val w = ctx.client.getWidget(groupId, 0)
            w != null && !w.isHidden
        }
    }

    private fun snapshotActorsForFrame() {
        val allActors = mutableListOf<Actor>()
        ctx.worldViews.getLocalPlayer()?.let { allActors.add(it) }
        allActors.addAll(ctx.worldViews.getTopLevelNpcs())
        allActors.addAll(ctx.worldViews.getTopLevelPlayers())

        prevAnimations.clear()
        prevInteracting.clear()
        prevHealthRatios.clear()
        for (actor in allActors) {
            prevAnimations[actor] = actor.animation
            prevInteracting[actor] = actor.interacting
            prevHealthRatios[actor] = actor.healthRatio
        }
    }

    // --- Tick polling ---

    private fun pollTickEvents() {
        if (!initialized) return

        pollStats()
        pollInventory()
        pollEquipment()
        pollNpcs()
        pollPlayers()
        pollObjects()
        pollGroundItems()
        pollVarbits()
        pollSettings()
        pollVarcs()
        pollGe()
        pollWidgets()
    }

    private fun pollStats() {
        if (statChangedListeners.isEmpty()) return
        for (skill in Skill.entries) {
            val idx = skill.ordinal
            val newXp = ctx.skills.getXp(skill)
            val newLevel = ctx.skills.getLevel(skill)
            val newBoosted = ctx.skills.getBoostedLevel(skill)
            if (newXp != prevXp[idx] || newLevel != prevLevel[idx] || newBoosted != prevBoosted[idx]) {
                val oldXp = prevXp[idx]; val oldLevel = prevLevel[idx]; val oldBoosted = prevBoosted[idx]
                prevXp[idx] = newXp; prevLevel[idx] = newLevel; prevBoosted[idx] = newBoosted
                statChangedListeners.forEach { it(skill, oldXp, newXp, oldLevel, newLevel, oldBoosted, newBoosted) }
            }
        }
    }

    private fun pollInventory() {
        if (inventoryChangedListeners.isEmpty()) return
        val current = ctx.inventory.getItems().toList()
        if (current != prevInventory) {
            val old = prevInventory
            prevInventory = current
            inventoryChangedListeners.forEach { it(old, current) }
        }
    }

    private fun pollEquipment() {
        if (equipmentChangedListeners.isEmpty()) return
        val current = ctx.equipment.getItems().toList()
        if (current != prevEquipment) {
            val old = prevEquipment
            prevEquipment = current
            equipmentChangedListeners.forEach { it(old, current) }
        }
    }

    private fun pollNpcs() {
        if (npcSpawnedListeners.isEmpty() && npcDespawnedListeners.isEmpty()) return
        val current = ctx.worldViews.getTopLevelNpcs().associateBy { it.index }

        if (npcSpawnedListeners.isNotEmpty()) {
            for ((index, npc) in current) {
                if (index !in prevNpcIndices) {
                    npcSpawnedListeners.forEach { it(npc) }
                }
            }
        }

        if (npcDespawnedListeners.isNotEmpty()) {
            for ((index, npc) in prevNpcIndices) {
                if (index !in current) {
                    npcDespawnedListeners.forEach { it(npc) }
                }
            }
        }

        prevNpcIndices = current
    }

    private fun pollPlayers() {
        if (playerSpawnedListeners.isEmpty() && playerDespawnedListeners.isEmpty()) return
        val localPlayer = ctx.worldViews.getLocalPlayer()
        val current = ctx.worldViews.getTopLevelPlayers()
            .filter { it != localPlayer && it.name != null }
            .associateBy { it.name!! }

        if (playerSpawnedListeners.isNotEmpty()) {
            for ((name, player) in current) {
                if (name !in prevPlayerNames) {
                    playerSpawnedListeners.forEach { it(player) }
                }
            }
        }

        if (playerDespawnedListeners.isNotEmpty()) {
            for ((name, player) in prevPlayerNames) {
                if (name !in current) {
                    playerDespawnedListeners.forEach { it(player) }
                }
            }
        }

        prevPlayerNames = current
    }

    private fun pollObjects() {
        if (objectSpawnedListeners.isEmpty() && objectDespawnedListeners.isEmpty()) return
        val current = ctx.worldViews.getTopLevelObjects().associateBy { objectKey(it) }

        if (objectSpawnedListeners.isNotEmpty()) {
            for ((key, obj) in current) {
                if (key !in prevObjectKeys) {
                    objectSpawnedListeners.forEach { it(obj) }
                }
            }
        }

        if (objectDespawnedListeners.isNotEmpty()) {
            for ((key, obj) in prevObjectKeys) {
                if (key !in current) {
                    objectDespawnedListeners.forEach { it(obj) }
                }
            }
        }

        prevObjectKeys = current
    }

    private fun pollGroundItems() {
        if (groundItemSpawnedListeners.isEmpty() && groundItemDespawnedListeners.isEmpty()) return
        val current = ctx.worldViews.getTopLevelGroundItems().associateBy { groundItemKey(it) }

        if (groundItemSpawnedListeners.isNotEmpty()) {
            for ((key, item) in current) {
                if (key !in prevGroundItemKeys) {
                    groundItemSpawnedListeners.forEach { it(item) }
                }
            }
        }

        if (groundItemDespawnedListeners.isNotEmpty()) {
            for ((key, item) in prevGroundItemKeys) {
                if (key !in current) {
                    groundItemDespawnedListeners.forEach { it(item) }
                }
            }
        }

        prevGroundItemKeys = current
    }

    private fun pollVarbits() {
        if (varbitChangedListeners.isEmpty() || watchedVarbits.isEmpty()) return
        for (varbitId in watchedVarbits) {
            val newValue = ctx.client.getVarbitValue(varbitId)
            val oldValue = prevVarbitValues[varbitId] ?: 0
            if (newValue != oldValue) {
                prevVarbitValues = prevVarbitValues + (varbitId to newValue)
                varbitChangedListeners.forEach { it(varbitId, oldValue, newValue) }
            }
        }
    }

    private fun pollSettings() {
        if (settingChangedListeners.isEmpty() || watchedSettings.isEmpty()) return
        for (settingId in watchedSettings) {
            val newValue = ctx.client.getVarpValue(settingId)
            val oldValue = prevSettingValues[settingId] ?: 0
            if (newValue != oldValue) {
                prevSettingValues = prevSettingValues + (settingId to newValue)
                settingChangedListeners.forEach { it(settingId, oldValue, newValue) }
            }
        }
    }

    private fun pollVarcs() {
        if (varcChangedListeners.isEmpty() || watchedVarcs.isEmpty()) return
        for (varcId in watchedVarcs) {
            val newValue = ctx.client.getVarcIntValue(varcId)
            val oldValue = prevVarcValues[varcId] ?: 0
            if (newValue != oldValue) {
                prevVarcValues = prevVarcValues + (varcId to newValue)
                varcChangedListeners.forEach { it(varcId, oldValue, newValue) }
            }
        }
    }

    private fun pollGe() {
        if (geOfferChangedListeners.isEmpty()) return
        val offers = ctx.client.grandExchangeOffers ?: return
        for (i in offers.indices) {
            val newState = offers[i].state
            val oldState = prevGeStates[i]
            if (newState != oldState) {
                prevGeStates[i] = newState
                geOfferChangedListeners.forEach { it(i, oldState, newState) }
            }
        }
    }

    private fun pollWidgets() {
        if ((widgetOpenedListeners.isEmpty() && widgetClosedListeners.isEmpty()) || watchedWidgets.isEmpty()) return
        for (groupId in watchedWidgets) {
            val widget = ctx.client.getWidget(groupId, 0)
            val nowVisible = widget != null && !widget.isHidden
            val wasVisible = prevWidgetVisible[groupId] ?: false

            if (nowVisible && !wasVisible) {
                prevWidgetVisible = prevWidgetVisible + (groupId to true)
                widgetOpenedListeners.forEach { it(groupId) }
            } else if (!nowVisible && wasVisible) {
                prevWidgetVisible = prevWidgetVisible + (groupId to false)
                widgetClosedListeners.forEach { it(groupId) }
            }
        }
    }

    // --- Frame polling ---

    private fun pollFrameEvents() {
        if (!initialized) return

        val allActors = mutableListOf<Actor>()
        ctx.worldViews.getLocalPlayer()?.let { allActors.add(it) }
        allActors.addAll(ctx.worldViews.getTopLevelNpcs())
        allActors.addAll(ctx.worldViews.getTopLevelPlayers())

        for (actor in allActors) {
            // Animation
            if (animationChangedListeners.isNotEmpty()) {
                val newAnim = actor.animation
                val oldAnim = prevAnimations[actor] ?: -1
                if (newAnim != oldAnim) {
                    prevAnimations[actor] = newAnim
                    animationChangedListeners.forEach { it(actor, oldAnim, newAnim) }
                }
            }

            // Interacting
            if (interactingChangedListeners.isNotEmpty()) {
                val newTarget = actor.interacting
                val oldTarget = prevInteracting[actor]
                if (newTarget != oldTarget) {
                    prevInteracting[actor] = newTarget
                    interactingChangedListeners.forEach { it(actor, oldTarget, newTarget) }
                }
            }

            // Health
            if (healthChangedListeners.isNotEmpty()) {
                val newRatio = actor.healthRatio
                val oldRatio = prevHealthRatios[actor] ?: -1
                if (newRatio != oldRatio) {
                    prevHealthRatios[actor] = newRatio
                    healthChangedListeners.forEach { it(actor, oldRatio, newRatio) }
                }
            }
        }

        // Clean up actors that no longer exist
        val actorSet = allActors.toSet()
        prevAnimations.keys.retainAll(actorSet)
        prevInteracting.keys.retainAll(actorSet)
        prevHealthRatios.keys.retainAll(actorSet)
    }

    // --- Composite key helpers ---

    private fun objectKey(obj: TileObject): Long {
        val loc = obj.worldLocation
        return (obj.id.toLong() shl 32) or (loc.x.toLong() shl 16) or (loc.y.toLong() shl 2) or loc.plane.toLong()
    }

    private fun groundItemKey(item: GroundItem): Long {
        val pos = item.position
        return (item.id.toLong() shl 32) or (pos.x.toLong() shl 16) or (pos.y.toLong() shl 2) or pos.plane.toLong()
    }
}

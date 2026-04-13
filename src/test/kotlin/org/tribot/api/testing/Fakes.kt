package org.tribot.api.testing

import io.mockk.every
import io.mockk.mockk
import net.runelite.api.*
import net.runelite.api.coords.WorldPoint
import net.runelite.api.widgets.Widget
import org.tribot.automation.script.ScriptContext
import org.tribot.automation.script.core.definition.ItemDefinition
import org.tribot.automation.script.core.definition.NpcDefinition
import org.tribot.automation.script.core.definition.ObjectDefinition

fun fakeContext(block: ScriptContext.() -> Unit = {}): ScriptContext {
    val ctx = mockk<ScriptContext>(relaxed = true)
    ctx.block()
    return ctx
}

fun fakeNpc(
    id: Int = 1,
    name: String = "Npc",
    worldLocation: WorldPoint = WorldPoint(3200, 3200, 0),
    combatLevel: Int = 1,
    animation: Int = -1,
    interacting: Actor? = null,
    healthRatio: Int = -1,
    healthScale: Int = -1
): NPC {
    val npc = mockk<NPC>(relaxed = true)
    every { npc.id } returns id
    every { npc.name } returns name
    every { npc.worldLocation } returns worldLocation
    every { npc.combatLevel } returns combatLevel
    every { npc.animation } returns animation
    every { npc.interacting } returns interacting
    every { npc.healthRatio } returns healthRatio
    every { npc.healthScale } returns healthScale
    return npc
}

fun fakePlayer(
    name: String = "Player",
    worldLocation: WorldPoint = WorldPoint(3200, 3200, 0),
    combatLevel: Int = 70,
    animation: Int = -1
): Player {
    val player = mockk<Player>(relaxed = true)
    every { player.name } returns name
    every { player.worldLocation } returns worldLocation
    every { player.combatLevel } returns combatLevel
    every { player.animation } returns animation
    return player
}

fun fakeNpcDef(
    id: Int = 1,
    name: String = "Npc",
    combatLevel: Int = 1,
    actions: List<String?> = listOf("Attack", null, null, null, null)
): NpcDefinition {
    val def = mockk<NpcDefinition>(relaxed = true)
    every { def.id } returns id
    every { def.name } returns name
    every { def.combatLevel } returns combatLevel
    every { def.actions } returns actions
    return def
}

fun fakeObjectDef(
    id: Int = 1,
    name: String = "Object",
    actions: List<String?> = listOf("Use", null, null, null, null)
): ObjectDefinition {
    val def = mockk<ObjectDefinition>(relaxed = true)
    every { def.id } returns id
    every { def.name } returns name
    every { def.actions } returns actions
    return def
}

fun fakeItemDef(
    id: Int = 1,
    name: String = "Item",
    stackable: Boolean = false,
    tradable: Boolean = true,
    members: Boolean = false,
    noted: Boolean = false,
    inventoryActions: List<String?> = listOf("Use", null, null, "Drop", null)
): ItemDefinition {
    val def = mockk<ItemDefinition>(relaxed = true)
    every { def.id } returns id
    every { def.name } returns name
    every { def.stackable } returns stackable
    every { def.tradable } returns tradable
    every { def.members } returns members
    every { def.inventoryActions } returns inventoryActions
    every { def.isNoted() } returns noted
    return def
}

fun fakeWidget(
    text: String? = null,
    actions: Array<String?>? = null,
    spriteId: Int = -1,
    itemId: Int = -1,
    type: Int = 0,
    isHidden: Boolean = false,
    isSelfHidden: Boolean = false,
    index: Int = 0,
    dynamicChildren: Array<Widget>? = null,
    children: Array<Widget>? = null,
    nestedChildren: Array<Widget>? = null
): Widget {
    val widget = mockk<Widget>(relaxed = true)
    every { widget.text } returns text
    every { widget.actions } returns actions
    every { widget.spriteId } returns spriteId
    every { widget.itemId } returns itemId
    every { widget.type } returns type
    every { widget.isHidden } returns isHidden
    every { widget.isSelfHidden } returns isSelfHidden
    every { widget.index } returns index
    every { widget.dynamicChildren } returns dynamicChildren
    every { widget.children } returns children
    every { widget.nestedChildren } returns nestedChildren
    return widget
}

package org.tribot.api.requirements

import org.tribot.api.ApiContext

/**
 * Requires a widget to be visible (or hidden).
 */
class WidgetRequirement(
    val groupId: Int,
    val childId: Int,
    val visible: Boolean = true,
    private val name: String = "Widget $groupId:$childId"
) : Requirement {

    override fun check(): Boolean {
        val widget = ApiContext.get().client.getWidget(groupId, childId) ?: return !visible
        return widget.isHidden != visible
    }

    override val displayText: String get() = "$name ${if (visible) "visible" else "hidden"}"
}

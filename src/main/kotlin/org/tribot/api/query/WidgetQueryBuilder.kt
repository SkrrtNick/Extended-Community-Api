package org.tribot.api.query

import net.runelite.api.widgets.Widget
import org.tribot.api.ApiContext

/**
 * Fluent query builder for searching RuneLite widgets within a specified group.
 *
 * Widgets are organized hierarchically: root widgets belong to a group,
 * and each can have dynamic, static, and nested children.
 *
 * Usage:
 * ```
 * val results = WidgetQueryBuilder()
 *     .group(219)
 *     .textContains("continue")
 *     .visible()
 *     .results()
 * ```
 */
class WidgetQueryBuilder : QueryBuilder<Widget, WidgetQueryBuilder>() {

    private var groupId: Int? = null
    private var searchChildren = true
    private var searchGrandchildren = false

    /** Required: set the widget group ID to search within. */
    fun group(groupId: Int): WidgetQueryBuilder {
        this.groupId = groupId
        return this
    }

    /** Also search grandchildren (children of children), one level deeper. */
    fun includeGrandchildren(): WidgetQueryBuilder {
        searchGrandchildren = true
        return this
    }

    /** Filter to widgets whose text exactly matches one of the given values. */
    fun texts(vararg texts: String): WidgetQueryBuilder = filter { widget ->
        val text = widget.text ?: return@filter false
        texts.any { text == it }
    }

    /** Filter to widgets whose text contains one of the given substrings (case-insensitive). */
    fun textContains(vararg texts: String): WidgetQueryBuilder = filter { widget ->
        val text = widget.text ?: return@filter false
        texts.any { text.contains(it, ignoreCase = true) }
    }

    /** Filter to widgets that have at least one action exactly matching one of the given values. */
    fun actions(vararg actions: String): WidgetQueryBuilder = filter { widget ->
        val widgetActions = widget.actions ?: return@filter false
        widgetActions.filterNotNull().any { it in actions }
    }

    /** Filter to widgets that have an action containing one of the given substrings (case-insensitive). */
    fun actionContains(vararg actions: String): WidgetQueryBuilder = filter { widget ->
        val widgetActions = widget.actions ?: return@filter false
        widgetActions.filterNotNull().any { a -> actions.any { a.contains(it, ignoreCase = true) } }
    }

    /** Filter to widgets whose sprite ID matches one of the given IDs. */
    fun spriteIds(vararg ids: Int): WidgetQueryBuilder = filter { widget ->
        val idSet = ids.toSet()
        widget.spriteId in idSet
    }

    /** Filter to widgets whose item ID matches one of the given IDs. */
    fun itemIds(vararg ids: Int): WidgetQueryBuilder = filter { widget ->
        val idSet = ids.toSet()
        widget.itemId in idSet
    }

    /** Filter to widgets whose type matches one of the given types. */
    fun types(vararg types: Int): WidgetQueryBuilder = filter { widget ->
        val typeSet = types.toSet()
        widget.type in typeSet
    }

    /** Filter to widgets that are visible (not hidden and not self-hidden). */
    fun visible(): WidgetQueryBuilder = filter { widget ->
        !widget.isHidden && !widget.isSelfHidden
    }

    /** Filter to widgets that are hidden (either hidden or self-hidden). */
    fun hidden(): WidgetQueryBuilder = filter { widget ->
        widget.isHidden || widget.isSelfHidden
    }

    /** Filter to widgets that have non-empty text. */
    fun hasText(): WidgetQueryBuilder = filter { widget ->
        !widget.text.isNullOrEmpty()
    }

    /** Filter to widgets whose index matches one of the given indices. */
    fun childIndex(vararg indices: Int): WidgetQueryBuilder = filter { widget ->
        val indexSet = indices.toSet()
        widget.index in indexSet
    }

    override fun fetchEntities(): List<Widget> {
        val gid = groupId ?: return emptyList()
        val result = mutableListOf<Widget>()

        // Iterate through root-level children of the group
        var index = 0
        while (true) {
            val widget = ApiContext.get().client.getWidget(gid, index) ?: break
            result.add(widget)

            if (searchChildren) {
                collectChildren(widget, result)

                if (searchGrandchildren) {
                    val children = allDirectChildren(widget)
                    for (child in children) {
                        collectChildren(child, result)
                    }
                }
            }
            index++
        }

        return result
    }

    override fun results(): QueryResults<Widget> = QueryResults(applyFilters(fetchEntities()))

    /** Collects all direct children (dynamic, static, nested) of a widget into the result list. */
    private fun collectChildren(widget: Widget, result: MutableList<Widget>) {
        widget.dynamicChildren?.let { result.addAll(it) }
        widget.children?.let { result.addAll(it) }
        widget.nestedChildren?.let { result.addAll(it) }
    }

    /** Returns all direct children (dynamic, static, nested) of a widget as a single list. */
    private fun allDirectChildren(widget: Widget): List<Widget> {
        val children = mutableListOf<Widget>()
        widget.dynamicChildren?.let { children.addAll(it) }
        widget.children?.let { children.addAll(it) }
        widget.nestedChildren?.let { children.addAll(it) }
        return children
    }
}

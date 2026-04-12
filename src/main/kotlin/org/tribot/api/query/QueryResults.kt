package org.tribot.api.query

/**
 * Wraps a list of query results with null-safe accessors and transformation methods.
 */
open class QueryResults<T>(private val items: List<T>) : Iterable<T> {

    val size: Int get() = items.size

    val isEmpty: Boolean get() = items.isEmpty()

    fun first(): T? = items.firstOrNull()

    fun last(): T? = items.lastOrNull()

    fun random(): T? = if (items.isEmpty()) null else items.random()

    fun get(index: Int): T? = items.getOrNull(index)

    fun limit(count: Int): QueryResults<T> = QueryResults(items.take(count))

    fun sort(comparator: Comparator<T>): QueryResults<T> =
        QueryResults(items.sortedWith(comparator))

    fun asList(): List<T> = items

    override fun iterator(): Iterator<T> = items.iterator()
}

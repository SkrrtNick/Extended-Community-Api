package org.tribot.api.query

/**
 * Abstract base for all entity query builders.
 * Provides a fluent API for adding filters and executing queries.
 *
 * @param T the entity type being queried
 * @param B the concrete builder type (for fluent return types)
 */
abstract class QueryBuilder<T, B : QueryBuilder<T, B>> {

    protected val filters: MutableList<(T) -> Boolean> = mutableListOf()

    /**
     * Adds a custom filter predicate. Only entities matching all filters are returned.
     */
    @Suppress("UNCHECKED_CAST")
    fun filter(predicate: (T) -> Boolean): B {
        filters.add(predicate)
        return this as B
    }

    /**
     * Executes the query: fetches entities, applies all filters, and wraps in results.
     */
    abstract fun results(): QueryResults<T>

    /**
     * Fetches the raw list of entities from the game state.
     */
    protected abstract fun fetchEntities(): List<T>

    /**
     * Applies all registered filters to the given entity list.
     */
    protected fun applyFilters(entities: List<T>): List<T> =
        entities.filter { entity -> filters.all { filter -> filter(entity) } }
}

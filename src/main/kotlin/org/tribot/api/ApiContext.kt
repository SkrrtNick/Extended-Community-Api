package org.tribot.api

import org.tribot.automation.script.ScriptContext

/**
 * Global accessor for the current [ScriptContext].
 *
 * Call [init] once at script startup (e.g. in your `onStart` override) to make the
 * context available throughout the Community API without passing it as a parameter.
 *
 * ```
 * override fun onStart(ctx: ScriptContext) {
 *     ApiContext.init(ctx)
 * }
 * ```
 */
object ApiContext {

    private var instance: ScriptContext? = null

    /**
     * Stores the [ScriptContext] for use by the entire API surface.
     * Must be called before any API methods that depend on the context.
     */
    fun init(ctx: ScriptContext) {
        instance = ctx
    }

    /**
     * Returns the current [ScriptContext].
     * @throws IllegalStateException if [init] has not been called yet.
     */
    fun get(): ScriptContext =
        instance ?: error("ApiContext not initialized — call ApiContext.init(ctx) in your script's onStart")

    /**
     * Clears the stored context. Useful in tests or when a script stops.
     */
    fun reset() {
        instance = null
    }
}

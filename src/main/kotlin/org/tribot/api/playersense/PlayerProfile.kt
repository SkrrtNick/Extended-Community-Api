package org.tribot.api.playersense

import java.util.Random

class PlayerProfile private constructor(
    private val values: Map<ProfileProperty, Double>,
    private val overrides: Map<ProfileProperty, Double>
) {

    operator fun get(property: ProfileProperty): Double =
        overrides[property] ?: values[property]
            ?: ((property.min + property.max) / 2.0)

    fun withOverride(property: ProfileProperty, value: Double): PlayerProfile =
        PlayerProfile(values, overrides + (property to value.coerceIn(property.min, property.max)))

    fun clearOverride(property: ProfileProperty): PlayerProfile =
        PlayerProfile(values, overrides - property)

    fun generate(property: ProfileProperty, rangeLow: Long, rangeHigh: Long): Long {
        val bias = this[property]
        val normalized = (bias - property.min) / (property.max - property.min)
        val range = rangeHigh - rangeLow
        val center = rangeLow + (range * normalized)
        val stdDev = range * 0.15
        val value = center + (threadLocalRandom.get().nextGaussian() * stdDev)
        return value.toLong().coerceIn(rangeLow, rangeHigh)
    }

    companion object {
        private val threadLocalRandom = ThreadLocal.withInitial { Random() }

        fun fromSeed(seed: String): PlayerProfile {
            val rng = Random(seed.hashCode().toLong())
            val values = ProfileProperty.entries.associateWith { prop ->
                prop.min + (rng.nextDouble() * (prop.max - prop.min))
            }
            return PlayerProfile(values, emptyMap())
        }

        fun default(): PlayerProfile {
            val values = ProfileProperty.entries.associateWith { prop ->
                (prop.min + prop.max) / 2.0
            }
            return PlayerProfile(values, emptyMap())
        }
    }
}

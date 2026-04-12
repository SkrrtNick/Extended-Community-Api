package org.tribot.api.playersense

enum class ProfileProperty(
    val min: Double,
    val max: Double,
    val description: String
) {
    REACTION_TIME(0.0, 1.0, "Reaction speed to game events"),
    MOUSE_SPEED(0.0, 1.0, "Mouse movement speed preference"),
    IDLE_DURATION(0.0, 1.0, "Duration of idle pauses between actions"),
    EXTRA_ACTION_FREQUENCY(0.0, 1.0, "Frequency of unnecessary extra actions"),
    MISCLICK_RATE(0.0, 0.1, "Probability of misclicking"),
    ACTION_VARIANCE(0.0, 1.0, "Variance in timing between repeated actions"),
    CAMERA_STYLE(0.0, 1.0, "Camera control preference: keyboard vs mouse"),
    TAB_CHECK_FREQUENCY(0.0, 1.0, "Frequency of checking game tabs"),
    FATIGUE_RATE(0.0, 1.0, "Rate at which behavior degrades over time"),
    INTERACTION_DISTANCE(0.0, 1.0, "Preferred distance for interacting with entities")
}

package com.codebaron.spinwheel.widget.domain.model

data class WidgetConfig(
    val id: String,
    val name: String,
    val type: String,
    val networkAttributes: NetworkAttributes,
    val assetsHost: String,
    val wheelRotation: WheelRotation,
    val wheelAssets: WheelAssets
)

data class NetworkAttributes(
    val refreshIntervalSeconds: Int,
    val networkTimeoutMs: Int,
    val retryAttempts: Int,
    val cacheExpirationSeconds: Int,
    val debugMode: Boolean
)

data class WheelRotation(
    val durationMs: Int,
    val minimumSpins: Int,
    val maximumSpins: Int,
    val spinEasing: EasingType
)

data class WheelAssets(
    val background: String,
    val wheelFrame: String,
    val wheelSpin: String,
    val wheel: String
)

enum class EasingType {
    EASE_IN_OUT_CUBIC,
    LINEAR,
    EASE_IN,
    EASE_OUT;

    companion object {
        fun fromString(value: String): EasingType = when (value.lowercase()) {
            "easeinoutcubic" -> EASE_IN_OUT_CUBIC
            "linear" -> LINEAR
            "easein" -> EASE_IN
            "easeout" -> EASE_OUT
            else -> EASE_IN_OUT_CUBIC
        }
    }
}

data class SpinResult(
    val finalRotation: Float,
    val totalSpins: Int,
    val segment: Int
)

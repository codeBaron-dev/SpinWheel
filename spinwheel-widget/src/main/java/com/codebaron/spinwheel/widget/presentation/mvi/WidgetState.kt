package com.codebaron.spinwheel.widget.presentation.mvi

import com.codebaron.spinwheel.widget.domain.model.WheelBitmaps
import com.codebaron.spinwheel.widget.domain.model.WidgetConfig

data class WidgetState(
    val isLoading: Boolean = true,
    val isSpinning: Boolean = false,
    val config: WidgetConfig? = null,
    val wheelBitmaps: WheelBitmaps? = null,
    val currentRotation: Float = 0f,
    val targetRotation: Float = 0f,
    val error: WidgetError? = null,
    val lastUpdateTime: Long = 0L
) {
    val isReady: Boolean
        get() = !isLoading && config != null && wheelBitmaps != null && error == null

    val canSpin: Boolean
        get() = isReady && !isSpinning
}

sealed class WidgetError {
    data class NetworkError(val errorMessage: String) : WidgetError()
    data class ConfigError(val errorMessage: String) : WidgetError()
    data class ImageLoadError(val errorMessage: String) : WidgetError()
    data object Unknown : WidgetError()

    fun getMessage(): String = when (this) {
        is NetworkError -> errorMessage
        is ConfigError -> errorMessage
        is ImageLoadError -> errorMessage
        is Unknown -> "An unknown error occurred"
    }
}

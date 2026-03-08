package com.codebaron.spinwheel.widget.presentation.mvi

import com.codebaron.spinwheel.widget.domain.model.WheelBitmaps
import com.codebaron.spinwheel.widget.domain.model.WidgetConfig
import kotlinx.serialization.SerializationException
import java.io.IOException

/**
 * Reducer responsible for managing the state transitions of the SpinWheel widget.
 *
 * This class follows the MVI (Model-View-Intent) pattern by taking the current [WidgetState]
 * and an action (either a [WidgetIntent] or external data updates) to produce a new
 * immutable [WidgetState].
 *
 * It handles the logic for:
 * - UI lifecycle events (Initialization, Refreshing)
 * - Spin wheel mechanics (Spinning, Completion, Rotation tracking)
 * - Configuration and bitmap asset updates
 * - Error mapping from [Throwable] to domain-specific [WidgetError]
 */
class WidgetReducer {

    fun reduce(currentState: WidgetState, intent: WidgetIntent): WidgetState {
        return when (intent) {
            is WidgetIntent.Initialize -> currentState.copy(
                isLoading = true,
                error = null
            )

            is WidgetIntent.Refresh -> currentState.copy(
                isLoading = true,
                error = null
            )

            is WidgetIntent.SpinWheel -> {
                if (currentState.canSpin) {
                    currentState.copy(isSpinning = true)
                } else {
                    currentState
                }
            }

            is WidgetIntent.SpinComplete -> currentState.copy(
                isSpinning = false,
                currentRotation = currentState.targetRotation % 360f
            )

            is WidgetIntent.SetRotation -> currentState.copy(
                currentRotation = intent.rotation
            )

            is WidgetIntent.HandleError -> currentState.copy(
                isLoading = false,
                error = mapError(intent.error)
            )

            is WidgetIntent.UpdateConfig -> currentState.copy(
                isLoading = true
            )
        }
    }

    fun reduceWithConfig(currentState: WidgetState, config: WidgetConfig): WidgetState {
        return currentState.copy(
            config = config,
            isLoading = currentState.wheelBitmaps == null,
            error = null
        )
    }

    fun reduceWithBitmaps(currentState: WidgetState, bitmaps: WheelBitmaps): WidgetState {
        return currentState.copy(
            wheelBitmaps = bitmaps,
            isLoading = false,
            error = null,
            lastUpdateTime = System.currentTimeMillis()
        )
    }

    fun reduceWithTargetRotation(currentState: WidgetState, targetRotation: Float): WidgetState {
        return currentState.copy(
            targetRotation = targetRotation
        )
    }

    private fun mapError(throwable: Throwable): WidgetError {
        return when (throwable) {
            is IOException -> WidgetError.NetworkError(
                throwable.message ?: "Network error occurred"
            )
            is SerializationException -> WidgetError.ConfigError(
                "Invalid configuration format"
            )
            is IllegalStateException -> WidgetError.ImageLoadError(
                throwable.message ?: "Failed to load images"
            )
            else -> WidgetError.Unknown
        }
    }
}

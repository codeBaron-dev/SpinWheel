package com.codebaron.spinwheel.widget.presentation.mvi

import com.codebaron.spinwheel.widget.domain.model.EasingType
import com.codebaron.spinwheel.widget.domain.model.SpinResult

sealed class WidgetSideEffect {
    data class TriggerSpinAnimation(
        val fromDegrees: Float,
        val toDegrees: Float,
        val durationMs: Int,
        val easing: EasingType
    ) : WidgetSideEffect()

    data class UpdateWidgetUI(val widgetIds: IntArray) : WidgetSideEffect() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as UpdateWidgetUI
            return widgetIds.contentEquals(other.widgetIds)
        }

        override fun hashCode(): Int = widgetIds.contentHashCode()
    }

    data class ShowError(val error: WidgetError) : WidgetSideEffect()

    data class SpinCompleted(val result: SpinResult) : WidgetSideEffect()

    object ScheduleNextRefresh : WidgetSideEffect()

    object SaveState : WidgetSideEffect()
}

package com.codebaron.spinwheel.widget.presentation.mvi

sealed class WidgetIntent {
    object Initialize : WidgetIntent()
    object Refresh : WidgetIntent()
    object SpinWheel : WidgetIntent()
    object SpinComplete : WidgetIntent()
    data class UpdateConfig(val configUrl: String) : WidgetIntent()
    data class SetRotation(val rotation: Float) : WidgetIntent()
    data class HandleError(val error: Throwable) : WidgetIntent()
}

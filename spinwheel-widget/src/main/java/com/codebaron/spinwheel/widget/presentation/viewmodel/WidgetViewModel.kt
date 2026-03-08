package com.codebaron.spinwheel.widget.presentation.viewmodel

import com.codebaron.spinwheel.widget.domain.model.SpinResult
import com.codebaron.spinwheel.widget.domain.repository.WidgetConfigRepository
import com.codebaron.spinwheel.widget.domain.usecase.CalculateSpinResultUseCase
import com.codebaron.spinwheel.widget.domain.usecase.FetchWidgetConfigUseCase
import com.codebaron.spinwheel.widget.domain.usecase.GetCachedConfigUseCase
import com.codebaron.spinwheel.widget.domain.usecase.GetDefaultConfigUseCase
import com.codebaron.spinwheel.widget.domain.usecase.LoadWheelImagesUseCase
import com.codebaron.spinwheel.widget.presentation.mvi.WidgetError
import com.codebaron.spinwheel.widget.presentation.mvi.WidgetIntent
import com.codebaron.spinwheel.widget.presentation.mvi.WidgetReducer
import com.codebaron.spinwheel.widget.presentation.mvi.WidgetSideEffect
import com.codebaron.spinwheel.widget.presentation.mvi.WidgetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WidgetViewModel(
    private val fetchConfigUseCase: FetchWidgetConfigUseCase,
    private val getCachedConfigUseCase: GetCachedConfigUseCase,
    private val getDefaultConfigUseCase: GetDefaultConfigUseCase,
    private val loadWheelImagesUseCase: LoadWheelImagesUseCase,
    private val calculateSpinResultUseCase: CalculateSpinResultUseCase,
    private val repository: WidgetConfigRepository,
    private val reducer: WidgetReducer = WidgetReducer()
) {
    private val _state = MutableStateFlow(WidgetState())
    val state: StateFlow<WidgetState> = _state.asStateFlow()

    private val _sideEffects = MutableSharedFlow<WidgetSideEffect>()
    val sideEffects: SharedFlow<WidgetSideEffect> = _sideEffects.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        // Restore rotation state
        val savedRotation = repository.getRotationState()
        _state.value = _state.value.copy(currentRotation = savedRotation)
    }

    fun processIntent(intent: WidgetIntent) {
        val newState = reducer.reduce(_state.value, intent)
        _state.value = newState

        scope.launch {
            handleSideEffects(intent)
        }
    }

    private suspend fun handleSideEffects(intent: WidgetIntent) {
        when (intent) {
            is WidgetIntent.Initialize -> initializeWidget()
            is WidgetIntent.Refresh -> refreshConfig()
            is WidgetIntent.SpinWheel -> performSpin()
            is WidgetIntent.SpinComplete -> onSpinComplete()
            is WidgetIntent.UpdateConfig -> refreshConfig(intent.configUrl)
            is WidgetIntent.SetRotation -> {
                repository.saveRotationState(intent.rotation)
            }
            else -> Unit
        }
    }

    /**
     * Initializes the widget by attempting to load the configuration.
     *
     * It first attempts to retrieve a cached configuration. If no cache is available,
     * it falls back to the default bundled configuration. Upon successfully retrieving
     * a configuration, it updates the state via the reducer and triggers the loading
     * of wheel assets. In case of a total failure, it updates the state with a
     * [WidgetError.ConfigError] and emits an error side effect.
     */
    private suspend fun initializeWidget() {
        // Try cached config first
        val cachedConfig = getCachedConfigUseCase()
        if (cachedConfig != null) {
            _state.value = reducer.reduceWithConfig(_state.value, cachedConfig)
            loadImages(cachedConfig.assetsHost, cachedConfig.wheelAssets)
            return
        }

        // Fall back to default bundled config
        try {
            val defaultConfig = getDefaultConfigUseCase()
            _state.value = reducer.reduceWithConfig(_state.value, defaultConfig)
            loadImages(defaultConfig.assetsHost, defaultConfig.wheelAssets)
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = WidgetError.ConfigError("Failed to load configuration")
            )
            _sideEffects.emit(WidgetSideEffect.ShowError(_state.value.error!!))
        }
    }

    /**
     * Refreshes the widget configuration from a remote source or falls back to local storage.
     *
     * This function attempts to fetch a new configuration if a [configUrl] is provided.
     * - If [configUrl] is null or empty, it triggers [initializeWidget] to use cached or default settings.
     * - On success, it updates the state, loads the required assets, and schedules the next refresh.
     * - On failure, it attempts a graceful degradation by falling back to the cached configuration
     *   or the default bundled configuration.
     *
     * @param configUrl The optional URL to fetch the new configuration from.
     */
    private suspend fun refreshConfig(configUrl: String? = null) {
        // If no URL provided, use cached or default config
        if (configUrl.isNullOrEmpty()) {
            initializeWidget()
            return
        }

        fetchConfigUseCase(configUrl)
            .onSuccess { config ->
                _state.value = reducer.reduceWithConfig(_state.value, config)
                loadImages(config.assetsHost, config.wheelAssets)
                _sideEffects.emit(WidgetSideEffect.ScheduleNextRefresh)
            }
            .onFailure { error ->
                // Try to use cached/default config on failure
                val fallbackConfig = getCachedConfigUseCase() ?: getDefaultConfigUseCase()
                _state.value = reducer.reduceWithConfig(_state.value, fallbackConfig)
                loadImages(fallbackConfig.assetsHost, fallbackConfig.wheelAssets)
            }
    }

    /**
     * Asynchronously loads the image assets required for the spin wheel.
     *
     * This function uses the [loadWheelImagesUseCase] to fetch bitmaps from the provided [assetsHost].
     * On success, it updates the view state with the loaded bitmaps using the reducer.
     * On failure, it updates the state with an [WidgetError.ImageLoadError] and emits
     * a side effect to notify the UI.
     *
     * @param assetsHost The base URL or host path where the assets are stored.
     * @param assets The [com.codebaron.spinwheel.widget.domain.model.WheelAssets] object containing the specific image paths.
     */
    private suspend fun loadImages(
        assetsHost: String,
        assets: com.codebaron.spinwheel.widget.domain.model.WheelAssets
    ) {
        loadWheelImagesUseCase(assetsHost, assets, useLocalFallback = true)
            .onSuccess { bitmaps ->
                _state.value = reducer.reduceWithBitmaps(_state.value, bitmaps)
            }
            .onFailure { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = WidgetError.ImageLoadError(error.message ?: "Failed to load images")
                )
                _sideEffects.emit(WidgetSideEffect.ShowError(_state.value.error!!))
            }
    }

    /**
     * Executes the wheel spin logic by calculating the target rotation based on the current
     * configuration and emitting a side effect to trigger the UI animation.
     *
     * This function updates the repository spinning state, uses [calculateSpinResultUseCase]
     * to determine the end position, updates the internal state via the reducer, and
     * notifies the UI to start the animation with the configured duration and easing.
     */
    private suspend fun performSpin() {
        val config = _state.value.config ?: return
        val currentRotation = _state.value.currentRotation

        repository.setSpinning(true)

        val result = calculateSpinResultUseCase(
            currentRotation = currentRotation,
            rotation = config.wheelRotation
        )

        _state.value = reducer.reduceWithTargetRotation(_state.value, result.finalRotation)

        _sideEffects.emit(
            WidgetSideEffect.TriggerSpinAnimation(
                fromDegrees = currentRotation,
                toDegrees = result.finalRotation,
                durationMs = config.wheelRotation.durationMs,
                easing = config.wheelRotation.spinEasing
            )
        )
    }

    /**
     * Handles the completion of the spin animation.
     *
     * This function performs the following cleanup and state updates:
     * 1. Calculates the normalized [finalRotation] (0-359 degrees).
     * 2. Persists the final rotation state and updates the spinning status in the [repository].
     * 3. Emits a [WidgetSideEffect.SpinCompleted] event with the resulting data.
     * 4. Emits a [WidgetSideEffect.SaveState] event to trigger a global state persistence.
     */
    private suspend fun onSpinComplete() {
        val finalRotation = _state.value.targetRotation % 360f
        repository.saveRotationState(finalRotation)
        repository.setSpinning(false)

        _sideEffects.emit(
            WidgetSideEffect.SpinCompleted(
                SpinResult(
                    finalRotation = finalRotation,
                    totalSpins = 0,
                    segment = 0
                )
            )
        )
        _sideEffects.emit(WidgetSideEffect.SaveState)
    }

    fun getCurrentState(): WidgetState = _state.value

    fun onCleared() {
        scope.cancel()
    }
}

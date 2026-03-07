package com.codebaron.spinwheel.widget.domain.usecase

import com.codebaron.spinwheel.widget.domain.model.WheelAssets
import com.codebaron.spinwheel.widget.domain.model.WheelBitmaps
import com.codebaron.spinwheel.widget.domain.repository.WidgetConfigRepository

class LoadWheelImagesUseCase(
    private val repository: WidgetConfigRepository
) {
    companion object {
        private const val LOCAL_ASSETS_HOST = "local"
    }

    suspend operator fun invoke(
        baseUrl: String,
        assets: WheelAssets,
        useLocalFallback: Boolean = true
    ): Result<WheelBitmaps> {
        // If baseUrl is "local", use local resources directly without network
        val useLocalOnly = baseUrl.equals(LOCAL_ASSETS_HOST, ignoreCase = true)

        return runCatching {
            val background = loadImage(baseUrl, assets.background, useLocalFallback, useLocalOnly)
                ?: throw IllegalStateException("Failed to load background image")

            val wheel = loadImage(baseUrl, assets.wheel, useLocalFallback, useLocalOnly)
                ?: throw IllegalStateException("Failed to load wheel image")

            val frame = loadImage(baseUrl, assets.wheelFrame, useLocalFallback, useLocalOnly)
                ?: throw IllegalStateException("Failed to load frame image")

            val spinButton = loadImage(baseUrl, assets.wheelSpin, useLocalFallback, useLocalOnly)
                ?: throw IllegalStateException("Failed to load spin button image")

            WheelBitmaps(
                background = background,
                wheel = wheel,
                frame = frame,
                spinButton = spinButton
            )
        }
    }

    private suspend fun loadImage(
        baseUrl: String,
        filename: String,
        useLocalFallback: Boolean,
        useLocalOnly: Boolean
    ): android.graphics.Bitmap? {
        // If using local only mode, skip network entirely
        if (useLocalOnly) {
            return repository.getLocalImage(filename)
        }

        // Try network/cache first
        val networkResult = repository.downloadAndCacheImage(baseUrl, filename)
        if (networkResult.isSuccess) {
            return networkResult.getOrNull()
        }

        // Fall back to local resources if enabled
        if (useLocalFallback) {
            return repository.getLocalImage(filename)
        }

        return null
    }
}

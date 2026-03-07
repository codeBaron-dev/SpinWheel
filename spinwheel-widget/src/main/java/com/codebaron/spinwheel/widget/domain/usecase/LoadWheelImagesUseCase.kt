package com.codebaron.spinwheel.widget.domain.usecase

import com.codebaron.spinwheel.widget.domain.model.WheelAssets
import com.codebaron.spinwheel.widget.domain.model.WheelBitmaps
import com.codebaron.spinwheel.widget.domain.repository.WidgetConfigRepository

class LoadWheelImagesUseCase(
    private val repository: WidgetConfigRepository
) {
    suspend operator fun invoke(
        baseUrl: String,
        assets: WheelAssets,
        useLocalFallback: Boolean = true
    ): Result<WheelBitmaps> {
        return runCatching {
            // Try to load from network/cache first, fall back to local resources
            val background = loadImage(baseUrl, assets.background, useLocalFallback)
                ?: throw IllegalStateException("Failed to load background image")

            val wheel = loadImage(baseUrl, assets.wheel, useLocalFallback)
                ?: throw IllegalStateException("Failed to load wheel image")

            val frame = loadImage(baseUrl, assets.wheelFrame, useLocalFallback)
                ?: throw IllegalStateException("Failed to load frame image")

            val spinButton = loadImage(baseUrl, assets.wheelSpin, useLocalFallback)
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
        useLocalFallback: Boolean
    ): android.graphics.Bitmap? {
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

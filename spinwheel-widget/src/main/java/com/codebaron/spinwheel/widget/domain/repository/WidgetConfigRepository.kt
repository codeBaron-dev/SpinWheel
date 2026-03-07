package com.codebaron.spinwheel.widget.domain.repository

import android.graphics.Bitmap
import com.codebaron.spinwheel.widget.domain.model.WidgetConfig

interface WidgetConfigRepository {
    suspend fun fetchConfig(url: String): Result<WidgetConfig>
    suspend fun getDefaultConfig(): WidgetConfig
    suspend fun getCachedConfig(): WidgetConfig?
    suspend fun cacheConfig(config: WidgetConfig)
    fun isCacheValid(expirationSeconds: Int): Boolean
    fun getLastFetchTime(): Long
    suspend fun downloadAndCacheImage(baseUrl: String, filename: String): Result<Bitmap>
    suspend fun getLocalImage(resourceName: String): Bitmap?
    fun saveRotationState(degrees: Float)
    fun getRotationState(): Float
    fun setSpinning(isSpinning: Boolean)
    fun isSpinning(): Boolean
    suspend fun clearCache()
}

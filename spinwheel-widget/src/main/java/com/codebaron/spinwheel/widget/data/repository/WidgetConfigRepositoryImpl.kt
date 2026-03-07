package com.codebaron.spinwheel.widget.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.codebaron.spinwheel.widget.R
import com.codebaron.spinwheel.widget.data.local.ImageCacheManager
import com.codebaron.spinwheel.widget.data.local.SharedPreferencesManager
import com.codebaron.spinwheel.widget.data.mapper.ConfigDtoMapper
import com.codebaron.spinwheel.widget.data.remote.api.WidgetConfigApi
import com.codebaron.spinwheel.widget.data.remote.dto.WidgetConfigResponse
import com.codebaron.spinwheel.widget.domain.model.WidgetConfig
import com.codebaron.spinwheel.widget.domain.repository.WidgetConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class WidgetConfigRepositoryImpl(
    private val context: Context,
    private val api: WidgetConfigApi,
    private val prefsManager: SharedPreferencesManager,
    private val imageCacheManager: ImageCacheManager,
    private val mapper: ConfigDtoMapper,
    private val json: Json
) : WidgetConfigRepository {

    override suspend fun fetchConfig(url: String): Result<WidgetConfig> {
        return api.fetchConfig(url).map { response ->
            mapper.mapToDomain(response.data.first())
        }
    }

    override suspend fun getDefaultConfig(): WidgetConfig = withContext(Dispatchers.IO) {
        val inputStream = context.resources.openRawResource(R.raw.default_config)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val response = json.decodeFromString<WidgetConfigResponse>(jsonString)
        mapper.mapToDomain(response.data.first())
    }

    override suspend fun getCachedConfig(): WidgetConfig? {
        val entity = prefsManager.getCachedConfig() ?: return null
        return mapper.mapFromEntity(entity)
    }

    override suspend fun cacheConfig(config: WidgetConfig) {
        val entity = mapper.mapToEntity(config)
        prefsManager.saveCachedConfig(entity)
    }

    override fun isCacheValid(expirationSeconds: Int): Boolean {
        return !prefsManager.isCacheExpired(expirationSeconds)
    }

    override fun getLastFetchTime(): Long {
        return prefsManager.getLastFetchTime()
    }

    override suspend fun downloadAndCacheImage(
        baseUrl: String,
        filename: String
    ): Result<Bitmap> {
        // Check cache first
        imageCacheManager.getCachedImage(filename)?.let {
            return Result.success(it)
        }

        // Download and cache
        val imageUrl = buildImageUrl(baseUrl, filename)
        return api.downloadImage(imageUrl).map { bytes ->
            imageCacheManager.cacheImage(filename, bytes)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: throw IllegalStateException("Failed to decode image: $filename")
        }
    }

    override suspend fun getLocalImage(resourceName: String): Bitmap? =
        withContext(Dispatchers.IO) {
            val resourceId = when (resourceName) {
                "wheel.png" -> R.drawable.wheel
                "wheel-frame.png", "wheel_frame.png" -> R.drawable.wheel_frame
                "wheel-spin.png", "wheel_spin.png" -> R.drawable.wheel_spin
                "bg.jpeg", "bg_wheel" -> R.drawable.bg_wheel
                else -> null
            }

            resourceId?.let { resId ->
                // Try to decode as bitmap first (for PNG/JPG)
                BitmapFactory.decodeResource(context.resources, resId)
                    ?: renderDrawableToBitmap(resId) // Fall back to rendering drawable (for XML)
            }
        }

    private fun renderDrawableToBitmap(resId: Int, size: Int = 500): Bitmap? {
        return try {
            val drawable = androidx.core.content.ContextCompat.getDrawable(context, resId)
                ?: return null
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    override fun saveRotationState(degrees: Float) {
        prefsManager.saveCurrentRotation(degrees)
    }

    override fun getRotationState(): Float {
        return prefsManager.getCurrentRotation()
    }

    override fun setSpinning(isSpinning: Boolean) {
        prefsManager.setSpinning(isSpinning)
    }

    override fun isSpinning(): Boolean {
        return prefsManager.isSpinning()
    }

    override suspend fun clearCache() {
        prefsManager.clearCache()
        imageCacheManager.clearImageCache()
    }

    private fun buildImageUrl(baseUrl: String, filename: String): String {
        val cleanBase = baseUrl.trimEnd('/')
        val cleanFile = filename.trimStart('/')
        return "$cleanBase/$cleanFile"
    }
}

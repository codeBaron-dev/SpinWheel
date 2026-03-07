package com.codebaron.spinwheel.widget.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.codebaron.spinwheel.widget.data.local.entity.CachedConfigEntity
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray

@OptIn(ExperimentalSerializationApi::class)
class SharedPreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    private val cbor = Cbor {
        ignoreUnknownKeys = true
    }

    companion object {
        private const val PREFS_NAME = "spinwheel_widget_prefs"
        private const val KEY_CACHED_CONFIG = "cached_config"
        private const val KEY_LAST_FETCH_TIME = "last_fetch_time"
        private const val KEY_CURRENT_ROTATION = "current_rotation"
        private const val KEY_IS_SPINNING = "is_spinning"
    }

    fun saveCachedConfig(config: CachedConfigEntity) {
        val bytes = cbor.encodeToByteArray(config)
        val encoded = Base64.encodeToString(bytes, Base64.DEFAULT)
        prefs.edit()
            .putString(KEY_CACHED_CONFIG, encoded)
            .putLong(KEY_LAST_FETCH_TIME, System.currentTimeMillis())
            .apply()
    }

    fun getCachedConfig(): CachedConfigEntity? {
        val encoded = prefs.getString(KEY_CACHED_CONFIG, null) ?: return null
        return try {
            val bytes = Base64.decode(encoded, Base64.DEFAULT)
            cbor.decodeFromByteArray<CachedConfigEntity>(bytes)
        } catch (e: Exception) {
            null
        }
    }

    fun getLastFetchTime(): Long = prefs.getLong(KEY_LAST_FETCH_TIME, 0L)

    fun isCacheExpired(expirationSeconds: Int): Boolean {
        val lastFetch = getLastFetchTime()
        if (lastFetch == 0L) return true
        return System.currentTimeMillis() - lastFetch > expirationSeconds * 1000L
    }

    fun saveCurrentRotation(degrees: Float) {
        prefs.edit().putFloat(KEY_CURRENT_ROTATION, degrees).apply()
    }

    fun getCurrentRotation(): Float = prefs.getFloat(KEY_CURRENT_ROTATION, 0f)

    fun setSpinning(isSpinning: Boolean) {
        prefs.edit().putBoolean(KEY_IS_SPINNING, isSpinning).apply()
    }

    fun isSpinning(): Boolean = prefs.getBoolean(KEY_IS_SPINNING, false)

    fun clearCache() {
        prefs.edit().clear().apply()
    }
}

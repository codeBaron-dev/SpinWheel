package com.codebaron.spinwheel.widget.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ImageCacheManager(context: Context) {

    private val cacheDir: File = File(context.cacheDir, CACHE_FOLDER)

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    companion object {
        private const val CACHE_FOLDER = "spinwheel_images"
    }

    suspend fun cacheImage(filename: String, bytes: ByteArray): File =
        withContext(Dispatchers.IO) {
            val file = getImageFile(filename)
            FileOutputStream(file).use { fos ->
                fos.write(bytes)
            }
            file
        }

    suspend fun getCachedImage(filename: String): Bitmap? =
        withContext(Dispatchers.IO) {
            val file = getImageFile(filename)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        }

    fun isImageCached(filename: String): Boolean {
        return getImageFile(filename).exists()
    }

    suspend fun clearImageCache() = withContext(Dispatchers.IO) {
        cacheDir.listFiles()?.forEach { it.delete() }
    }

    fun getCacheSize(): Long {
        return cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    private fun getImageFile(filename: String): File {
        val hashedName = filename.hashCode().toString()
        return File(cacheDir, hashedName)
    }
}

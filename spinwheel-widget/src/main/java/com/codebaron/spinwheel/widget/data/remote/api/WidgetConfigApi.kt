package com.codebaron.spinwheel.widget.data.remote.api

import com.codebaron.spinwheel.widget.data.remote.dto.WidgetConfigResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

interface WidgetConfigApi {
    suspend fun fetchConfig(url: String): Result<WidgetConfigResponse>
    suspend fun downloadImage(imageUrl: String): Result<ByteArray>
}

class WidgetConfigApiImpl(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) : WidgetConfigApi {

    override suspend fun fetchConfig(url: String): Result<WidgetConfigResponse> =
        withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "application/json")
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("HTTP ${response.code}: ${response.message}")
                    }
                    val body = response.body?.string()
                        ?: throw IOException("Empty response body")
                    json.decodeFromString<WidgetConfigResponse>(body)
                }
            }
        }

    override suspend fun downloadImage(imageUrl: String): Result<ByteArray> =
        withContext(Dispatchers.IO) {
            runCatching {
                val directUrl = convertToDirectDownloadUrl(imageUrl)
                val request = Request.Builder()
                    .url(directUrl)
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("HTTP ${response.code}: ${response.message}")
                    }
                    response.body?.bytes()
                        ?: throw IOException("Empty image body")
                }
            }
        }

    private fun convertToDirectDownloadUrl(url: String): String {
        // Convert Google Drive sharing URLs to direct download URLs
        // Format: https://drive.google.com/file/d/FILE_ID/view?usp=sharing
        // To: https://drive.google.com/uc?export=download&id=FILE_ID
        val driveFileIdRegex = Regex("/d/([a-zA-Z0-9_-]+)/")
        val match = driveFileIdRegex.find(url)

        return if (match != null) {
            val fileId = match.groupValues[1]
            "https://drive.google.com/uc?export=download&id=$fileId"
        } else {
            url
        }
    }
}

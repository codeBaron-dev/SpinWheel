package com.codebaron.spinwheel.widget.data.local.entity

import kotlinx.serialization.Serializable

@Serializable
data class CachedConfigEntity(
    val id: String,
    val name: String,
    val type: String,
    val refreshIntervalSeconds: Int,
    val networkTimeoutMs: Int,
    val retryAttempts: Int,
    val cacheExpirationSeconds: Int,
    val debugMode: Boolean,
    val assetsHost: String,
    val rotationDurationMs: Int,
    val minimumSpins: Int,
    val maximumSpins: Int,
    val spinEasing: String,
    val bgAsset: String,
    val wheelFrameAsset: String,
    val wheelSpinAsset: String,
    val wheelAsset: String
)

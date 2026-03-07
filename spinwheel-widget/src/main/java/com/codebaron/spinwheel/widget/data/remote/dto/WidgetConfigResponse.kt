package com.codebaron.spinwheel.widget.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WidgetConfigResponse(
    val data: List<WidgetDataDto>,
    val meta: MetaDto
)

@Serializable
data class WidgetDataDto(
    val id: String,
    val name: String,
    val type: String,
    val network: NetworkDto,
    val wheel: WheelDto
)

@Serializable
data class NetworkDto(
    val attributes: NetworkAttributesDto,
    val assets: AssetsHostDto
)

@Serializable
data class NetworkAttributesDto(
    val refreshInterval: Int,
    val networkTimeout: Int,
    val retryAttempts: Int,
    val cacheExpiration: Int,
    val debugMode: Boolean
)

@Serializable
data class AssetsHostDto(
    val host: String
)

@Serializable
data class WheelDto(
    val rotation: RotationDto,
    val assets: WheelAssetsDto
)

@Serializable
data class RotationDto(
    val duration: Int,
    val minimumSpins: Int,
    val maximumSpins: Int,
    val spinEasing: String
)

@Serializable
data class WheelAssetsDto(
    val bg: String,
    val wheelFrame: String,
    val wheelSpin: String,
    val wheel: String
)

@Serializable
data class MetaDto(
    val version: Int,
    val copyright: String
)

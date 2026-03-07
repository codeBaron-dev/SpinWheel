package com.codebaron.spinwheel.widget.data.mapper

import com.codebaron.spinwheel.widget.data.local.entity.CachedConfigEntity
import com.codebaron.spinwheel.widget.data.remote.dto.WidgetDataDto
import com.codebaron.spinwheel.widget.domain.model.EasingType
import com.codebaron.spinwheel.widget.domain.model.NetworkAttributes
import com.codebaron.spinwheel.widget.domain.model.WheelAssets
import com.codebaron.spinwheel.widget.domain.model.WheelRotation
import com.codebaron.spinwheel.widget.domain.model.WidgetConfig

class ConfigDtoMapper {

    fun mapToDomain(dto: WidgetDataDto): WidgetConfig {
        return WidgetConfig(
            id = dto.id,
            name = dto.name,
            type = dto.type,
            networkAttributes = NetworkAttributes(
                refreshIntervalSeconds = dto.network.attributes.refreshInterval,
                networkTimeoutMs = dto.network.attributes.networkTimeout,
                retryAttempts = dto.network.attributes.retryAttempts,
                cacheExpirationSeconds = dto.network.attributes.cacheExpiration,
                debugMode = dto.network.attributes.debugMode
            ),
            assetsHost = dto.network.assets.host,
            wheelRotation = WheelRotation(
                durationMs = dto.wheel.rotation.duration,
                minimumSpins = dto.wheel.rotation.minimumSpins,
                maximumSpins = dto.wheel.rotation.maximumSpins,
                spinEasing = EasingType.fromString(dto.wheel.rotation.spinEasing)
            ),
            wheelAssets = WheelAssets(
                background = dto.wheel.assets.bg,
                wheelFrame = dto.wheel.assets.wheelFrame,
                wheelSpin = dto.wheel.assets.wheelSpin,
                wheel = dto.wheel.assets.wheel
            )
        )
    }

    fun mapToEntity(config: WidgetConfig): CachedConfigEntity {
        return CachedConfigEntity(
            id = config.id,
            name = config.name,
            type = config.type,
            refreshIntervalSeconds = config.networkAttributes.refreshIntervalSeconds,
            networkTimeoutMs = config.networkAttributes.networkTimeoutMs,
            retryAttempts = config.networkAttributes.retryAttempts,
            cacheExpirationSeconds = config.networkAttributes.cacheExpirationSeconds,
            debugMode = config.networkAttributes.debugMode,
            assetsHost = config.assetsHost,
            rotationDurationMs = config.wheelRotation.durationMs,
            minimumSpins = config.wheelRotation.minimumSpins,
            maximumSpins = config.wheelRotation.maximumSpins,
            spinEasing = config.wheelRotation.spinEasing.name,
            bgAsset = config.wheelAssets.background,
            wheelFrameAsset = config.wheelAssets.wheelFrame,
            wheelSpinAsset = config.wheelAssets.wheelSpin,
            wheelAsset = config.wheelAssets.wheel
        )
    }

    fun mapFromEntity(entity: CachedConfigEntity): WidgetConfig {
        return WidgetConfig(
            id = entity.id,
            name = entity.name,
            type = entity.type,
            networkAttributes = NetworkAttributes(
                refreshIntervalSeconds = entity.refreshIntervalSeconds,
                networkTimeoutMs = entity.networkTimeoutMs,
                retryAttempts = entity.retryAttempts,
                cacheExpirationSeconds = entity.cacheExpirationSeconds,
                debugMode = entity.debugMode
            ),
            assetsHost = entity.assetsHost,
            wheelRotation = WheelRotation(
                durationMs = entity.rotationDurationMs,
                minimumSpins = entity.minimumSpins,
                maximumSpins = entity.maximumSpins,
                spinEasing = EasingType.valueOf(entity.spinEasing)
            ),
            wheelAssets = WheelAssets(
                background = entity.bgAsset,
                wheelFrame = entity.wheelFrameAsset,
                wheelSpin = entity.wheelSpinAsset,
                wheel = entity.wheelAsset
            )
        )
    }
}

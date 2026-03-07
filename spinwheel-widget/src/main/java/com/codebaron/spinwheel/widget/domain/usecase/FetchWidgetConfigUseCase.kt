package com.codebaron.spinwheel.widget.domain.usecase

import com.codebaron.spinwheel.widget.domain.model.WidgetConfig
import com.codebaron.spinwheel.widget.domain.repository.WidgetConfigRepository

class FetchWidgetConfigUseCase(
    private val repository: WidgetConfigRepository
) {
    suspend operator fun invoke(configUrl: String): Result<WidgetConfig> {
        return repository.fetchConfig(configUrl).onSuccess { config ->
            repository.cacheConfig(config)
        }
    }
}

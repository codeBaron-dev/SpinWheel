package com.codebaron.spinwheel.widget.domain.usecase

import com.codebaron.spinwheel.widget.domain.model.WidgetConfig
import com.codebaron.spinwheel.widget.domain.repository.WidgetConfigRepository

class GetCachedConfigUseCase(
    private val repository: WidgetConfigRepository
) {
    suspend operator fun invoke(): WidgetConfig? {
        return repository.getCachedConfig()
    }
}

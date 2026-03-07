package com.codebaron.spinwheel.widget.di

import com.codebaron.spinwheel.widget.domain.usecase.CalculateSpinResultUseCase
import com.codebaron.spinwheel.widget.domain.usecase.FetchWidgetConfigUseCase
import com.codebaron.spinwheel.widget.domain.usecase.GetCachedConfigUseCase
import com.codebaron.spinwheel.widget.domain.usecase.GetDefaultConfigUseCase
import com.codebaron.spinwheel.widget.domain.usecase.LoadWheelImagesUseCase
import org.koin.dsl.module

val domainModule = module {

    factory { FetchWidgetConfigUseCase(repository = get()) }

    factory { GetCachedConfigUseCase(repository = get()) }

    factory { GetDefaultConfigUseCase(repository = get()) }

    factory { LoadWheelImagesUseCase(repository = get()) }

    factory { CalculateSpinResultUseCase() }
}

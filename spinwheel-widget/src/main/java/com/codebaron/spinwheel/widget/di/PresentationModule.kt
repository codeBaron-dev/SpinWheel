package com.codebaron.spinwheel.widget.di

import com.codebaron.spinwheel.widget.presentation.mvi.WidgetReducer
import com.codebaron.spinwheel.widget.presentation.viewmodel.WidgetViewModel
import org.koin.dsl.module

val presentationModule = module {

    factory { WidgetReducer() }

    // Singleton ViewModel to maintain state across widget updates
    single {
        WidgetViewModel(
            fetchConfigUseCase = get(),
            getCachedConfigUseCase = get(),
            getDefaultConfigUseCase = get(),
            loadWheelImagesUseCase = get(),
            calculateSpinResultUseCase = get(),
            repository = get(),
            reducer = get()
        )
    }
}

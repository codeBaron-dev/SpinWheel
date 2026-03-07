package com.codebaron.spinwheel.widget.di

import com.codebaron.spinwheel.widget.data.local.ImageCacheManager
import com.codebaron.spinwheel.widget.data.local.SharedPreferencesManager
import com.codebaron.spinwheel.widget.data.mapper.ConfigDtoMapper
import com.codebaron.spinwheel.widget.data.repository.WidgetConfigRepositoryImpl
import com.codebaron.spinwheel.widget.domain.repository.WidgetConfigRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {

    single { SharedPreferencesManager(androidContext()) }

    single { ImageCacheManager(androidContext()) }

    single { ConfigDtoMapper() }

    single<WidgetConfigRepository> {
        WidgetConfigRepositoryImpl(
            context = androidContext(),
            api = get(),
            prefsManager = get(),
            imageCacheManager = get(),
            mapper = get(),
            json = get()
        )
    }
}

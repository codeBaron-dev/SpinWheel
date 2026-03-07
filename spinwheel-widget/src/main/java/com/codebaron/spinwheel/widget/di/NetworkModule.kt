package com.codebaron.spinwheel.widget.di

import com.codebaron.spinwheel.widget.data.remote.api.WidgetConfigApi
import com.codebaron.spinwheel.widget.data.remote.api.WidgetConfigApiImpl
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val networkModule = module {

    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }

    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    single<WidgetConfigApi> {
        WidgetConfigApiImpl(
            okHttpClient = get(),
            json = get()
        )
    }
}

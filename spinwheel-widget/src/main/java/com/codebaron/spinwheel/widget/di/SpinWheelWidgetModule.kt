package com.codebaron.spinwheel.widget.di

import org.koin.core.module.Module

/**
 * All Koin modules for the SpinWheel widget library.
 * Include these modules when initializing Koin in your application.
 *
 * Usage:
 * ```kotlin
 * startKoin {
 *     androidContext(this@MyApplication)
 *     modules(spinWheelWidgetModules)
 * }
 * ```
 */
val spinWheelWidgetModules: List<Module> = listOf(
    networkModule,
    dataModule,
    domainModule,
    presentationModule
)

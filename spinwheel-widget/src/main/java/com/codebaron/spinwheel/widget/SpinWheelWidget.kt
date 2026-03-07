package com.codebaron.spinwheel.widget

import android.content.Context
import com.codebaron.spinwheel.widget.di.spinWheelWidgetModules
import com.codebaron.spinwheel.widget.presentation.widget.SpinWheelWidgetProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin

/**
 * Main entry point for the SpinWheel Widget library.
 *
 * Initialize in your Application class:
 * ```kotlin
 * class MyApp : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         SpinWheelWidget.initialize(this)
 *     }
 * }
 * ```
 */
object SpinWheelWidget {

    private var isInitialized = false

    /**
     * Initialize the SpinWheel Widget library.
     * Must be called before using any widget functionality.
     *
     * @param context Application context
     */
    fun initialize(context: Context) {
        if (isInitialized) return

        // Check if Koin is already started
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(context.applicationContext)
                modules(spinWheelWidgetModules)
            }
        } else {
            // Koin already started, just load our modules
            loadKoinModules(spinWheelWidgetModules)
        }

        isInitialized = true
    }

    /**
     * Request all widgets to refresh their content.
     *
     * @param context Context to send broadcast
     */
    fun refreshAllWidgets(context: Context) {
        SpinWheelWidgetProvider.requestUpdate(context)
    }

    /**
     * Get the number of active widget instances.
     *
     * @param context Context to access widget manager
     * @return Number of widget instances on home screen
     */
    fun getWidgetCount(context: Context): Int {
        return SpinWheelWidgetProvider.getWidgetIds(context).size
    }

    /**
     * Check if any widgets are currently installed.
     *
     * @param context Context to access widget manager
     * @return true if at least one widget is on the home screen
     */
    fun hasWidgets(context: Context): Boolean {
        return getWidgetCount(context) > 0
    }
}

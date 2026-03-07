package com.codebaron.spinwheel.rn

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import com.codebaron.spinwheel.widget.SpinWheelWidget
import com.codebaron.spinwheel.widget.presentation.widget.SpinWheelWidgetProvider
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class SpinWheelModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String = "SpinWheelWidget"

    @ReactMethod
    fun initialize(promise: Promise) {
        try {
            SpinWheelWidget.initialize(reactContext)
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("INIT_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun updateWidget(configUrl: String, promise: Promise) {
        try {
            // Ensure initialized
            SpinWheelWidget.initialize(reactContext)

            // Refresh all widgets
            SpinWheelWidget.refreshAllWidgets(reactContext)
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("UPDATE_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun isWidgetInstalled(promise: Promise) {
        try {
            val hasWidgets = SpinWheelWidget.hasWidgets(reactContext)
            promise.resolve(hasWidgets)
        } catch (e: Exception) {
            promise.reject("CHECK_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun getWidgetCount(promise: Promise) {
        try {
            val count = SpinWheelWidget.getWidgetCount(reactContext)
            promise.resolve(count)
        } catch (e: Exception) {
            promise.reject("COUNT_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun clearCache(promise: Promise) {
        try {
            // Clear widget cache through repository
            // This would need to be exposed through SpinWheelWidget
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("CLEAR_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun spinWidget(promise: Promise) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(reactContext)
            val componentName = ComponentName(reactContext, SpinWheelWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (widgetIds.isNotEmpty()) {
                val intent = Intent(reactContext, SpinWheelWidgetProvider::class.java).apply {
                    action = SpinWheelWidgetProvider.ACTION_SPIN
                    putExtra(SpinWheelWidgetProvider.EXTRA_WIDGET_ID, widgetIds.first())
                }
                reactContext.sendBroadcast(intent)
                promise.resolve(true)
            } else {
                promise.reject("NO_WIDGET", "No widget installed")
            }
        } catch (e: Exception) {
            promise.reject("SPIN_ERROR", e.message, e)
        }
    }

    // Required for RN New Architecture support
    @ReactMethod
    fun addListener(eventName: String) {
        // Keep: Required for event emitter
    }

    @ReactMethod
    fun removeListeners(count: Int) {
        // Keep: Required for event emitter
    }
}

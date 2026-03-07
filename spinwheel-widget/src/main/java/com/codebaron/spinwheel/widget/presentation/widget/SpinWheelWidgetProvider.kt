package com.codebaron.spinwheel.widget.presentation.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.codebaron.spinwheel.widget.R
import com.codebaron.spinwheel.widget.presentation.mvi.WidgetError
import com.codebaron.spinwheel.widget.presentation.mvi.WidgetIntent
import com.codebaron.spinwheel.widget.presentation.mvi.WidgetSideEffect
import com.codebaron.spinwheel.widget.presentation.mvi.WidgetState
import com.codebaron.spinwheel.widget.presentation.renderer.WheelBitmapComposer
import com.codebaron.spinwheel.widget.presentation.viewmodel.WidgetViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SpinWheelWidgetProvider : AppWidgetProvider(), KoinComponent {

    private val viewModel: WidgetViewModel by inject()

    companion object {
        const val ACTION_SPIN = "com.codebaron.spinwheel.ACTION_SPIN"
        const val ACTION_SPIN_COMPLETE = "com.codebaron.spinwheel.ACTION_SPIN_COMPLETE"
        const val ACTION_REFRESH = "com.codebaron.spinwheel.ACTION_REFRESH"
        const val EXTRA_WIDGET_ID = "widget_id"
        const val EXTRA_TARGET_ROTATION = "target_rotation"

        fun getWidgetIds(context: Context): IntArray {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, SpinWheelWidgetProvider::class.java)
            return appWidgetManager.getAppWidgetIds(componentName)
        }

        fun requestUpdate(context: Context) {
            val intent = Intent(context, SpinWheelWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            context.sendBroadcast(intent)
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_SPIN -> handleSpinAction(context, intent)
            ACTION_SPIN_COMPLETE -> handleSpinComplete(context, intent)
            ACTION_REFRESH -> handleRefresh(context)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Widget first added - initialize
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Last widget removed - cleanup
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        scope.launch {
            try {
                viewModel.processIntent(WidgetIntent.Initialize)

                // Wait for state to be ready (with timeout)
                val state = viewModel.state.first { it.isReady || it.error != null }
                val remoteViews = createRemoteViews(context, state, widgetId)
                appWidgetManager.updateAppWidget(widgetId, remoteViews)
            } catch (e: Exception) {
                val errorViews = createErrorRemoteViews(context, widgetId, e.message)
                appWidgetManager.updateAppWidget(widgetId, errorViews)
            }
        }
    }

    private fun createRemoteViews(
        context: Context,
        state: WidgetState,
        widgetId: Int
    ): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_spin_wheel).apply {
            when {
                state.isReady && state.wheelBitmaps != null -> {
                    val composedBitmap = WheelBitmapComposer.compose(
                        state.wheelBitmaps,
                        state.currentRotation
                    )

                    setImageViewBitmap(R.id.iv_wheel_composed, composedBitmap)
                    setViewVisibility(R.id.progress_loading, View.GONE)
                    setViewVisibility(R.id.tv_error, View.GONE)
                    setViewVisibility(R.id.iv_wheel_composed, View.VISIBLE)

                    // Set click listener for spin
                    val spinIntent = Intent(context, SpinWheelWidgetProvider::class.java).apply {
                        action = ACTION_SPIN
                        putExtra(EXTRA_WIDGET_ID, widgetId)
                    }
                    val spinPendingIntent = PendingIntent.getBroadcast(
                        context,
                        widgetId,
                        spinIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setOnClickPendingIntent(R.id.btn_spin, spinPendingIntent)
                    setOnClickPendingIntent(R.id.iv_wheel_composed, spinPendingIntent)
                }

                state.error != null -> {
                    setViewVisibility(R.id.progress_loading, View.GONE)
                    setViewVisibility(R.id.iv_wheel_composed, View.GONE)
                    setViewVisibility(R.id.tv_error, View.VISIBLE)
                    setTextViewText(R.id.tv_error, getErrorMessage(context, state.error))

                    // Set click listener for retry
                    val refreshIntent = Intent(context, SpinWheelWidgetProvider::class.java).apply {
                        action = ACTION_REFRESH
                    }
                    val refreshPendingIntent = PendingIntent.getBroadcast(
                        context,
                        widgetId + 1000,
                        refreshIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setOnClickPendingIntent(R.id.tv_error, refreshPendingIntent)
                }

                else -> {
                    // Loading state
                    setViewVisibility(R.id.progress_loading, View.VISIBLE)
                    setViewVisibility(R.id.iv_wheel_composed, View.GONE)
                    setViewVisibility(R.id.tv_error, View.GONE)
                }
            }
        }
    }

    private fun createErrorRemoteViews(
        context: Context,
        widgetId: Int,
        errorMessage: String?
    ): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_spin_wheel).apply {
            setViewVisibility(R.id.progress_loading, View.GONE)
            setViewVisibility(R.id.iv_wheel_composed, View.GONE)
            setViewVisibility(R.id.tv_error, View.VISIBLE)
            setTextViewText(R.id.tv_error, errorMessage ?: context.getString(R.string.error_unknown))

            // Set click listener for retry
            val refreshIntent = Intent(context, SpinWheelWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                widgetId + 1000,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.tv_error, refreshPendingIntent)
        }
    }

    private fun handleSpinAction(context: Context, intent: Intent) {
        val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        scope.launch {
            viewModel.processIntent(WidgetIntent.SpinWheel)

            // Collect the spin animation side effect
            viewModel.sideEffects.collect { effect ->
                when (effect) {
                    is WidgetSideEffect.TriggerSpinAnimation -> {
                        // Start animation service
                        val animationIntent = Intent(context, WidgetAnimationService::class.java).apply {
                            putExtra(EXTRA_WIDGET_ID, widgetId)
                            putExtra("from_degrees", effect.fromDegrees)
                            putExtra("to_degrees", effect.toDegrees)
                            putExtra("duration", effect.durationMs)
                            putExtra("easing", effect.easing.name)
                        }
                        context.startService(animationIntent)
                        return@collect
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun handleSpinComplete(context: Context, intent: Intent) {
        val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val targetRotation = intent.getFloatExtra(EXTRA_TARGET_ROTATION, 0f)

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        scope.launch {
            viewModel.processIntent(WidgetIntent.SetRotation(targetRotation % 360f))
            viewModel.processIntent(WidgetIntent.SpinComplete)

            val appWidgetManager = AppWidgetManager.getInstance(context)
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun handleRefresh(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetIds = getWidgetIds(context)
        onUpdate(context, appWidgetManager, widgetIds)
    }

    private fun getErrorMessage(context: Context, error: WidgetError): String {
        return when (error) {
            is WidgetError.NetworkError -> context.getString(R.string.error_network)
            is WidgetError.ConfigError -> context.getString(R.string.error_config)
            is WidgetError.ImageLoadError -> context.getString(R.string.error_image)
            is WidgetError.Unknown -> context.getString(R.string.error_unknown)
        }
    }
}

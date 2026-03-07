package com.codebaron.spinwheel.widget.presentation.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.widget.RemoteViews
import com.codebaron.spinwheel.widget.R
import com.codebaron.spinwheel.widget.SpinWheelWidget
import com.codebaron.spinwheel.widget.domain.model.WheelBitmaps
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
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SpinWheelWidgetProvider : AppWidgetProvider(), KoinComponent {

    private val viewModel: WidgetViewModel by inject()

    private fun ensureInitialized(context: Context) {
        try {
            SpinWheelWidget.initialize(context.applicationContext)
        } catch (e: Exception) {
            // Already initialized or error
        }
    }

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
        ensureInitialized(context)
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        ensureInitialized(context)
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
        // Try to show widget with local assets immediately (fast path)
        // This ensures the widget always displays something
        try {
            val fallbackViews = createFallbackRemoteViews(context, widgetId)
            appWidgetManager.updateAppWidget(widgetId, fallbackViews)
        } catch (e: Exception) {
            // Even fallback failed, show loading while we try ViewModel
            val loadingViews = createLoadingRemoteViews(context)
            appWidgetManager.updateAppWidget(widgetId, loadingViews)
        }

        // Then try to load via ViewModel for potential remote config updates
        scope.launch {
            try {
                viewModel.processIntent(WidgetIntent.Initialize)

                // Wait for state to be ready (with timeout of 5 seconds)
                val state = withTimeoutOrNull(5000L) {
                    viewModel.state.first { it.isReady || it.error != null }
                }

                if (state?.isReady == true && state.wheelBitmaps != null) {
                    val remoteViews = createRemoteViews(context, state, widgetId)
                    appWidgetManager.updateAppWidget(widgetId, remoteViews)
                }
                // If state is not ready or has error, we already showed fallback, so do nothing
            } catch (e: Exception) {
                // ViewModel failed, but we already have fallback displayed
                // Log error for debugging
                android.util.Log.e("SpinWheelWidget", "Failed to update via ViewModel", e)
            }
        }
    }

    private fun createLoadingRemoteViews(context: Context): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_spin_wheel).apply {
            setViewVisibility(R.id.progress_loading, View.VISIBLE)
            setViewVisibility(R.id.iv_wheel_composed, View.GONE)
            setViewVisibility(R.id.tv_error, View.GONE)
        }
    }

    private fun createFallbackRemoteViews(context: Context, widgetId: Int): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_spin_wheel).apply {
            // Load local drawable resources directly
            val wheelBitmaps = loadLocalBitmaps(context)
            if (wheelBitmaps != null) {
                val composedBitmap = WheelBitmapComposer.compose(wheelBitmaps, 0f)
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
            } else {
                throw IllegalStateException("Cannot load local bitmaps")
            }
        }
    }

    private fun loadLocalBitmaps(context: Context): WheelBitmaps? {
        return try {
            val resources = context.resources

            // Background is an XML drawable, render it to bitmap
            val background = loadDrawableAsBitmap(context, R.drawable.bg_wheel, 500)
                ?: createSolidColorBitmap(500, 0xFF1a1a2e.toInt())

            val wheel = BitmapFactory.decodeResource(resources, R.drawable.wheel)
                ?: return null
            val frame = BitmapFactory.decodeResource(resources, R.drawable.wheel_frame)
                ?: return null
            val spinButton = BitmapFactory.decodeResource(resources, R.drawable.wheel_spin)
                ?: return null

            WheelBitmaps(
                background = background,
                wheel = wheel,
                frame = frame,
                spinButton = spinButton
            )
        } catch (e: Exception) {
            android.util.Log.e("SpinWheelWidget", "Failed to load local bitmaps", e)
            null
        }
    }

    private fun loadDrawableAsBitmap(context: Context, resId: Int, size: Int): Bitmap? {
        return try {
            // Try to decode as bitmap first (PNG/JPG)
            BitmapFactory.decodeResource(context.resources, resId)
                ?: run {
                    // For XML drawables, render to bitmap
                    val drawable = androidx.core.content.ContextCompat.getDrawable(context, resId)
                        ?: return null
                    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bitmap
                }
        } catch (e: Exception) {
            null
        }
    }

    private fun createSolidColorBitmap(size: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(color)
        return bitmap
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

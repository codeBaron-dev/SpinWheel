package com.codebaron.spinwheel.widget.presentation.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.IBinder
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.PathInterpolator
import android.widget.RemoteViews
import com.codebaron.spinwheel.widget.R
import com.codebaron.spinwheel.widget.domain.model.EasingType
import com.codebaron.spinwheel.widget.presentation.mvi.WidgetState
import com.codebaron.spinwheel.widget.presentation.renderer.WheelBitmapComposer
import com.codebaron.spinwheel.widget.presentation.viewmodel.WidgetViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * A foreground-capable [Service] responsible for managing the spin animation of the App Widget.
 *
 * This service handles the interpolation logic for the wheel's rotation, rendering frame-by-frame
 * updates to the widget's RemoteViews. It implements frame rate throttling (targeting ~20 FPS)
 * to balance smooth visual performance with system resource consumption and AppWidgetManager
 * update limitations.
 *
 * The service expects an [Intent] containing:
 * - [SpinWheelWidgetProvider.EXTRA_WIDGET_ID]: The ID of the widget to animate.
 * - `from_degrees`: The starting rotation angle (Float).
 * - `to_degrees`: The target rotation angle (Float).
 * - `duration`: Animation duration in milliseconds (Int).
 * - `easing`: The name of the [EasingType] to apply to the animation.
 *
 * Upon completion, it broadcasts [SpinWheelWidgetProvider.ACTION_SPIN_COMPLETE] to notify
 * the provider of the final rotation state.
 */
class WidgetAnimationService : Service() {

    private val viewModel: WidgetViewModel by inject()
    private var animator: ValueAnimator? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Frame rate control (targeting ~20 FPS for widget updates)
    private val frameIntervalMs = 50L
    private var lastUpdateTime = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { processAnimationIntent(it) }
        return START_NOT_STICKY
    }

    private fun processAnimationIntent(intent: Intent) {
        val widgetId = intent.getIntExtra(SpinWheelWidgetProvider.EXTRA_WIDGET_ID, -1)
        val fromDegrees = intent.getFloatExtra("from_degrees", 0f)
        val toDegrees = intent.getFloatExtra("to_degrees", 360f)
        val duration = intent.getIntExtra("duration", 2000)
        val easingName = intent.getStringExtra("easing") ?: EasingType.EASE_IN_OUT_CUBIC.name

        if (widgetId == -1) {
            stopSelf()
            return
        }

        startSpinAnimation(widgetId, fromDegrees, toDegrees, duration, easingName)
    }

    private fun startSpinAnimation(
        widgetId: Int,
        fromDegrees: Float,
        toDegrees: Float,
        duration: Int,
        easingName: String
    ) {
        animator?.cancel()

        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val state = viewModel.getCurrentState()

        if (state.wheelBitmaps == null) {
            stopSelf()
            return
        }

        animator = ValueAnimator.ofFloat(fromDegrees, toDegrees).apply {
            this.duration = duration.toLong()
            interpolator = getInterpolator(easingName)

            addUpdateListener { animation ->
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastUpdateTime >= frameIntervalMs) {
                    lastUpdateTime = currentTime

                    val currentRotation = animation.animatedValue as Float
                    updateWidgetRotation(appWidgetManager, widgetId, currentRotation, state)
                }
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // Final update with exact target rotation
                    updateWidgetRotation(appWidgetManager, widgetId, toDegrees, state)

                    // Notify spin complete
                    val completeIntent = Intent(
                        applicationContext,
                        SpinWheelWidgetProvider::class.java
                    ).apply {
                        action = SpinWheelWidgetProvider.ACTION_SPIN_COMPLETE
                        putExtra(SpinWheelWidgetProvider.EXTRA_WIDGET_ID, widgetId)
                        putExtra(SpinWheelWidgetProvider.EXTRA_TARGET_ROTATION, toDegrees)
                    }
                    sendBroadcast(completeIntent)

                    stopSelf()
                }

                override fun onAnimationCancel(animation: Animator) {
                    stopSelf()
                }
            })

            start()
        }
    }

    private fun updateWidgetRotation(
        appWidgetManager: AppWidgetManager,
        widgetId: Int,
        rotation: Float,
        state: WidgetState
    ) {
        scope.launch {
            try {
                if (state.wheelBitmaps != null) {
                    val composedBitmap = WheelBitmapComposer.compose(
                        state.wheelBitmaps,
                        rotation
                    )

                    val remoteViews = RemoteViews(
                        applicationContext.packageName,
                        R.layout.widget_spin_wheel
                    ).apply {
                        setImageViewBitmap(R.id.iv_wheel_composed, composedBitmap)
                    }

                    appWidgetManager.partiallyUpdateAppWidget(widgetId, remoteViews)
                }
            } catch (e: Exception) {
                // Ignore update errors during animation
            }
        }
    }

    private fun getInterpolator(easingName: String): android.view.animation.Interpolator {
        return try {
            when (EasingType.valueOf(easingName)) {
                EasingType.EASE_IN_OUT_CUBIC -> PathInterpolator(0.65f, 0f, 0.35f, 1f)
                EasingType.LINEAR -> LinearInterpolator()
                EasingType.EASE_IN -> AccelerateInterpolator(2f)
                EasingType.EASE_OUT -> DecelerateInterpolator(2f)
            }
        } catch (e: Exception) {
            PathInterpolator(0.65f, 0f, 0.35f, 1f)
        }
    }

    override fun onDestroy() {
        animator?.cancel()
        scope.cancel()
        super.onDestroy()
    }
}

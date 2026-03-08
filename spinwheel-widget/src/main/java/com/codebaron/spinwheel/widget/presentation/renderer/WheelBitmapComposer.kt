package com.codebaron.spinwheel.widget.presentation.renderer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import com.codebaron.spinwheel.widget.domain.model.WheelBitmaps

/**
 * A utility object responsible for composing the final visual representation of the spin wheel.
 * It layers various [WheelBitmaps] components (background, rotating wheel, frame, and spin button)
 * onto a single [Bitmap] and provides placeholder bitmaps for loading and error states.
 *
 * This composer handles:
 * 1. Layering order of wheel components.
 * 2. Applying rotation to the wheel layer.
 * 3. Scaling bitmaps to the desired output dimensions.
 * 4. Generating fallback visual states.
 */
object WheelBitmapComposer {

    private const val DEFAULT_OUTPUT_SIZE = 500
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    fun compose(
        bitmaps: WheelBitmaps,
        wheelRotation: Float,
        outputSize: Int = DEFAULT_OUTPUT_SIZE
    ): Bitmap {
        val output = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val centerX = outputSize / 2f
        val centerY = outputSize / 2f

        // 1. Draw background (fills entire canvas)
        drawScaledBitmap(canvas, bitmaps.background, outputSize, outputSize, 0f, 0f)

        // 2. Draw rotating wheel (centered and rotated)
        drawRotatedBitmap(canvas, bitmaps.wheel, outputSize, outputSize, centerX, centerY, wheelRotation)

        // 3. Draw frame overlay (static, centered)
        drawScaledBitmap(canvas, bitmaps.frame, outputSize, outputSize, 0f, 0f)

        // 4. Draw spin button (static, centered, smaller)
        val buttonSize = outputSize / 4
        val buttonX = centerX - buttonSize / 2f
        val buttonY = centerY - buttonSize / 2f
        drawScaledBitmap(canvas, bitmaps.spinButton, buttonSize, buttonSize, buttonX, buttonY)

        return output
    }

    fun composeWithRotation(
        bitmaps: WheelBitmaps,
        wheelRotation: Float,
        outputSize: Int = DEFAULT_OUTPUT_SIZE
    ): Bitmap {
        return compose(bitmaps, wheelRotation, outputSize)
    }

    private fun drawScaledBitmap(
        canvas: Canvas,
        source: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        left: Float,
        top: Float
    ) {
        val scaled = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
        canvas.drawBitmap(scaled, left, top, paint)
        if (scaled != source) {
            scaled.recycle()
        }
    }

    private fun drawRotatedBitmap(
        canvas: Canvas,
        source: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        centerX: Float,
        centerY: Float,
        rotation: Float
    ) {
        val scaled = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)

        canvas.save()
        canvas.rotate(rotation, centerX, centerY)
        canvas.drawBitmap(scaled, 0f, 0f, paint)
        canvas.restore()

        if (scaled != source) {
            scaled.recycle()
        }
    }

    fun createLoadingBitmap(size: Int): Bitmap {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#1a1a2e")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
        return output
    }

    fun createErrorBitmap(size: Int): Bitmap {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#2d2d2d")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

        // Error X
        val xPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#ff4444")
            style = Paint.Style.STROKE
            strokeWidth = size / 20f
            strokeCap = Paint.Cap.ROUND
        }
        val padding = size / 4f
        canvas.drawLine(padding, padding, size - padding, size - padding, xPaint)
        canvas.drawLine(size - padding, padding, padding, size - padding, xPaint)

        return output
    }
}

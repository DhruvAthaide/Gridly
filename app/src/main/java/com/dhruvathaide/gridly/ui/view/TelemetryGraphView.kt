package com.dhruvathaide.gridly.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import com.dhruvathaide.gridly.data.remote.model.TelemetryDto

class TelemetryGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paintDriver1 = Paint().apply {
        color = Color.CYAN
        style = Paint.Style.STROKE
        strokeWidth = 6f // Thicker
        isAntiAlias = true
        setShadowLayer(12f, 0f, 0f, Color.CYAN) // Glow effect
    }

    private val paintDriver2 = Paint().apply {
        color = Color.MAGENTA
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
        setShadowLayer(12f, 0f, 0f, Color.MAGENTA) // Glow effect
    }
    
    // Fill paints for gradients
    private val paintFill1 = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintGrid = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private var driver1Data: List<TelemetryDto> = emptyList()
    private var driver2Data: List<TelemetryDto> = emptyList()

    init {
        // Hardware acceleration usually needed for ShadowLayer
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    fun setData(d1: List<TelemetryDto>, d2: List<TelemetryDto>) {
        this.driver1Data = d1
        this.driver2Data = d2
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val w = width.toFloat()
        val h = height.toFloat()

        // Draw Grid
        canvas.drawLine(0f, h * 0.25f, w, h * 0.25f, paintGrid)
        canvas.drawLine(0f, h * 0.5f, w, h * 0.5f, paintGrid)
        canvas.drawLine(0f, h * 0.75f, w, h * 0.75f, paintGrid)

        if (driver1Data.isNotEmpty()) {
            drawDriverPath(canvas, driver1Data, paintDriver1, w, h, Color.CYAN)
        }
        
        if (driver2Data.isNotEmpty()) {
            drawDriverPath(canvas, driver2Data, paintDriver2, w, h, Color.MAGENTA)
        }
    }

    private fun drawDriverPath(canvas: Canvas, data: List<TelemetryDto>, paint: Paint, w: Float, h: Float, baseColor: Int) {
        val path = Path()
        val stepX = w / (data.size - 1).coerceAtLeast(1)

        val startY = h * (1f - (data.first().throttle / 100f))
        path.moveTo(0f, startY)

        data.forEachIndexed { index, point ->
            val x = index * stepX
            val y = h * (1f - (point.throttle / 100f))
            path.lineTo(x, y)
        }
        
        // Draw Stroke
        canvas.drawPath(path, paint)
        
        // Draw Gradient Fill
        // Close the path to bottom for fill
        val fillPath = Path(path)
        fillPath.lineTo(w, h)
        fillPath.lineTo(0f, h)
        fillPath.close()
        
        paintFill1.shader = LinearGradient(0f, 0f, 0f, h, baseColor, Color.TRANSPARENT, Shader.TileMode.CLAMP)
        paintFill1.alpha = 50 // Semi-transparent
        canvas.drawPath(fillPath, paintFill1)
    }
}

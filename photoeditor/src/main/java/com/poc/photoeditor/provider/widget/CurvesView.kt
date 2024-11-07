package com.poc.photoeditor.provider.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.poc.photoeditor.provider.ui.model.CurveConfig

class CurvesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val circleRadius = 10f
    private val curveWidth = 1.5f
    private val horizontalGridWidth = 0.3f
    private val verticalGridWidth = 0.9f

    private var callback: CurveColorResponse? = null

    interface CurveColorResponse {
        fun onPointUpdate(points: ArrayList<PointF>, graphWidth: Float, graphHeight: Float)
        fun onInitialized(points: ArrayList<PointF>)
    }

    fun setupCallback(cb: CurveColorResponse) {
        callback = cb
    }

    fun setupCurve(config: CurveConfig) {
        controlPoints.clear()
        controlPoints.addAll(config.points)
         if (config.points.size == 2) {
            controlPoints.add(1, PointF(viewWidth / 2, viewHeight / 2))
        }
        invalidate()
    }

    fun getDefaultPoints() = if (controlPoints.isNotEmpty()) {
        arrayListOf(controlPoints.first(), controlPoints.last())
    } else arrayListOf()

    private val curvePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = curveWidth
    }

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.WHITE
        strokeWidth = curveWidth
    }

    private val gridHorizontalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.WHITE
        strokeWidth = horizontalGridWidth
    }

    private val gridVerticalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.WHITE
        strokeWidth = verticalGridWidth
    }
    private val path = Path()
    private val controlPoints = arrayListOf<PointF>()
    private var viewWidth = 0f
    private var viewHeight = 0f

    private fun drawGrid(canvas: Canvas) {
        val cellWidth = viewWidth / 3
        val cellHeight = viewHeight / 3

        // Draw vertical lines
        for (i in 0..3) {
            when (i) {
                0 -> {
                    canvas.drawLine(verticalGridWidth, 0f, verticalGridWidth, viewHeight, gridVerticalPaint)
                }
                3 -> {
                    canvas.drawLine(viewWidth - verticalGridWidth, 0f, viewWidth - verticalGridWidth, viewHeight, gridVerticalPaint)
                }
                else -> {
                    val x = cellWidth * i
                    canvas.drawLine(x, 0f, x, viewHeight, gridVerticalPaint)
                }
            }

        }// Draw horizontal lines
        for (i in 0..3) {
            when (i) {
                0 -> {
                    canvas.drawLine(0f, horizontalGridWidth, viewWidth, horizontalGridWidth, gridHorizontalPaint)
                }
                3 -> {
                    canvas.drawLine(0f, viewHeight - horizontalGridWidth, viewWidth, viewHeight - horizontalGridWidth, gridHorizontalPaint)
                }
                else -> {
                    val y = cellHeight * i
                    canvas.drawLine(0f, y, viewWidth, y, gridHorizontalPaint)
                }
            }
        }
    }

    private fun updateMeasuredSize() {
        if (viewWidth == 0f || viewHeight == 0f) {
            viewWidth = width.toFloat()
            viewHeight = height.toFloat()
            controlPoints.clear()
            controlPoints.add(PointF(circleRadius, viewHeight - circleRadius))
            controlPoints.add(PointF(viewWidth / 2, viewHeight / 2))
            controlPoints.add(PointF(viewWidth - circleRadius, 10f))
            callback?.onInitialized(controlPoints)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        updateMeasuredSize()

        drawGrid(canvas)

        path.reset()
        if (controlPoints.isNotEmpty()) {
            path.moveTo(controlPoints[0].x, controlPoints[0].y)
            canvas.drawCircle(controlPoints[0].x, controlPoints[0].y, circleRadius, circlePaint)
        }
        for (i in 1 until controlPoints.size) {
            val currentPoint = controlPoints[i]
            if (i >= controlPoints.size - 1) {
                canvas.drawCircle(currentPoint.x, currentPoint.y, circleRadius, circlePaint)
                break
            }

            val nextPoint = controlPoints[i + 1]
            path.quadTo(currentPoint.x, currentPoint.y, nextPoint.x, nextPoint.y)
        }
        canvas.drawPath(path, curvePaint)
    }

    private fun updateCenterPoint(x: Float, y: Float) {
        controlPoints.clear()
        controlPoints.add(PointF(circleRadius, viewHeight - circleRadius))
        controlPoints.add(PointF(x, y))
        controlPoints.add(PointF(viewWidth - circleRadius, 10f))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {

            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP -> {
                updateCenterPoint(x, y)

                callback?.onPointUpdate(controlPoints.clone() as ArrayList<PointF>, viewWidth, viewHeight)
            }
        }
        invalidate() // Redraw the view
        return true
    }
}
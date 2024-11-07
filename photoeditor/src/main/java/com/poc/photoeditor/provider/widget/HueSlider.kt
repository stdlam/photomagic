package com.poc.photoeditor.provider.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Shader
import android.util.AttributeSet
import ir.kotlin.kavehcolorpicker.R
import kotlin.math.floor

class HueSlider(context: Context, attributeSet: AttributeSet?) :
    BaseColorPicker(context, attributeSet) {

    constructor(context: Context) : this(context, null)

    private var hsvHolder = FloatArray(3)

    private lateinit var hueBitmapShader: BitmapShader


    /**
     * Bitmap that is 360 pixels and each pixels represents a degree in hue.
     */
    private val hueBitmap: Bitmap = BitmapFactory.decodeResource(
        resources,
        R.drawable.full_hue_bitmap,
        BitmapFactory.Options().apply {
            inScaled = false
        })

    private val hueMatrix = Matrix()

    private var onHueChanged: ((hue: Float, argbColor: Int) -> Unit)? = null
    private var onHueChangedListener: OnHueChangedListener? = null

    val hue: Float
        get() =
            hsvHolder[0]

    override fun onCirclePositionChanged(circlePositionX: Float, circlePositionY: Float) {
        callListeners(hsvHolder[0], calculateColorAt(circleX))

        invalidate()
    }

    private fun calculateColorAt(ex: Float): Int {
        // Closer the indicator (handle) to the end of view the higher is hue value.
        hsvHolder[0] =
            floor(360f * (ex - drawingStart) / (widthF - drawingStart))

        // Brightness and saturation is left untouched.
        hsvHolder[1] = 1f
        hsvHolder[2] = 1f

        return Color.HSVToColor(hsvHolder)
    }

    override fun initializeSliderPaint() {
        circleColor = Color.WHITE
        hueBitmapShader =
            BitmapShader(hueBitmap, Shader.TileMode.MIRROR, Shader.TileMode.REPEAT).apply {
                setLocalMatrix(hueMatrix.apply {
                    // Resize the bitmap to match width of hue bitmap (360) to whatever width of view is.
                    setTranslate(drawingStart, 0f)

                    postScale(
                        (widthF - drawingStart) / hueBitmap.width,
                        1f,
                        drawingStart,
                        0f
                    )
                })
            }

        linePaint.shader = hueBitmapShader
    }

    fun setOnHueChangedListener(onHueChangedListener: OnHueChangedListener) {
        this.onHueChangedListener = onHueChangedListener
    }

    fun setOnHueChangedListener(onHueChangedListener: ((hue: Float, argbColor: Int) -> Unit)) {
        onHueChanged = onHueChangedListener
    }

    private fun callListeners(hue: Float, argbColor: Int) {
        onHueChanged?.invoke(hue, argbColor)
        onHueChangedListener?.onHueChanged(hue, argbColor)
    }


    interface OnHueChangedListener {
        fun onHueChanged(hue: Float, argbColor: Int)
    }

}
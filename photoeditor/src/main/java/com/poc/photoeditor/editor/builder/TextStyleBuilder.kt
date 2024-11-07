package com.poc.photoeditor.editor.builder

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import com.poc.photoeditor.editor.model.TextBorder
import com.poc.photoeditor.editor.model.TextShadow
import com.poc.photoeditor.provider.ui.utils.ColorTransparentUtils
import java.io.File

open class TextStyleBuilder {
    val values = mutableMapOf<TextStyle, Any>()

    /**
     * Set this textSize style
     *
     * @param size Size to apply on text
     */
    fun withTextSize(size: Float) {
        values[TextStyle.SIZE] = size
    }

    /**
     * Set this textShadow style
     *
     * @param radius Radius of the shadow to apply on text
     * @param dx Horizontal distance of the shadow
     * @param dy Vertical distance of the shadow
     * @param color Color of the shadow
     */
    fun withTextShadow(radius: Float, dx: Float, dy: Float, color: Int) {
        val shadow = TextShadow(radius, dx, dy, color)
        withTextShadow(shadow)
    }

    /**
     * Set this color style
     *
     * @param color Color to apply on text
     */
    fun withTextColor(color: Pair<Int, Int>) {
        values[TextStyle.COLOR] = color
    }

    /**
     * Set this [Typeface] style
     *
     * @param textTypeface TypeFace to apply on text
     */
    fun withTextFont(textTypeface: Typeface) {
        values[TextStyle.FONT_FAMILY] = textTypeface
    }

    fun withTextFontFile(fontFile: File) {
        values[TextStyle.FONT_FAMILY] = fontFile
    }

    /**
     * Set this gravity style
     *
     * @param gravity Gravity style to apply on text
     */
    fun withGravity(gravity: Int) {
        values[TextStyle.GRAVITY] = gravity
    }

    /**
     * Set this background color
     *
     * @param background Background color to apply on text, this method overrides the preview set on [TextStyleBuilder.withBackgroundDrawable]
     */
    fun withBackgroundColor(background: Pair<Int, Int>) {
        values[TextStyle.BACKGROUND] = background
    }

    /**
     * Set this background [Drawable], this method overrides the preview set on [TextStyleBuilder.withBackgroundColor]
     *
     * @param bgDrawable Background drawable to apply on text
     */
    fun withBackgroundDrawable(bgDrawable: Drawable) {
        values[TextStyle.BACKGROUND] = bgDrawable
    }

    /**
     * Set this textAppearance style
     *
     * @param textAppearance Text style to apply on text
     */
    fun withTextAppearance(textAppearance: Int) {
        values[TextStyle.TEXT_APPEARANCE] =
            textAppearance
    }

    fun withTextStyle(typeface: Int) {
        values[TextStyle.TEXT_STYLE] = typeface
    }

    fun withTextStyle(typeface: Typeface) {
        values[TextStyle.TEXT_STYLE] = typeface
    }

    fun withTextFlag(paintFlag: Int) {
        values[TextStyle.TEXT_FLAG] = paintFlag
    }

    fun withTextShadow(textShadow: TextShadow) {
        values[TextStyle.SHADOW] = textShadow
    }

    fun withTextBorder(textBorder: TextBorder) {
        values[TextStyle.BORDER] = textBorder
    }

    fun withLetterSpacing(spacing: Int) {
        values[TextStyle.LETTER_SPACING] = spacing
    }

    fun withLineSpacing(spacing: Int) {
        values[TextStyle.LINE_SPACING] = spacing
    }

    /**
     * Method to apply all the style setup on this Builder}
     *
     * @param textView TextView to apply the style
     */
    fun applyStyle(textView: TextView) {
        for ((key, value) in values) {
            when (key) {
                TextStyle.SIZE -> {
                    val size = value as Float
                    applyTextSize(textView, size)
                }
                TextStyle.COLOR -> {
                    try {
                        val color = value as Pair<Int, Int>
                        val finalColor = if (color.first != -1) {
                            Color.parseColor(ColorTransparentUtils.transparentColor(color.first, color.second))
                        } else {
                            Color.parseColor(ColorTransparentUtils.transparentColor(color.first, 0))
                        }
                        applyTextColor(textView, finalColor)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                TextStyle.FONT_FAMILY -> {
                    if (value is File) {
                        applyFontFamily(textView, Typeface.createFromFile(value))
                    } else if (value is Typeface) {
                        applyFontFamily(textView, value)
                    }
                }
                TextStyle.GRAVITY -> {
                    val gravity = value as Int
                    applyGravity(textView, gravity)
                }
                TextStyle.BACKGROUND -> {
                    if (value is Drawable) {
                        applyBackgroundDrawable(textView, value)
                    } else if (value is Pair<*, *>) {
                        try {
                            val color = value as Pair<Int, Int>
                            if (color.first != -1) {
                                val finalColor = Color.parseColor(ColorTransparentUtils.transparentColor(color.first, color.second))
                                applyBackgroundColor(textView, finalColor)
                            } else {
                                applyBackgroundColor(textView, 0)
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                TextStyle.TEXT_APPEARANCE -> {
                    if (value is Int) {
                        applyTextAppearance(textView, value)
                    }
                }
                TextStyle.TEXT_STYLE -> {
                    if (value is Typeface) {
                        applyTextStyle(textView, value)
                    } else if (value is Int) {
                        applyTextStyle(textView, value)
                    }
                }
                TextStyle.TEXT_FLAG -> {
                    val flag = value as Int
                    applyTextFlag(textView, flag)
                }
                TextStyle.SHADOW -> {
                    run {
                        if (value is TextShadow) {
                            applyTextShadow(textView, value)
                        }
                    }
                    run {
                        if (value is TextBorder) {
                            applyTextBorder(textView, value)
                        }
                    }
                }
                TextStyle.BORDER -> {
                    if (value is TextBorder) {
                        applyTextBorder(textView, value)
                    }
                }

                TextStyle.LETTER_SPACING -> {
                    applyTextSpacing(textView, value as Int)
                }

                TextStyle.LINE_SPACING -> {
                    applyLineSpacing(textView, value as Int)
                }
            }
        }
    }

    protected open fun applyTextSize(textView: TextView, size: Float) {
        textView.textSize = size
    }

    protected fun applyTextShadow(
        textView: TextView,
        radius: Float,
        dx: Float,
        dy: Float,
        color: Int
    ) {
        textView.setShadowLayer(radius, dx, dy, color)
    }

    protected open fun applyTextColor(textView: TextView, color: Int) {
        textView.setTextColor(color)
    }

    protected open fun applyFontFamily(textView: TextView, typeface: Typeface?) {
        textView.typeface = typeface
    }

    protected open fun applyGravity(textView: TextView, gravity: Int) {
        textView.gravity = gravity
    }

    protected open fun applyBackgroundColor(textView: TextView, color: Int) {
        textView.setBackgroundColor(color)
    }

    protected open fun removeBackgroundColor(textView: TextView) {
        textView.background = null
    }

    protected open fun applyBackgroundDrawable(textView: TextView, bg: Drawable?) {
        textView.background = bg
    }

    // border
    protected open fun applyTextBorder(textView: TextView, textBorder: TextBorder) {
        try {
            val strokeColors = textBorder.strokeColor
            val transColor = if (strokeColors.first != -1) {
                Color.parseColor(ColorTransparentUtils.transparentColor(strokeColors.first, strokeColors.second))
            } else {
                Color.parseColor(ColorTransparentUtils.transparentColor(strokeColors.first, 0))
            }

            val gd = GradientDrawable()
            gd.cornerRadius = textBorder.corner
            gd.setStroke(textBorder.strokeWidth, transColor)
            textView.foreground = gd
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // shadow
    protected open fun applyTextShadow(textView: TextView, textShadow: TextShadow) {
        textView.setShadowLayer(textShadow.radius, textShadow.dx, textShadow.dy, textShadow.color)
    }

    // bold or italic
    protected open fun applyTextStyle(textView: TextView, typeface: Int) {
        textView.setTypeface(textView.typeface, typeface)
    }

    protected open fun applyTextStyle(textView: TextView, typeface: Typeface) {
        textView.setTypeface(typeface)
    }

    // underline or strike
    protected open fun applyTextFlag(textView: TextView, flag: Int) {
//        textView.setPaintFlags(textView.getPaintFlags()|flag);
        textView.paint.flags = flag
    }

    protected open fun applyTextAppearance(textView: TextView, styleAppearance: Int) {
        textView.setTextAppearance(styleAppearance)
    }

    protected open fun applyTextSpacing(textView: TextView, spacing: Int) {
        textView.letterSpacing = spacing.toFloat()
    }

    protected open fun applyLineSpacing(textView: TextView, spacing: Int) {
        textView.setLineSpacing(spacing.toFloat(), 1f)
    }

    /**
     * Enum to maintain current supported style properties used on on [PhotoEditor.addText] and [PhotoEditor.editText]
     */
    enum class TextStyle(val property: String) {
        SIZE("TextSize"),
        COLOR("TextColor"),
        GRAVITY("Gravity"),
        FONT_FAMILY("FontFamily"),
        BACKGROUND("Background"),
        TEXT_APPEARANCE("TextAppearance"),
        TEXT_STYLE("TextStyle"),
        TEXT_FLAG("TextFlag"),
        SHADOW("Shadow"),
        BORDER("Border"),
        LETTER_SPACING("LetterSpacing"),
        LINE_SPACING("LineSpacing");

    }
}
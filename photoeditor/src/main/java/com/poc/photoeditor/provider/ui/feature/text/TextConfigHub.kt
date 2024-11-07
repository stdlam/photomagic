package com.poc.photoeditor.provider.ui.feature.text

import android.graphics.Typeface
import com.poc.photoeditor.provider.ui.model.TextAlign
import java.io.File

interface TextConfigHub {
    fun onUpdateFont(font: File)
    fun onUpdateColor(color: Int, opacity: Int)
    fun onUpdateStrokeColor(color: Int, size: Int, opacity: Int)
    fun onUpdateBackgroundColor(color: Int, opacity: Int)
    fun onUpdateSpacing(align: TextAlign, characterSpace: Int, lineSpace: Int)
    fun getFont(): File?
    fun getTextColor(): Pair<Int, Int>?
    fun getStrokeColor(): Triple<Int, Int, Int>?
    fun getBackgroundColor(): Pair<Int, Int>?
    fun getGravity(): Triple<Int, Int, Int>
}
package com.poc.photoeditor.editor

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresPermission
import androidx.annotation.UiThread
import com.poc.photoeditor.editor.builder.TextStyleBuilder
import com.poc.photoeditor.editor.graphic.Graphic
import com.poc.photoeditor.editor.graphic.Text
import com.poc.photoeditor.editor.view.PhotoEditorView
import com.poc.photoeditor.editor.view.SaveSettings

interface PhotoEditor {

    /**
     * This add the text on the [PhotoEditorView] with provided parameters
     * by default [TextView.setText] will be 18sp
     *
     * @param text         text to display
     * @param styleBuilder text style builder with your style
     */
    @SuppressLint("ClickableViewAccessibility")
    fun addText(text: String, styleBuilder: TextStyleBuilder?, listener: Graphic.GraphicListener, isDuplicate: Boolean)

    /**
     * This will update the text and color on provided view
     *
     * @param view         root view where text view is a child
     * @param inputText    text to update [TextView]
     * @param styleBuilder style to apply on [TextView]
     */
    fun editText(text: Text, inputText: String, styleBuilder: TextStyleBuilder?)

    fun addToEditor(graphic: Graphic, isDuplicate: Boolean)

    /**
     * Remove all helper boxes from views
     */
    @UiThread
    fun clearHelperBox()

    /**
     * Removes all the edited operations performed [PhotoEditorView]
     * This will also clear the undo and redo stack
     */
    fun clearAllViews()


    /**
     * Setup of custom effect using effect type and set parameters values
     *
     * @param customEffect [CustomEffect.Builder.setParameter]
     */
    fun setFilterEffect(customEffect: CustomEffect?)

    /**
     * Set pre-define filter available
     *
     * @param filterType type of filter want to apply [PhotoEditorImpl]
     */
    fun setFilterEffect(filterType: PhotoFilter)

    /**
     * Save the edited image on given path
     *
     * @param imagePath      path on which image to be saved
     * @param saveSettings   builder for multiple save options [SaveSettings]
     */
    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    suspend fun saveAsFile(
        imagePath: String,
        saveSettings: SaveSettings = SaveSettings.Builder().build()
    ): SaveFileResult

    /**
     * Save the edited image as bitmap
     *
     * @param saveSettings builder for multiple save options [SaveSettings]
     */
    suspend fun saveAsBitmap(saveSettings: SaveSettings = SaveSettings.Builder().build()): Bitmap

    fun saveAsFile(imagePath: String, saveSettings: SaveSettings, onSaveListener: OnSaveListener)

    fun saveAsFile(imagePath: String, onSaveListener: OnSaveListener)

    fun saveAsBitmap(saveSettings: SaveSettings, onSaveBitmap: OnSaveBitmap)

    fun saveAsBitmap(onSaveBitmap: OnSaveBitmap)

    /**
     * Callback on editing operation perform on [PhotoEditorView]
     *
     * @param onPhotoEditorListener [OnPhotoEditorListener]
     */
    fun setOnPhotoEditorListener(onPhotoEditorListener: OnPhotoEditorListener)

    /**
     * Check if any changes made need to save
     *
     * @return true if nothing is there to change
     */
    val isCacheEmpty: Boolean

    /**
     * Builder pattern to define [PhotoEditor] Instance
     */
    class Builder(var context: Context, var photoEditorView: PhotoEditorView) {

        @JvmField
        var imageView: ImageView = photoEditorView.source

        @JvmField
        var deleteView: View? = null

        @JvmField
        var textTypeface: Typeface? = null

        // By default, pinch-to-scale is enabled for text
        @JvmField
        var isTextPinchScalable = true

        @JvmField
        var clipSourceImage = false
        fun setDeleteView(deleteView: View?): Builder {
            this.deleteView = deleteView
            return this
        }

        /**
         * set default text font to be added on image
         *
         * @param textTypeface typeface for custom font
         * @return [Builder] instant to build [PhotoEditor]
         */
        fun setDefaultTextTypeface(textTypeface: Typeface?): Builder {
            this.textTypeface = textTypeface
            return this
        }

        /**
         * Set false to disable pinch-to-scale for text inserts.
         * Set to "true" by default.
         *
         * @param isTextPinchScalable flag to make pinch to zoom for text inserts.
         * @return [Builder] instant to build [PhotoEditor]
         */
        fun setPinchTextScalable(isTextPinchScalable: Boolean): Builder {
            this.isTextPinchScalable = isTextPinchScalable
            return this
        }

        /**
         * @return build PhotoEditor instance
         */
        fun build(): PhotoEditor {
            return PhotoEditorImpl(this)
        }

        /**
         * Set true true to clip the drawing brush to the source image.
         *
         * @param clip a boolean to indicate if brush drawing is clipped or not.
         */
        fun setClipSourceImage(clip: Boolean): Builder {
            clipSourceImage = clip
            return this
        }

    }

    /**
     * A callback to save the edited image asynchronously
     */
    interface OnSaveListener {
        /**
         * Call when edited image is saved successfully on given path
         *
         * @param imagePath path on which image is saved
         */
        fun onSuccess(imagePath: String)

        /**
         * Call when failed to saved image on given path
         *
         * @param exception exception thrown while saving image
         */
        fun onFailure(exception: Exception)
    }
}
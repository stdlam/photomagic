package com.poc.photoeditor.editor.task

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import com.poc.photoeditor.editor.SaveFileResult
import com.poc.photoeditor.editor.helper.BitmapUtil.removeTransparency
import com.poc.photoeditor.editor.helper.BoxHelper
import com.poc.photoeditor.editor.view.PhotoEditorView
import com.poc.photoeditor.editor.view.SaveSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

internal class PhotoSaverTask(
    private val photoEditorView: PhotoEditorView,
    private val boxHelper: BoxHelper,
    private var saveSettings: SaveSettings
) {

    private fun onBeforeSaveImage() {
        boxHelper.clearHelperBox()
    }

    fun saveImageAsBitmap(): Bitmap {
        onBeforeSaveImage()
        val bitmap = buildBitmap()
        return bitmap
    }

    suspend fun saveImageAsFile(imagePath: String): SaveFileResult {
        onBeforeSaveImage()
        val capturedBitmap = buildBitmap()

        val result = withContext(Dispatchers.IO) {
            val file = File(imagePath)
            try {
                FileOutputStream(file, false).use { outputStream ->
                    capturedBitmap.compress(
                        saveSettings.compressFormat,
                        saveSettings.compressQuality,
                        outputStream
                    )
                    outputStream.flush()
                }

                SaveFileResult.Success
            } catch (e: IOException) {
                SaveFileResult.Failure(e)
            }
        }

        if (result is SaveFileResult.Success) {
            // Clear all views if it's enabled in save settings
            if (saveSettings.isClearViewsEnabled) {
                boxHelper.clearAllViews()
            }
        }

        return result
    }

    private fun buildBitmap(): Bitmap {
        return if (saveSettings.isTransparencyEnabled) {
            removeTransparency(captureView(photoEditorView))
        } else {
            captureView(photoEditorView)
        }
    }

    private fun captureView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    companion object {
        const val TAG = "PhotoSaverTask"
    }

}
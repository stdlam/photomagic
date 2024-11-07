package com.poc.photoeditor.provider

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import com.poc.photoeditor.provider.ui.PhotoActivity

class PhotoEditProvider(private val editLauncher: ActivityResultLauncher<Intent>, private val context: Context) {
    companion object {
        const val PHOTO_EDIT_PATH = "PHOTO_EDIT_PATH"
    }

    init {
        try {
            System.loadLibrary("NativeImageProcessor")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startEdit(photoPath: Uri) {
        editLauncher.launch(Intent(
            context, PhotoActivity::class.java
        ).apply {
            putExtra(PHOTO_EDIT_PATH, photoPath)
        })
    }

    private fun handleResult() {

    }

    private fun setResultCancel() {

    }
}


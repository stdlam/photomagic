package com.poc.photoeditor.editor

import android.graphics.Bitmap

interface OnSaveBitmap {
    fun onBitmapReady(saveBitmap: Bitmap)
}
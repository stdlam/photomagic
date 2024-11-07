package com.poc.photoeditor.provider.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.provider.MediaStore
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream


object BitmapUtils {
    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.JPEG, 100, bytes)

        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Temp", null)
        return Uri.parse(path.toString())
    }

    fun saveBitmapToUri(context: Context, bitmap: Bitmap, outputUri: Uri) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(CompressFormat.JPEG, 100, stream)
            inputStream = ByteArrayInputStream(stream.toByteArray())
            outputStream = context.contentResolver.openOutputStream(outputUri)
            inputStream.copyTo(outputStream!!)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    fun cornerPin(b: Bitmap, srcPoints: FloatArray?, dstPoints: FloatArray?): Bitmap {
        val w = b.width
        val h = b.height
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val c = Canvas(result)
        val m = Matrix()
        m.setPolyToPoly(srcPoints, 0, dstPoints, 0, 4)
        c.drawBitmap(b, m, p)
        return result
    }
}
package com.poc.photoeditor.editor

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import java.nio.IntBuffer
import javax.microedition.khronos.opengles.GL10

object GLToolbox {
    private fun loadShader(shaderType: Int, source: String): Int {
        val shader = GLES20.glCreateShader(shaderType)
        if (shader != 0) {
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                val info = GLES20.glGetShaderInfoLog(shader)
                GLES20.glDeleteShader(shader)
                throw RuntimeException("Could not compile shader $shaderType:$info")
            }
        }
        return shader
    }

    @JvmStatic
    fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return 0
        }
        val pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (pixelShader == 0) {
            return 0
        }
        val program = GLES20.glCreateProgram()
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader)
            checkGlError("glAttachShader")
            GLES20.glAttachShader(program, pixelShader)
            checkGlError("glAttachShader")
            GLES20.glLinkProgram(program)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(
                program, GLES20.GL_LINK_STATUS, linkStatus,
                0
            )
            if (linkStatus[0] != GLES20.GL_TRUE) {
                val info = GLES20.glGetProgramInfoLog(program)
                GLES20.glDeleteProgram(program)
                throw RuntimeException("Could not link program: $info")
            }
        }
        return program
    }

    @JvmStatic
    fun checkGlError(op: String) {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            throw RuntimeException("$op: glError $error")
        }
    }

    @JvmStatic
    fun initTexParams() {
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
    }

    /**
     * Save filter bitmap from [ImageFilterView]
     *
     * @param glSurfaceView surface view on which is image is drawn
     * @param gl            open gl source to read pixels from [GLSurfaceView]
     * @return save bitmap
     * @throws OutOfMemoryError error when system is out of memory to load and save bitmap
     */
    @Throws(OutOfMemoryError::class)
    fun createBitmapFromGLSurface(glSurfaceView: GLSurfaceView, gl: GL10): Bitmap {
        val x = 0
        val y = 0
        val w = glSurfaceView.width
        val h = glSurfaceView.height
        val bitmapBuffer = IntArray(w * h)
        val bitmapSource = IntArray(w * h)
        val intBuffer = IntBuffer.wrap(bitmapBuffer)
        intBuffer.position(0)

        gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer)
        var offset1: Int
        var offset2: Int
        for (i in 0 until h) {
            offset1 = i * w
            offset2 = (h - i - 1) * w
            for (j in 0 until w) {
                val texturePixel = bitmapBuffer[offset1 + j]
                val blue = texturePixel shr 16 and 0xff
                val red = texturePixel shl 16 and 0x00ff0000
                val pixel = texturePixel and -0xff0100 or red or blue
                bitmapSource[offset2 + j] = pixel
            }
        }

        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888)
    }
}
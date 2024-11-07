package com.poc.photoeditor.editor

import java.io.IOException

sealed interface SaveFileResult {
    object Success : SaveFileResult
    class Failure(val exception: IOException) : SaveFileResult
}
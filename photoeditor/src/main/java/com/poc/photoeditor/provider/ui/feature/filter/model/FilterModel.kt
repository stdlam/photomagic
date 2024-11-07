package com.poc.photoeditor.provider.ui.feature.filter.model

import android.graphics.Bitmap
import com.poc.photoeditor.editor.PhotoFilter

data class FilterModel(
    val name: String,
    val filter: PhotoFilter,
    var bitmap: Bitmap? = null,
    var selected: Boolean
)

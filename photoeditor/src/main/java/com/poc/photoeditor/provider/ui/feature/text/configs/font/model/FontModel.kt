package com.poc.photoeditor.provider.ui.feature.text.configs.font.model

import java.io.File

data class FontModel(
    val name: String,
    val fontFile: File,
    val fontColor: String,
    var selected: Boolean
)

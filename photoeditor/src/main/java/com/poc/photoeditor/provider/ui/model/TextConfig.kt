package com.poc.photoeditor.provider.ui.model

import com.poc.photoeditor.editor.builder.TextStyleBuilder

data class TextConfig(
    var text: String,
    var textStyleBuilder: TextStyleBuilder?
)

enum class TextAlign {
    LEFT,
    MIDDLE,
    RIGHT
}

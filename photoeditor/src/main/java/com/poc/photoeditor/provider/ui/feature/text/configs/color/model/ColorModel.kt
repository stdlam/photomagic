package com.poc.photoeditor.provider.ui.feature.text.configs.color.model

data class ColorModel(
    val icon: Int? = null,
    var color: Int? = null,
    val type: ColorType = ColorType.COLOR,
    var selected: Boolean
)

enum class ColorType {
    NONE,
    PICKER,
    COLOR
}

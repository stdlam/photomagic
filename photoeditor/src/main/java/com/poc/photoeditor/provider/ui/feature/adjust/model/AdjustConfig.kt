package com.poc.photoeditor.provider.ui.feature.adjust.model

import android.graphics.Color
import com.poc.photoeditor.provider.ui.feature.adjust.hsl.model.HSLModel

data class AdjustConfig(
    var brightness: Int = 50,
    var constrast: Int = 50,
    var warmth: Int = 50,
    var tint: Int = 50,
    var showdow: Int = 50,
    var hslModel: HSLModel = HSLModel(
        0,
        0,
        0,
        Color.parseColor(
            "#FF3938"
        )
    )
)

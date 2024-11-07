package com.poc.photoeditor.provider.ui

import com.poc.photoeditor.provider.ui.feature.adjust.hsl.model.HSLModel

interface AdjustCommunity {
    fun applyBrightness(
        value: Int,
        doneDeal: Boolean,
        discard: Boolean
    )

    fun applyContrast(
        value: Int,
        doneDeal: Boolean,
        discard: Boolean
    )

    fun applyWarmth(
        value: Int,
        doneDeal: Boolean,
        discard: Boolean
    )

    fun applyTint(
        value: Int,
        doneDeal: Boolean,
        discard: Boolean
    )

    fun applyShadow(
        value: Int,
        doneDeal: Boolean,
        discard: Boolean
    )

    fun applyHLS(
        value: HSLModel,
        doneDeal: Boolean,
        discard: Boolean
    )

}
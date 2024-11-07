package com.poc.photoeditor.provider.ui.model

data class ConfigUIModel(
    val id: Int,
    val icon: Int?,
    val name: String,
    var selected: Boolean,
    val color: Int? = null
)

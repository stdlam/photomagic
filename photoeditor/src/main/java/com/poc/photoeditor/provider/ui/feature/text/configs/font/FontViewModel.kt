package com.poc.photoeditor.provider.ui.feature.text.configs.font

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poc.photoeditor.provider.ui.feature.text.configs.font.model.FontFilterModel
import com.poc.photoeditor.provider.ui.feature.text.configs.font.model.FontModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FontViewModel : ViewModel() {
    companion object {
        private const val UPPERCASE_CHARS = "QWERTYUIOPASDFGHJKLZXCVBNM"
        private const val LOWERCASE_CHARS = "qwertyuiopasdfghjklzxcvbnm"
    }
    private val deviceFonts = arrayListOf<FontModel>()
    private val _state = MutableStateFlow<ViewEvent>(ViewEvent.DeviceFont(arrayListOf()))
    val state = _state.asStateFlow()

    sealed class ViewEvent {
        data class DeviceFont(val fonts: ArrayList<FontModel>) : ViewEvent()

        data class FontFilters(val data: ArrayList<FontFilterModel>) : ViewEvent()
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getAllFonts()?.forEachIndexed { index, font ->
                Log.d("Font", "getAllFonts ${font}")
                deviceFonts.add(
                    FontModel(
                        font.nameWithoutExtension,
                        font,
                        "#706E6E",
                        index == 0
                    )
                )
            }
            val categories = groupFontName()
            withContext(Dispatchers.Main) {
                _state.emit(ViewEvent.FontFilters(categories))
                _state.emit(ViewEvent.DeviceFont(deviceFonts))
            }
        }
    }

    fun unselectOldFont() {
        deviceFonts.firstOrNull{ it.selected }?.selected = false
    }

    fun filter(name: String) {
        viewModelScope.launch {
            when (name) {
                "All" -> {
                    _state.emit(ViewEvent.DeviceFont(deviceFonts))
                }
                "Basic" -> {
                    _state.emit(ViewEvent.DeviceFont(
                        ArrayList(
                            deviceFonts.filter { !it.name.contains("-") }
                    )))
                }
                else -> {
                    _state.emit(ViewEvent.DeviceFont(
                        ArrayList(
                            deviceFonts.filter { it.name.contains("$name-") }
                        )))
                }
            }
        }
    }

    private fun getAllFonts(): Array<out File>? {
        val path = "/system/fonts"
        val folder = File(path)
        return folder.listFiles()
    }

    private fun groupFontName(): ArrayList<FontFilterModel> {
        val result = arrayListOf(
            FontFilterModel("All", true),
            FontFilterModel("Basic", false)
        )
        deviceFonts.forEach { font ->
            val splitting = font.name.split("-")
            if (splitting.size > 1) {
                var filterName = ""
                val reversed = splitting[0].reversed()
                var isEndOfUpper = false
                for (i in reversed.indices) {
                    val char = reversed[i]
                    if (LOWERCASE_CHARS.contains(char) && isEndOfUpper) {
                        filterName = filterName.reversed()
                        break
                    }
                    filterName += char
                    if (UPPERCASE_CHARS.contains(char)) {
                        if (i == reversed.length - 1) {
                            filterName = filterName.reversed()
                            break
                        }

                        isEndOfUpper= true
                    }
                }

                val existedFilterName = result.map { it.name }
                if (!existedFilterName.contains(filterName)) {
                    result.add(FontFilterModel(filterName, result.size == 0))
                }
            }
        }

        return result
    }
}
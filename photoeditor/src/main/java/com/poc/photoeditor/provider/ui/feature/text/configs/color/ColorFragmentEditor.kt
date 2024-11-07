package com.poc.photoeditor.provider.ui.feature.text.configs.color

import android.annotation.SuppressLint
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.FragmentColorBinding
import com.poc.photoeditor.provider.ui.base.EditorBaseFragment
import com.poc.photoeditor.provider.ui.feature.text.configs.color.model.ColorType
import com.poc.photoeditor.provider.ui.utils.InflateAlias
import com.poc.photoeditor.provider.widget.StartSpaceItemDecoration

class ColorFragmentEditor : EditorBaseFragment<FragmentColorBinding>() {
    override val bindingInflater: InflateAlias<FragmentColorBinding>
        get() = FragmentColorBinding::inflate

    private var selectedColor = -1

    private val colorAdapter = ColorAdapter {
        when (it.type) {
            ColorType.PICKER -> {
                ColorPickerBottomSheetDialog().showDialog(childFragmentManager, object : ColorPickerBottomSheetDialog.Callback {
                    override fun onPickedColor(color: Int) {
                        selectedColor = color
                        getBackResult()

                    }
                })
            }
            ColorType.COLOR -> {
                selectedColor = it.color ?: -1
                getBackResult()
            }
            else -> {
                selectedColor = -1
                getBackResult()
            }
        }
    }

    private fun getBackResult() {
        try {
            binding?.sbOpacity?.progress?.let { alpha ->
                getTextBottomSheetDialog()?.onUpdateColor(
                    selectedColor,
                    alpha
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        binding?.root?.requestLayout()
    }

    private fun setupConfig() {
        getTextBottomSheetDialog()?.getTextColor()?.let { colors ->
            colorAdapter.updateSelectedColor(colors.first)
            binding?.sbOpacity?.progress = colors.second
            binding?.tvOpacityValue?.text = "${colors.second}%"
        }
    }

    override fun setupViews() {
        binding?.rvColors?.run {
            adapter = colorAdapter
            addItemDecoration(StartSpaceItemDecoration(context, R.dimen.long_8))
        }

        binding?.sbOpacity?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding?.tvOpacityValue?.text = "$progress%"
                getBackResult()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        setupConfig()
        colorAdapter.getSelectedColor()?.let {
            selectedColor = it
            getBackResult()
        }
    }
}
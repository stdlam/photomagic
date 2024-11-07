package com.poc.photoeditor.provider.ui.feature.text.configs.background

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.res.ResourcesCompat
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.FragmentBackgroundBinding
import com.poc.photoeditor.provider.ui.base.EditorBaseFragment
import com.poc.photoeditor.provider.ui.feature.text.configs.color.ColorAdapter
import com.poc.photoeditor.provider.ui.feature.text.configs.color.ColorPickerBottomSheetDialog
import com.poc.photoeditor.provider.ui.feature.text.configs.color.model.ColorType
import com.poc.photoeditor.provider.ui.utils.InflateAlias
import com.poc.photoeditor.provider.widget.StartSpaceItemDecoration

class BackgroundFragmentEditor : EditorBaseFragment<FragmentBackgroundBinding>() {
    override val bindingInflater: InflateAlias<FragmentBackgroundBinding>
        get() = FragmentBackgroundBinding::inflate

    private var selectedColor = -1

    private val colorAdapter = ColorAdapter(true) {
        if (it.type == ColorType.NONE) {
            setupNonColorViews()
            selectedColor = -1
            getBackResult()
        } else if (it.type == ColorType.PICKER) {
            ColorPickerBottomSheetDialog().showDialog(childFragmentManager, object : ColorPickerBottomSheetDialog.Callback {
                override fun onPickedColor(color: Int) {
                    selectedColor = color
                    getBackResult()
                }
            })
        } else {
            setupColorViews()
            selectedColor = it.color ?: 0
            getBackResult()
        }
    }

    private fun getBackResult() {
        binding?.sbOpacity?.progress?.let { alpha ->
            getTextBottomSheetDialog()?.onUpdateBackgroundColor(
                selectedColor,
                alpha
            )
        }

    }

    override fun onResume() {
        super.onResume()
        binding?.root?.requestLayout()
    }

    private fun setupConfig() {
        getTextBottomSheetDialog()?.getBackgroundColor()?.let { colors ->
            if (colors.first != -1) {
                setupColorViews()
                colorAdapter.updateSelectedColor(colors.first)
                binding?.sbOpacity?.progress = colors.second
                binding?.tvOpacityValue?.text = "${colors.second}%"
            }
        }
    }

    private fun setupNonColorViews() {
        val disableColor = ResourcesCompat.getColor(resources, R.color.disable_grey, null)
        binding?.tvOpacity?.setTextColor(disableColor)
        binding?.tvOpacityValue?.setTextColor(disableColor)
        binding?.sbOpacity?.thumbTintList = ColorStateList.valueOf(disableColor)
        binding?.sbOpacity?.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_disable_progress, null)
        binding?.sbOpacity?.isEnabled = false
    }

    private fun setupColorViews() {
        val enableColor = ResourcesCompat.getColor(resources, R.color.white, null)
        binding?.tvOpacity?.setTextColor(enableColor)
        binding?.tvOpacityValue?.setTextColor(enableColor)
        binding?.sbOpacity?.thumbTintList = ColorStateList.valueOf(enableColor)
        binding?.sbOpacity?.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_green_progress, null)
        binding?.sbOpacity?.isEnabled = true
    }

    override fun setupViews() {
        super.setupViews()
        setupNonColorViews()
        setupConfig()
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
    }
}
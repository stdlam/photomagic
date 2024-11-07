package com.poc.photoeditor.provider.ui.feature.text.configs.strokecolor

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.res.ResourcesCompat
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.FragmentStrokeColorBinding
import com.poc.photoeditor.provider.ui.base.EditorBaseFragment
import com.poc.photoeditor.provider.ui.feature.text.configs.color.ColorAdapter
import com.poc.photoeditor.provider.ui.feature.text.configs.color.ColorPickerBottomSheetDialog
import com.poc.photoeditor.provider.ui.feature.text.configs.color.model.ColorType
import com.poc.photoeditor.provider.ui.utils.InflateAlias
import com.poc.photoeditor.provider.widget.StartSpaceItemDecoration

class StrokeColorFragmentEditor : EditorBaseFragment<FragmentStrokeColorBinding>() {
    override val bindingInflater: InflateAlias<FragmentStrokeColorBinding>
        get() = FragmentStrokeColorBinding::inflate

    private var selectedColor = -1

    private val colorAdapter = ColorAdapter(true) {
        if (it.type == ColorType.NONE) {
            setupNonColorViews()
            selectedColor = -1
            getBackResult()
        } else {
            setupColorViews()
            if (it.type == ColorType.PICKER) {
                ColorPickerBottomSheetDialog().showDialog(childFragmentManager, object : ColorPickerBottomSheetDialog.Callback {
                    override fun onPickedColor(color: Int) {
                        selectedColor = color
                        getBackResult()
                    }
                })
            } else if (it.type == ColorType.COLOR) {
                selectedColor = it.color ?: -1
                getBackResult()
            }
        }
    }

    private fun setupConfig() {
        getTextBottomSheetDialog()?.getStrokeColor()?.let { colors ->
            colorAdapter.updateSelectedColor(colors.first)
            binding?.sbOpacity?.progress = colors.second
            binding?.tvOpacityValue?.text = "${colors.second}%"
            binding?.sbSize?.progress = colors.third
            binding?.tvSizeValue?.text = "${colors.third}%"
            if (colors.first != -1) {
                setupColorViews()
            } else {
                setupNonColorViews()
            }
        }
    }

    private fun getBackResult() {
        getTextBottomSheetDialog()?.onUpdateStrokeColor(
            selectedColor,
            binding?.sbSize?.progress ?: 0,
            binding?.sbOpacity?.progress ?: 0
        )
    }

    override fun onResume() {
        super.onResume()
        binding?.root?.requestLayout()
    }

    private fun setupNonColorViews() {
        val disableColor = ResourcesCompat.getColor(resources, R.color.disable_grey, null)
        binding?.tvOpacity?.setTextColor(disableColor)
        binding?.tvOpacityValue?.setTextColor(disableColor)
        binding?.sbOpacity?.thumbTintList = ColorStateList.valueOf(disableColor)
        binding?.sbOpacity?.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_disable_progress, null)
        binding?.sbOpacity?.isEnabled = false

        binding?.tvSize?.setTextColor(disableColor)
        binding?.tvSizeValue?.setTextColor(disableColor)
        binding?.sbSize?.thumbTintList = ColorStateList.valueOf(disableColor)
        binding?.sbSize?.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_disable_progress, null)
        binding?.sbSize?.isEnabled = false
    }

    private fun setupColorViews() {
        val enableColor = ResourcesCompat.getColor(resources, R.color.white, null)
        binding?.tvOpacity?.setTextColor(enableColor)
        binding?.tvOpacityValue?.setTextColor(enableColor)
        binding?.sbOpacity?.thumbTintList = ColorStateList.valueOf(enableColor)
        binding?.sbOpacity?.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_green_progress, null)
        binding?.sbOpacity?.isEnabled = true

        binding?.tvSize?.setTextColor(enableColor)
        binding?.tvSizeValue?.setTextColor(enableColor)
        binding?.sbSize?.thumbTintList = ColorStateList.valueOf(enableColor)
        binding?.sbSize?.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_green_progress, null)
        binding?.sbSize?.isEnabled = true
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

        binding?.sbSize?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding?.tvSizeValue?.text = "$progress%"
                getBackResult()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }
}
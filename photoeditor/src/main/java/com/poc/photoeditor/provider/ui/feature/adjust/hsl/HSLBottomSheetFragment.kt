package com.poc.photoeditor.provider.ui.feature.adjust.hsl

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.FragmentManager
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.DialogHslBinding
import com.poc.photoeditor.provider.ui.DiscardConfirmBottomSheetDialog
import com.poc.photoeditor.provider.ui.base.BaseBottomSheetDialogFragment
import com.poc.photoeditor.provider.ui.feature.adjust.hsl.model.HSLModel
import com.poc.photoeditor.provider.ui.feature.text.configs.color.ColorAdapter
import com.poc.photoeditor.provider.ui.feature.text.configs.color.ColorPickerBottomSheetDialog
import com.poc.photoeditor.provider.ui.feature.text.configs.color.model.ColorType
import com.poc.photoeditor.provider.ui.utils.BlurHelper
import com.poc.photoeditor.provider.ui.utils.InflateFragmentAlias
import com.poc.photoeditor.provider.widget.StartSpaceItemDecoration

class HSLBottomSheetFragment(
    private val hsl: HSLModel,
    private val callback: ((HSLModel, Boolean, Boolean) -> Unit)? = null
) : BaseBottomSheetDialogFragment<DialogHslBinding>() {
    override val mInflater: InflateFragmentAlias<DialogHslBinding>
        get() = DialogHslBinding::inflate

    override fun getTheme(): Int {
        return R.style.TransparentRadius0BottomSheetBackgroundDialog
    }

    private var tmpHSL = hsl.copy()

    private val colorAdapter = ColorAdapter {
        when (it.type) {
            ColorType.PICKER -> {
                ColorPickerBottomSheetDialog().showDialog(childFragmentManager, object : ColorPickerBottomSheetDialog.Callback {
                    override fun onPickedColor(color: Int) {
                        tmpHSL.baseColor = color
                        getBackResult()
                    }
                })
            }
            ColorType.COLOR -> {
                tmpHSL.baseColor = it.color ?: -1
                getBackResult()
            }
            else -> {
                tmpHSL.baseColor = -1
                getBackResult()
            }
        }
    }

    private fun getBackResult(isFinal: Boolean = false, isDiscard: Boolean = false) {
        callback?.invoke(
            if (isDiscard) hsl else tmpHSL,
            isFinal,
            isDiscard
        )
    }

    private fun setupSeekbars() {
        binding.tvHueValue.text = tmpHSL.hue.toString()
        binding.sbHue.progress = tmpHSL.hue + 100

        binding.tvSaturationValue.text = tmpHSL.saturation.toString()
        binding.sbSaturation.progress = tmpHSL.saturation + 100

        binding.tvLuminanceValue.text = tmpHSL.luminance.toString()
        binding.sbLuminance.progress = tmpHSL.luminance + 100

        binding.sbHue.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvHueValue.text = "${progress - 100}"
                tmpHSL.hue = progress - 100
                getBackResult()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding.sbSaturation.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvSaturationValue.text = "${progress - 100}"
                tmpHSL.saturation = progress - 100
                getBackResult()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding.sbLuminance.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvLuminanceValue.text = "${progress - 100}"
                tmpHSL.luminance = progress - 100
                getBackResult()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }

    fun showDialog(fragmentManager: FragmentManager) {
        try {
            val tagDialog = DialogHslBinding::class.java.name
            val fragment = fragmentManager.findFragmentByTag(tagDialog)
            if (fragment == null) {
                showNow(fragmentManager, tagDialog)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupColors() {
        binding.rvColors.run {
            adapter = colorAdapter
            addItemDecoration(StartSpaceItemDecoration(context, R.dimen.long_8))
        }

        colorAdapter.updateSelectedColor(hsl.baseColor)
    }

    override fun setupUI(binding: DialogHslBinding) {
        dialog?.run {
            setCanceledOnTouchOutside(false)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setDimAmount(0.6f)
        }

        setupSeekbars()
        setupColors()

        binding.ivCheck.setOnClickListener {
            getBackResult(true)
            dismiss()
        }

        binding.ivClose.setOnClickListener {
            if (checkIfConfigDifference()) {
                DiscardConfirmBottomSheetDialog().showDialog(
                    childFragmentManager,
                    object : DiscardConfirmBottomSheetDialog.Callback {
                        override fun onShowed() {
                            updateBlurBackground(true)
                        }

                        override fun onCancel() {
                            updateBlurBackground(false)
                        }

                        override fun onDiscard() {
                            discard()
                            updateBlurBackground(false)
                            dismiss()
                        }

                    }
                )
            } else {
                dismiss()
            }
        }
    }

    private fun discard() {
        getBackResult(isFinal = true, isDiscard = true)
    }

    private fun checkIfConfigDifference(): Boolean {
        return tmpHSL != hsl
    }

    private fun updateBlurBackground(isBlur: Boolean) {
        if (isBlur) {
            val bitmap = BlurHelper.takeScreenShot(dialog?.ownerActivity?.window)
            val blurredBitmap = BlurHelper.fastBlur(bitmap, 20)
            val blurDrawable = BitmapDrawable(resources, blurredBitmap)
            dialog?.window?.setBackgroundDrawable(blurDrawable)
        } else {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

    }

}
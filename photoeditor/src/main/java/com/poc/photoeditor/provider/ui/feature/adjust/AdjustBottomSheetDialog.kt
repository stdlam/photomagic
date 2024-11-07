package com.poc.photoeditor.provider.ui.feature.adjust

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.DialogAdjustBinding
import com.poc.photoeditor.provider.ui.DiscardConfirmBottomSheetDialog
import com.poc.photoeditor.provider.ui.base.BaseBottomSheetDialogFragment
import com.poc.photoeditor.provider.ui.feature.adjust.curve.CurveBottomSheetFragment
import com.poc.photoeditor.provider.ui.feature.adjust.hsl.HSLBottomSheetFragment
import com.poc.photoeditor.provider.ui.feature.adjust.model.AdjustConfig
import com.poc.photoeditor.provider.ui.feature.text.configs.ConfigsAdapter
import com.poc.photoeditor.provider.ui.model.CurveConfig
import com.poc.photoeditor.provider.ui.model.ConfigUIModel
import com.poc.photoeditor.provider.ui.utils.BlurHelper
import com.poc.photoeditor.provider.ui.utils.InflateFragmentAlias
import com.poc.photoeditor.provider.widget.StartSpaceItemDecoration

class AdjustBottomSheetDialog(
    private val curveConfigs: ArrayList<CurveConfig>? = null,
    private val adjustConfig: AdjustConfig
) : BaseBottomSheetDialogFragment<DialogAdjustBinding>() {
    override val mInflater: InflateFragmentAlias<DialogAdjustBinding>
        get() = DialogAdjustBinding::inflate

    companion object {
        private const val CURVE = 0
        const val BRIGHTNESS = 1
        const val CONTRAST = 2
        const val WARMTH = 3
        const val TINT = 4
        const val HSL = 5
        const val SHADOW = 6
    }

    interface Callback {
        fun applyCurve(curveConfigs: ArrayList<CurveConfig>?, width: Float, height: Float, doneDeal: Boolean)
        fun applyAdjust(adjust: AdjustConfig, id: Int, doneDeal: Boolean, discard: Boolean)
    }

    override fun getTheme(): Int {
        return R.style.TransparentRadius0BottomSheetBackgroundDialog
    }

    private var tmpCurveConfig: ArrayList<CurveConfig>? = curveConfigs
    private var currentAdjustId: Int = 0
    private var tmpAdjust = adjustConfig.copy()
    private var adjustProgression = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            when (currentAdjustId) {
                BRIGHTNESS -> {
                    tmpAdjust.brightness = progress
                    callback?.applyAdjust(
                        tmpAdjust,
                        currentAdjustId,
                        doneDeal = false,
                        discard = false
                    )
                }

                CONTRAST -> {
                    tmpAdjust.constrast = progress
                    callback?.applyAdjust(
                        tmpAdjust,
                        currentAdjustId,
                        doneDeal = false,
                        discard = false
                    )
                }

                WARMTH -> {
                    tmpAdjust.warmth = progress
                    callback?.applyAdjust(
                        tmpAdjust,
                        currentAdjustId,
                        doneDeal = false,
                        discard = false
                    )
                }

                TINT -> {
                    tmpAdjust.tint = progress
                    callback?.applyAdjust(
                        tmpAdjust,
                        currentAdjustId,
                        doneDeal = false,
                        discard = false
                    )
                }

                SHADOW -> {
                    tmpAdjust.showdow = progress
                    callback?.applyAdjust(
                        tmpAdjust,
                        currentAdjustId,
                        doneDeal = false,
                        discard = false
                    )
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }

    }

    private val configData = arrayListOf(
        ConfigUIModel(CURVE, R.drawable.ic_curve, "Curve", false),
        ConfigUIModel(BRIGHTNESS, R.drawable.ic_brightness, "Brightness", false),
        ConfigUIModel(CONTRAST, R.drawable.ic_contrast, "Contrast", false),
        ConfigUIModel(WARMTH, R.drawable.ic_warmth, "Warmth", false),
        ConfigUIModel(TINT, R.drawable.ic_tint, "Tint", false),
        ConfigUIModel(HSL, R.drawable.ic_hsl, "HSL", false),
        ConfigUIModel(SHADOW, R.drawable.ic_shadow, "Shadow", false)
    )

    private var callback: Callback? = null
    private val configsAdapter = ConfigsAdapter {
        currentAdjustId = it.id
        setupSeekbar()
        when (it.id) {
            CURVE -> {
                binding.root.visibility = View.GONE
                CurveBottomSheetFragment(tmpCurveConfig) { selectedCurve, hideCurveFragment, curveWidth, curveHeight ->
                    if (hideCurveFragment) {
                        binding.root.visibility = View.VISIBLE
                    }
                    tmpCurveConfig = selectedCurve
                    callback?.applyCurve(selectedCurve, curveWidth, curveHeight, hideCurveFragment)
                }.showDialog(childFragmentManager)
                binding.sbConfig.visibility = View.INVISIBLE
            }

            BRIGHTNESS -> {
                binding.sbConfig.visibility = View.VISIBLE
                binding.sbConfig.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_green_progress, null)
            }

            CONTRAST -> {
                binding.sbConfig.visibility = View.VISIBLE
                binding.sbConfig.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_green_progress, null)
            }

            WARMTH -> {
                binding.sbConfig.visibility = View.VISIBLE
                binding.sbConfig.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_purple_to_yellow_progress, null)
            }

            TINT -> {
                binding.sbConfig.visibility = View.VISIBLE
                binding.sbConfig.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_green_to_pink_progress, null)
            }

            SHADOW -> {
                binding.sbConfig.visibility = View.VISIBLE
                binding.sbConfig.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_purple_to_yellow_progress, null)
            }

            HSL -> {
                binding.sbConfig.visibility = View.GONE
                HSLBottomSheetFragment(adjustConfig.hslModel) { result, shouldSave, isDiscard ->
                    tmpAdjust.hslModel = result
                    callback?.applyAdjust(tmpAdjust, currentAdjustId, doneDeal = shouldSave, discard = isDiscard)
                }.showDialog(childFragmentManager)
            }
        }
    }

    fun showDialog(fragmentManager: FragmentManager, callback: Callback) {
        this.callback = callback
        try {
            val tagDialog = DialogAdjustBinding::class.java.name
            val fragment = fragmentManager.findFragmentByTag(tagDialog)
            if (fragment == null) {
                showNow(fragmentManager, tagDialog)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupSeekbar() {
        // reset callback
        binding.sbConfig.setOnSeekBarChangeListener(null)
        when (currentAdjustId) {
            BRIGHTNESS -> {
                binding.sbConfig.progress = tmpAdjust.brightness
            }

            CONTRAST -> {
                binding.sbConfig.progress = tmpAdjust.constrast
            }

            WARMTH -> {
                binding.sbConfig.progress = tmpAdjust.warmth
            }

            TINT -> {
                binding.sbConfig.progress = tmpAdjust.tint
            }

            SHADOW -> {
                binding.sbConfig.progress = tmpAdjust.showdow
            }
        }
        binding.sbConfig.setOnSeekBarChangeListener(adjustProgression)
    }

    private fun setupAdjustConfig() {
        binding.rvConfigs.run {
            addItemDecoration(StartSpaceItemDecoration(context, R.dimen.long_24))
            adapter = configsAdapter
            configsAdapter.submitData(configData)
        }

    }

    override fun setupUI(binding: DialogAdjustBinding) {
        dialog?.run {
            setCanceledOnTouchOutside(false)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setDimAmount(0.6f)
        }

        setupAdjustConfig()

        binding.ivCheck.setOnClickListener {
            callback?.applyAdjust(tmpAdjust, currentAdjustId, doneDeal = true, discard = false)
            dismiss()
        }

        binding.ivClose.setOnClickListener {
            if (checkIfDConfigDifference()) {
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
        callback?.applyAdjust(adjustConfig, currentAdjustId, doneDeal = true, discard = true)
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

    private fun checkIfDConfigDifference(): Boolean {
        return adjustConfig.brightness != tmpAdjust.brightness ||
                adjustConfig.warmth != tmpAdjust.warmth ||
                adjustConfig.showdow != tmpAdjust.showdow ||
                adjustConfig.tint != tmpAdjust.tint ||
                adjustConfig.constrast != tmpAdjust.constrast ||
                adjustConfig.hslModel != tmpAdjust.hslModel
    }
}
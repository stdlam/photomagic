package com.poc.photoeditor.provider.ui.feature.text.configs.color

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.fragment.app.FragmentManager
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.DialogColorPickerBinding
import com.poc.photoeditor.provider.ui.base.BaseBottomSheetDialogFragment
import com.poc.photoeditor.provider.ui.utils.InflateFragmentAlias
import com.poc.photoeditor.provider.widget.StartSpaceItemDecoration

class ColorPickerBottomSheetDialog : BaseBottomSheetDialogFragment<DialogColorPickerBinding>() {
    interface Callback {
        fun onPickedColor(color: Int)
    }

    override val mInflater: InflateFragmentAlias<DialogColorPickerBinding>
        get() = DialogColorPickerBinding::inflate

    private var callback: Callback? = null

    private val colorAdapter = ColorAdapter(includePicker = false) {

    }

    override fun getTheme(): Int {
        return R.style.Radius40BottomSheetBackgroundDialog
    }

    private fun setupConfiguration() {
        binding.rvColors.run {
            adapter = colorAdapter
            addItemDecoration(StartSpaceItemDecoration(context, R.dimen.long_8))
        }
        binding.rcpView.circleIndicatorRadius
        binding.rcpView.hueSliderView = binding.hueSeekBar
        binding.rcpView.setOnColorChangedListener { color ->
            colorAdapter.changeSelectedColor(color)
        }
    }

    fun showDialog(fragmentManager: FragmentManager, callback: Callback
    ) {
        this.callback = callback
        try {
            val tagDialog = ColorPickerBottomSheetDialog::class.java.name
            val fragment = fragmentManager.findFragmentByTag(tagDialog)
            if (fragment == null) {
                showNow(fragmentManager, tagDialog)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setupUI(binding: DialogColorPickerBinding) {
        dialog?.run {
            setCanceledOnTouchOutside(false)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setDimAmount(0.6f)
        }

        setupConfiguration()
        binding.ivCheck.setOnClickListener {
            colorAdapter.getSelectedColor()?.let { pickedColor ->
                callback?.onPickedColor(pickedColor)
            }

            dismiss()
        }

        binding.ivClose.setOnClickListener {
            dismiss()
        }
    }

}
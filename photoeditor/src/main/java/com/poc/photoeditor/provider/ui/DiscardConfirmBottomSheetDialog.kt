package com.poc.photoeditor.provider.ui

import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.DialogDiscardConfirmBinding
import com.poc.photoeditor.provider.ui.base.BaseBottomSheetDialogFragment
import com.poc.photoeditor.provider.ui.utils.InflateFragmentAlias

class DiscardConfirmBottomSheetDialog : BaseBottomSheetDialogFragment<DialogDiscardConfirmBinding>() {
    override val mInflater: InflateFragmentAlias<DialogDiscardConfirmBinding>
        get() = DialogDiscardConfirmBinding::inflate

    interface Callback {
        fun onShowed()
        fun onCancel()
        fun onDiscard()
    }

    private var callback: Callback? = null

    override fun getTheme(): Int {
        return R.style.Radius300BottomSheetBackgroundDialog
    }

    fun showDialog(fragmentManager: FragmentManager, callback: Callback) {
        this.callback = callback
        try {
            val tagDialog = DialogDiscardConfirmBinding::class.java.name
            val fragment = fragmentManager.findFragmentByTag(tagDialog)
            if (fragment == null) {
                showNow(fragmentManager, tagDialog)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setupUI(binding: DialogDiscardConfirmBinding) {
        dialog?.run {
            setCanceledOnTouchOutside(false)
            window?.setBackgroundDrawable(ColorDrawable(ResourcesCompat.getColor(resources, R.color.white_20, null)))
            window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window?.setDimAmount(1f)
        }

        binding.tvCancel.setOnClickListener {
            callback?.onCancel()
            dismiss()
        }

        binding.tvDiscard.setOnClickListener {
            callback?.onDiscard()
            dismiss()
        }
        callback?.onShowed()
    }
}
package com.poc.photoeditor.provider.ui.feature.text

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentManager
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.DialogInputBinding
import com.poc.photoeditor.provider.ui.base.BaseBottomSheetDialogFragment
import com.poc.photoeditor.provider.ui.feature.filter.FilterBottomSheetDialog
import com.poc.photoeditor.provider.ui.utils.InflateFragmentAlias


class TextInputDialog(private val input: String = "") : BaseBottomSheetDialogFragment<DialogInputBinding>() {
    companion object {
        val TAG = TextInputDialog::class.java.simpleName
        private const val DIM_AMOUNT = 0.6f
    }
    override val mInflater: InflateFragmentAlias<DialogInputBinding>
        get() = DialogInputBinding::inflate

    interface Callback {
        fun apply(text: String, isEdit: Boolean)
    }

    private val handler = Handler(Looper.getMainLooper())
    private var callback: Callback? = null
    private var requestFocusRunnable = Runnable {
        try {
            binding.etTextWidget.requestFocus()
            val imm = dialog?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(binding.etTextWidget, InputMethodManager.SHOW_IMPLICIT)
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        requestFocus()
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(requestFocusRunnable)
    }

    override fun getTheme(): Int {
        return R.style.Radius14BottomSheetBackgroundDialog
    }

    private fun requestFocus() {
        handler.postDelayed(requestFocusRunnable, 300)
    }

    override fun setupUI(binding: DialogInputBinding) {
        super.setupUI(binding)
        dialog?.run {
            setCanceledOnTouchOutside(false)
            isCancelable = false
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setDimAmount(DIM_AMOUNT)
        }
        setupViewEvents()
        binding.etTextWidget.setText(input)
    }

    private fun setupViewEvents() {
        binding.tvDone.setOnClickListener {
            callback?.apply(binding.etTextWidget.text.toString(), input.isNotBlank())
            dismiss()
        }

        binding.tvCancel.setOnClickListener {
            dismiss()
        }
    }

    fun showDialog(
        fragmentManager: FragmentManager,
        callback: Callback
    ) {
        try {
            this.callback = callback
            val tagDialog = FilterBottomSheetDialog::class.java.name
            val fragment = fragmentManager.findFragmentByTag(tagDialog)
            if (fragment == null) {
                showNow(fragmentManager, tagDialog)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
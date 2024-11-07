package com.poc.photoeditor.provider.ui.base

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleObserver
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.poc.photoeditor.provider.ui.utils.InflateFragmentAlias

abstract class BaseBottomSheetDialogFragment<VB : ViewBinding> : BottomSheetDialogFragment(),
    LifecycleObserver {

    private lateinit var _binding: VB
    protected val binding: VB
        get() = _binding

    protected abstract val mInflater: InflateFragmentAlias<VB>

    open fun setupUI(binding: VB) {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = mInflater.invoke(inflater, container, false).apply {
        _binding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.run {
            setCanceledOnTouchOutside(true)
        }
        setupUI(binding)
    }

    override fun onStart() {
        super.onStart()
        Log.d("BaseBottomSheetDialogFragment", "$this onStart")
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onStop() {
        super.onStop()
        Log.d("BaseBottomSheetDialogFragment", "$this onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("BaseBottomSheetDialogFragment", "$this onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("BaseBottomSheetDialogFragment", "$this onDestroy")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("BaseBottomSheetDialogFragment", "$this onDetach")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("BaseBottomSheetDialogFragment", "$this onDetach")
    }
}
package com.poc.photoeditor.provider.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleObserver
import androidx.viewbinding.ViewBinding
import com.poc.photoeditor.provider.ui.utils.InflateFragmentAlias

abstract class BaseDialogFragment<VB : ViewBinding> : DialogFragment(), LifecycleObserver {
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
        setupUI(binding)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
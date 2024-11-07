package com.poc.photoeditor.provider.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.poc.photoeditor.provider.ui.feature.text.TextBottomSheetDialog
import com.poc.photoeditor.provider.ui.utils.InflateAlias

abstract class EditorBaseFragment<VB : ViewBinding?> : Fragment() {
    abstract val bindingInflater: InflateAlias<VB>

    private var _binding: ViewBinding? = null

    @Suppress("UNCHECKED_CAST")
    val binding: VB?
        get() = _binding as? VB

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = bindingInflater.invoke(inflater, container, false).apply {
        _binding = this
    }?.root

    open fun setupViews() {}

    fun getTextBottomSheetDialog() = parentFragment as? TextBottomSheetDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
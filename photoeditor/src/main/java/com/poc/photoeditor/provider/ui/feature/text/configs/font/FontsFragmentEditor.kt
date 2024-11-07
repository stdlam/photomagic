package com.poc.photoeditor.provider.ui.feature.text.configs.font

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.FragmentFontsBinding
import com.poc.photoeditor.provider.ui.base.EditorBaseFragment
import com.poc.photoeditor.provider.ui.feature.text.configs.font.adapter.FontAdapter
import com.poc.photoeditor.provider.ui.feature.text.configs.font.adapter.FontFilterAdapter
import com.poc.photoeditor.provider.ui.utils.InflateAlias
import com.poc.photoeditor.provider.widget.StartSpaceItemDecoration
import com.poc.photoeditor.provider.widget.TopSpaceItemDecoration
import kotlinx.coroutines.launch

class FontsFragmentEditor : EditorBaseFragment<FragmentFontsBinding>() {
    override val bindingInflater: InflateAlias<FragmentFontsBinding>
        get() = FragmentFontsBinding::inflate

    private val viewModel: FontViewModel by viewModels()

    private val fontAdapter = FontAdapter(
        {
            getTextBottomSheetDialog()?.onUpdateFont(it.fontFile)
        },
        {
            viewModel.unselectOldFont()
        }
    )
    private val fontFilterAdapter = FontFilterAdapter {
        viewModel.filter(it.name)
    }

    override fun onResume() {
        super.onResume()
        binding?.root?.requestLayout()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setupViews() {
        binding?.rvFilter?.run {
            addItemDecoration(StartSpaceItemDecoration(context, R.dimen.long_6))
            adapter = fontFilterAdapter
        }

        binding?.rvFonts?.run {
            addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    rv.parent.requestDisallowInterceptTouchEvent(true)
                    return false
                }

                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

                }

                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

                }

            })
            addItemDecoration(TopSpaceItemDecoration(context, R.dimen.long_20))
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = fontAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupConfig() {
        getTextBottomSheetDialog()?.getFont()?.let {
            fontAdapter.selectFont(it)
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.state.collect {
                if (it is FontViewModel.ViewEvent.FontFilters) {
                    fontFilterAdapter.submitData(it.data)
                }
                if (it is FontViewModel.ViewEvent.DeviceFont) {
                    fontAdapter.submitData(it.fonts)
                    setupConfig()

                    fontAdapter.getSelectedFont()?.fontFile?.let {
                        getTextBottomSheetDialog()?.onUpdateFont(it)
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeData()
    }
}
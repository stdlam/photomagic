package com.poc.photoeditor.provider.ui.feature.filter

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import android.widget.Filter
import android.widget.RelativeLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.DialogFilterBinding
import com.poc.photoeditor.editor.PhotoFilter
import com.poc.photoeditor.editor.view.ImageFilterView
import com.poc.photoeditor.provider.ui.DiscardConfirmBottomSheetDialog
import com.poc.photoeditor.provider.ui.base.BaseBottomSheetDialogFragment
import com.poc.photoeditor.provider.ui.feature.filter.model.FilterModel
import com.poc.photoeditor.provider.ui.utils.BitmapUtils
import com.poc.photoeditor.provider.ui.utils.BlurHelper
import com.poc.photoeditor.provider.ui.utils.InflateFragmentAlias
import com.poc.photoeditor.provider.widget.StartSpaceItemDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilterBottomSheetDialog(
    private val config: FilterModel? = null
) : BaseBottomSheetDialogFragment<DialogFilterBinding>() {
    interface Callback {
        fun apply(filter: FilterModel)
        fun saveFilter()
    }

    private var filterSource: Uri? = null
    private var rawBitmap: Bitmap? = null
    private var callback: Callback? = null
    private var filterAdapter: FilterAdapter? = null

    override val mInflater: InflateFragmentAlias<DialogFilterBinding>
        get() = DialogFilterBinding::inflate

    fun showFilterDialog(uri: Uri?,
                         bitmap: Bitmap?,
                         fragmentManager: FragmentManager,
                         callback: Callback) {
        this.callback = callback
        this.rawBitmap = bitmap
        filterSource = uri
        try {
            val tagDialog = FilterBottomSheetDialog::class.java.name
            val fragment = fragmentManager.findFragmentByTag(tagDialog)
            if (fragment == null) {
                showNow(fragmentManager, tagDialog)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

    private fun discard() {
        config?.let {
            callback?.apply(it)
        } ?: kotlin.run {
            filterAdapter?.getNonFilter()?.let {
                callback?.apply(it)
            }
        }
    }

    override fun setupUI(binding: DialogFilterBinding) {
        dialog?.setCanceledOnTouchOutside(false)
        setupFilterList()
        binding.ivCheck.setOnClickListener {
            callback?.saveFilter()
            dismiss()
        }

        binding.ivClose.setOnClickListener {
            if (checkIfDifferentConfig()) {
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

    private fun checkIfDifferentConfig(): Boolean {
        val selectedFilter = filterAdapter?.getSelectedFilter()
        return if (config != null) config.filter != selectedFilter?.filter
        else selectedFilter?.filter != PhotoFilter.ORIGIN
    }

    private fun setupFilterList() {
        val sampleBitmap = if (filterSource != null) {
            MediaStore.Images.Media.getBitmap(
                context?.contentResolver, filterSource
            )
        } else rawBitmap

        sampleBitmap?.let {
            val reducedBitmap = BitmapUtils.getResizedBitmap(it, 50)
            filterAdapter = FilterAdapter { selectedFilter ->
                callback?.apply(selectedFilter)
            }
            binding.rvFilters.run {
                recycledViewPool.setMaxRecycledViews(0, 0)
                addItemDecoration(StartSpaceItemDecoration(context, R.dimen.long_8))
                adapter = filterAdapter

                insertBitmapSamples(reducedBitmap, getFilterData()) { filterSamples ->
                    filterAdapter?.submitData(filterSamples)

                    config?.let { filterModel ->
                        filterAdapter?.selectFilter(filterModel)
                    }
                }
            }
        }
    }

    private fun insertBitmapSamples(sampleBitmap: Bitmap,
                                    filters: ArrayList<FilterModel>,
                                    inserted: (ArrayList<FilterModel>) -> Unit
    ) = lifecycleScope.launch(Dispatchers.IO) {
        withContext(Dispatchers.Main) {
            binding.flSample.visibility = View.VISIBLE
        }

        filters.forEach { filter ->
            binding.ivSample.run {
                setSourceBitmap(sampleBitmap)
                setFilterEffect(filter.filter)
                filter.bitmap = saveBitmap()
            }

        }
        withContext(Dispatchers.Main) {
            binding.flSample.visibility = View.GONE
            inserted.invoke(filters)
        }
    }

    private fun getFilterData() = arrayListOf(
        FilterModel("Origin", filter = PhotoFilter.ORIGIN, selected = true),
        FilterModel("Auto Fix", filter = PhotoFilter.AUTO_FIX, selected = false),
        FilterModel("Black White", filter = PhotoFilter.BLACK_WHITE, selected = false),
        FilterModel("Brightness", filter = PhotoFilter.BRIGHTNESS, selected = false),
        FilterModel("Contrast", filter = PhotoFilter.CONTRAST, selected = false),
        FilterModel("Cross Process", filter = PhotoFilter.CROSS_PROCESS, selected = false),
        FilterModel("Documentary", filter = PhotoFilter.DOCUMENTARY, selected = false),
        FilterModel("Due Tone", filter = PhotoFilter.DUE_TONE, selected = false),
        FilterModel("Fill Light", filter = PhotoFilter.FILL_LIGHT, selected = false),
        FilterModel("Fish Eye", filter = PhotoFilter.FISH_EYE, selected = false),
        FilterModel("Flip Vertical", filter = PhotoFilter.FLIP_VERTICAL, selected = false),
        FilterModel("Flip Horizontal", filter = PhotoFilter.FLIP_HORIZONTAL, selected = false),
        FilterModel("Grain", filter = PhotoFilter.GRAIN, selected = false),
        FilterModel("Gray Scale", filter = PhotoFilter.GRAY_SCALE, selected = false),
        FilterModel("Lomish", filter = PhotoFilter.LOMISH, selected = false),
        FilterModel("Negative", filter = PhotoFilter.NEGATIVE, selected = false),
        FilterModel("Posterize", filter = PhotoFilter.POSTERIZE, selected = false),
        FilterModel("Rotate", filter = PhotoFilter.ROTATE, selected = false),
        FilterModel("Saturate", filter = PhotoFilter.SATURATE, selected = false),
        FilterModel("Sepia", filter = PhotoFilter.SEPIA, selected = false),
        FilterModel("Sharpen", filter = PhotoFilter.SHARPEN, selected = false),
        FilterModel("Temperature", filter = PhotoFilter.TEMPERATURE, selected = false),
        FilterModel("Tint", filter = PhotoFilter.TINT, selected = false),
        FilterModel("Vignette", filter = PhotoFilter.VIGNETTE, selected = false)
    )
}
package com.poc.photoeditor.provider.ui.feature.crop

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.DialogCropBinding
import com.poc.photoeditor.provider.ui.base.BaseBottomSheetDialogFragment
import com.poc.photoeditor.provider.ui.feature.crop.model.OptionModel
import com.poc.photoeditor.provider.ui.feature.text.configs.ConfigsAdapter
import com.poc.photoeditor.provider.ui.model.ConfigUIModel
import com.poc.photoeditor.provider.ui.utils.InflateFragmentAlias
import com.poc.photoeditor.provider.widget.StartSpaceItemDecoration

class CropBottomSheetDialog(private val result: (Boolean) -> Unit) : BaseBottomSheetDialogFragment<DialogCropBinding>() {
    companion object {
        private const val CROP_ORIGINAL = 0
        private const val CROP_FREE = 1
        private const val CROP_SQUARE = 2
        private const val CROP_3_4 = 3
        private const val CROP_4_3 = 4
        private const val ROTATE_LEFT = 5
        private const val ROTATE_RIGHT = 6
        private const val ROTATE_MIRROR = 7
        private const val ROTATE_FLIP = 8
        private const val PERSPECTIVE_HORIZONTAL = 9
        private const val PERSPECTIVE_VERTICAL = 10
    }
    override val mInflater: InflateFragmentAlias<DialogCropBinding>
        get() = DialogCropBinding::inflate

    private val options = arrayListOf(
        OptionModel(0, "Crop"),
        OptionModel(1, "Rotate"),
        OptionModel(2, "Perspective")
    )

    private val configsAdapter = ConfigsAdapter {

    }

    override fun getTheme(): Int {
        return R.style.TransparentRadius0BottomSheetBackgroundDialog
    }

    private var selectedOption = options.first()

    private val cropConfigData = arrayListOf(
        ConfigUIModel(CROP_ORIGINAL, R.drawable.ic_crop_original, "Origin", false),
        ConfigUIModel(CROP_FREE, R.drawable.ic_crop_free, "Free", false),
        ConfigUIModel(CROP_SQUARE, R.drawable.ic_crop_square, "Square", false),
        ConfigUIModel(CROP_3_4, R.drawable.ic_crop_3_4, "3:4", false),
        ConfigUIModel(CROP_4_3, R.drawable.ic_crop_4_3, "4:3", false)
    )

    private val rotateConfigData = arrayListOf(
        ConfigUIModel(ROTATE_LEFT, R.drawable.ic_rotate_left, "Left", false),
        ConfigUIModel(ROTATE_RIGHT, R.drawable.ic_rotate_right, "Right", false),
        ConfigUIModel(ROTATE_MIRROR, R.drawable.ic_rotate_mirror, "Mirror", false),
        ConfigUIModel(ROTATE_FLIP, R.drawable.ic_rotate_flip, "Flip", false)
    )

    private val perspectiveConfigData = arrayListOf(
        ConfigUIModel(PERSPECTIVE_HORIZONTAL, R.drawable.ic_perspetive_horizontal, "Horizontal", false),
        ConfigUIModel(PERSPECTIVE_VERTICAL, R.drawable.ic_perspective_vertical, "Vertical", false)
    )

    override fun setupUI(binding: DialogCropBinding) {
        setupConfigs()
        dialog?.run {
            setCanceledOnTouchOutside(false)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setDimAmount(0.6f)
        }
    }

    fun showDialog(fragmentManager: FragmentManager) {
        try {
            val tagDialog = DialogCropBinding::class.java.name
            val fragment = fragmentManager.findFragmentByTag(tagDialog)
            if (fragment == null) {
                showNow(fragmentManager, tagDialog)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupOptions() {
        val disableColor = ResourcesCompat.getColor(resources, R.color.disable_grey, null)
        val enableColor = ResourcesCompat.getColor(resources, R.color.white, null)
        when (selectedOption.id) {
            0 -> {
                binding.tvCrop.setTextColor(enableColor)
                binding.tvRotate.setTextColor(disableColor)
                binding.tvPerspective.setTextColor(disableColor)
            }
            1 -> {
                binding.tvCrop.setTextColor(disableColor)
                binding.tvRotate.setTextColor(enableColor)
                binding.tvPerspective.setTextColor(disableColor)
            }
            else -> {
                binding.tvCrop.setTextColor(disableColor)
                binding.tvRotate.setTextColor(disableColor)
                binding.tvPerspective.setTextColor(enableColor)
            }
        }
    }

    private fun selectOption() {
        context?.let { ctx ->
            binding.clOptions.visibility = View.VISIBLE
            binding.clOptions.startAnimation(AnimationUtils.loadAnimation(ctx, R.anim.slide_up))
        }
        setupOptions()
    }

    private fun setupOptionEvents() {
        binding.tvOption.setOnClickListener {
            selectOption()
        }

        binding.ivArrow.setOnClickListener {
            selectOption()
        }

        binding.tvCrop.setOnClickListener {
            context?.let { ctx ->
                binding.clOptions.visibility = View.GONE
                binding.clOptions.startAnimation(AnimationUtils.loadAnimation(ctx, R.anim.slide_down))
            }
            selectedOption = options[0]
            binding.tvOption.text = getString(R.string.common_crop)

            submitConfigs()
            binding.rlView.visibility = View.INVISIBLE
        }

        binding.tvRotate.setOnClickListener {
            context?.let { ctx ->
                binding.clOptions.visibility = View.GONE
                binding.clOptions.startAnimation(AnimationUtils.loadAnimation(ctx, R.anim.slide_down))
            }
            selectedOption = options[1]
            binding.tvOption.text = getString(R.string.common_rotate)
            submitConfigs()

            binding.rlView.visibility = View.VISIBLE
            binding.rlView.scrollToCenter()
        }

        binding.tvPerspective.setOnClickListener {
            context?.let { ctx ->
                binding.clOptions.visibility = View.GONE
                binding.clOptions.startAnimation(AnimationUtils.loadAnimation(ctx, R.anim.slide_down))
            }
            selectedOption = options[2]
            binding.tvOption.text = getString(R.string.common_perspective)
            submitConfigs()

            binding.rlView.visibility = View.VISIBLE
            binding.rlView.scrollToCenter()
        }
    }

    private fun submitConfigs() {
        when (selectedOption.id) {
            0 -> {
                configsAdapter.submitData(cropConfigData)
            }
            1 -> {
                configsAdapter.submitData(rotateConfigData)
            }
            else -> {
                configsAdapter.submitData(perspectiveConfigData)
            }
        }
    }

    private fun setupConfigs() {
        binding.rvConfigs.run {
            addItemDecoration(StartSpaceItemDecoration(context, R.dimen.long_24))
            adapter = configsAdapter
        }

        binding.ivClose.setOnClickListener {
            result.invoke(false)
            dismiss()
        }

        binding.ivCheck.setOnClickListener {
            result.invoke(true)
            dismiss()
        }

        submitConfigs()
        setupOptionEvents()
        setupOptions()
    }
}
package com.poc.photoeditor.provider.ui.feature.adjust.curve

import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.util.Log
import androidx.fragment.app.FragmentManager
import com.poc.photoeditor.databinding.DialogCurveBinding
import com.poc.photoeditor.provider.ui.DiscardConfirmBottomSheetDialog
import com.poc.photoeditor.provider.ui.base.BaseBottomSheetDialogFragment
import com.poc.photoeditor.provider.ui.feature.text.configs.ConfigsAdapter
import com.poc.photoeditor.provider.ui.model.CurveConfig
import com.poc.photoeditor.provider.ui.model.ConfigUIModel
import com.poc.photoeditor.provider.ui.utils.BlurHelper
import com.poc.photoeditor.provider.ui.utils.InflateFragmentAlias
import com.poc.photoeditor.provider.widget.CurvesView
import com.poc.photoeditor.provider.widget.StartSpaceItemDecoration

class CurveBottomSheetFragment(private val curves: ArrayList<CurveConfig>? = null,
                               private val callback: ((ArrayList<CurveConfig>?, Boolean, Float, Float) -> Unit)? = null
) : BaseBottomSheetDialogFragment<DialogCurveBinding>() {
    override val mInflater: InflateFragmentAlias<DialogCurveBinding>
        get() = DialogCurveBinding::inflate

    companion object {
        const val RGB = 0
        const val R = 1
        const val G = 2
        const val B = 3
    }

    private var curveViewWidth = 0f
    private var curveViewHeight = 0f
    private var tmpCurves: ArrayList<CurveConfig> = arrayListOf()
    private var tmpCurve: CurveConfig? = null

    private val curveCallback = object : CurvesView.CurveColorResponse {
        override fun onPointUpdate(
            points: ArrayList<PointF>,
            graphWidth: Float,
            graphHeight: Float
        ) {
            curveViewWidth = graphWidth
            curveViewHeight = graphHeight
            tmpCurve?.points = points
            tmpCurves.run {
                firstOrNull { it.id == tmpCurve?.id }?.let { curve ->
                    curve.points = points
                }
                callback?.invoke(this, false, graphWidth, graphHeight)
            }
        }

        override fun onInitialized(points: ArrayList<PointF>) {
            initTmpCurves()
        }
    }

    override fun getTheme(): Int {
        return com.poc.photoeditor.R.style.TransparentRadius0BottomSheetBackgroundDialog
    }

    private val configData = arrayListOf(
        ConfigUIModel(RGB, com.poc.photoeditor.R.drawable.ic_color, "RGB", true),
        ConfigUIModel(R, null, "R", false, Color.parseColor("#FF3938")),
        ConfigUIModel(G, null, "G", false, Color.parseColor("#67E432")),
        ConfigUIModel(B, null, "B", false, Color.parseColor("#3781FC"))
    )

    private val configsAdapter = ConfigsAdapter { config ->
        setupCurveViews(config.id)
    }

    private fun setupCurveViews(configId: Int) {
        Log.d("Curving", "setupCurveViews configId=$configId, tmpCurves=$tmpCurves")
        tmpCurve = tmpCurves.firstOrNull { it.id == configId }
        tmpCurve?.let {
            binding.cvColor.setupCurve(it)
        }
    }

    fun showDialog(fragmentManager: FragmentManager) {
        try {
            val tagDialog = DialogCurveBinding::class.java.name
            val fragment = fragmentManager.findFragmentByTag(tagDialog)
            if (fragment == null) {
                showNow(fragmentManager, tagDialog)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupColorsConfig() {
        binding.rvConfigs.run {
            addItemDecoration(StartSpaceItemDecoration(context, com.poc.photoeditor.R.dimen.long_24))
            adapter = configsAdapter
            configsAdapter.submitData(configData)
        }
        setupCurveViews(RGB)
    }

    private fun initTmpCurves() {
        val defaultPoints = binding.cvColor.getDefaultPoints()
        Log.d("Curving", "initTmpCurves defaultPoints=$defaultPoints")
        if (tmpCurves.isEmpty()) {
            tmpCurves = arrayListOf(
                CurveConfig(RGB, defaultPoints),
                CurveConfig(R, defaultPoints),
                CurveConfig(G, defaultPoints),
                CurveConfig(B, defaultPoints)
            )
        }
        setupColorsConfig()
    }

    override fun setupUI(binding: DialogCurveBinding) {
        binding.cvColor.setupCallback(curveCallback)
        curves?.let {
            tmpCurves.clear()
            it.forEach { curve ->
                tmpCurves.add(CurveConfig(curve.id, curve.points))
            }
        }

        dialog?.run {
            setCanceledOnTouchOutside(false)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setDimAmount(0.6f)
        }
        binding.ivCheck.setOnClickListener {
            callback?.invoke(tmpCurves, true, curveViewWidth, curveViewHeight)
            dismiss()
        }

        binding.ivClose.setOnClickListener {
            if (checkIfConfigDifference() || checkIfInitChanged()) {
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
                callback?.invoke(curves, true, curveViewWidth, curveViewHeight)
                dismiss()
            }
        }
    }

    private fun discard() {
        callback?.invoke(curves, true, curveViewWidth, curveViewHeight)
    }

    private fun checkIfInitChanged(): Boolean {
        return curves == null && tmpCurves.any { it.points.size > 2 }
    }

    private fun checkIfConfigDifference(): Boolean {
        var changed = false
        curves?.forEachIndexed { index, curve ->
            if (index < tmpCurves.size) {
                if (curve.id == tmpCurves[index].id &&
                    curve.points != tmpCurves.get(index).points) {
                    changed = true
                }
            }
        }
        return changed
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
}
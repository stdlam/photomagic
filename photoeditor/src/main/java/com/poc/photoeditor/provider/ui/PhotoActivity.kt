package com.poc.photoeditor.provider.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.ActivityPhotoBinding
import com.poc.photoeditor.editor.OnPhotoEditorListener
import com.poc.photoeditor.editor.OnSaveBitmap
import com.poc.photoeditor.editor.PhotoEditor
import com.poc.photoeditor.editor.graphic.Graphic
import com.poc.photoeditor.editor.graphic.Text
import com.poc.photoeditor.editor.view.ViewType
import com.poc.photoeditor.provider.PhotoEditProvider
import com.poc.photoeditor.provider.ui.feature.adjust.AdjustBottomSheetDialog
import com.poc.photoeditor.provider.ui.feature.adjust.AdjustBottomSheetDialog.Companion.BRIGHTNESS
import com.poc.photoeditor.provider.ui.feature.adjust.AdjustBottomSheetDialog.Companion.CONTRAST
import com.poc.photoeditor.provider.ui.feature.adjust.AdjustBottomSheetDialog.Companion.HSL
import com.poc.photoeditor.provider.ui.feature.adjust.AdjustBottomSheetDialog.Companion.SHADOW
import com.poc.photoeditor.provider.ui.feature.adjust.AdjustBottomSheetDialog.Companion.TINT
import com.poc.photoeditor.provider.ui.feature.adjust.AdjustBottomSheetDialog.Companion.WARMTH
import com.poc.photoeditor.provider.ui.feature.adjust.curve.CurveBottomSheetFragment
import com.poc.photoeditor.provider.ui.feature.adjust.hsl.ColorFilterGenerator
import com.poc.photoeditor.provider.ui.feature.adjust.hsl.model.HSLModel
import com.poc.photoeditor.provider.ui.feature.adjust.model.AdjustConfig
import com.poc.photoeditor.provider.ui.feature.filter.FilterBottomSheetDialog
import com.poc.photoeditor.provider.ui.feature.filter.model.FilterModel
import com.poc.photoeditor.provider.ui.feature.text.TextBottomSheetDialog
import com.poc.photoeditor.provider.ui.model.CurveConfig
import com.poc.photoeditor.provider.ui.model.TextConfig
import com.poc.photoeditor.provider.ui.utils.updateWindowForBlurs
import com.poc.photoeditor.provider.widget.StartSpaceItemDecoration
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.ToneCurveSubFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import com.poc.photoeditor.editor.view.PhotoEditorView
import com.poc.photoeditor.provider.ui.feature.crop.CropperActivity
import com.poc.photoeditor.provider.ui.utils.BitmapUtils

class PhotoActivity
    : AppCompatActivity(),
    OnPhotoEditorListener,
    Graphic.GraphicListener,
    AdjustCommunity {

    companion object {
        private const val MIN_COLOR_COORDINATE = 0f
        private const val MAX_COLOR_COORDINATE = 255f
        const val PHOTO_CROPPING = "PHOTO_CROPPING"
    }

    private lateinit var binding: ActivityPhotoBinding

    private var sourceUri: Uri? = null
    lateinit var mPhotoEditor: PhotoEditor
    private var currentTextEditor: Text? = null
    private var currentFilter: FilterModel? = null
    private var curveConfig: ArrayList<CurveConfig>? = null
    private var adjustConfig: AdjustConfig = AdjustConfig()

    private val cropLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        it.data?.data?.let { croppedUri ->
            lifecycleScope.launch(Dispatchers.Main) {
                val croppedBitmap = withContext(Dispatchers.IO) {
                    try {
                        MediaStore.Images.Media.getBitmap(contentResolver, croppedUri)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }

                binding.photoEditorView.source.setImageBitmap(croppedBitmap)
                binding.ivPreview.setImageBitmap(croppedBitmap)

                // delete temp uri
                contentResolver.delete(croppedUri, null, null)
            }

        }
    }

    private val filterCallback = object : FilterBottomSheetDialog.Callback {
        override fun apply(filter: FilterModel) {
            currentFilter = filter
            binding.photoEditorView.setFilterEffect(filter.filter)
        }

        override fun saveFilter() {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val savedFilter = binding.photoEditorView.saveFilter()
                    withContext(Dispatchers.Main) {
                        binding.ivPreview.setImageBitmap(savedFilter)
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }

    }

    private fun confirmToSave() {
        mPhotoEditor.saveAsBitmap(object: OnSaveBitmap {
            override fun onBitmapReady(saveBitmap: Bitmap) {
                sourceUri?.let {
                    BitmapUtils.saveBitmapToUri(this@PhotoActivity, saveBitmap, it)
                    setResult(Activity.RESULT_OK, Intent().apply {
                        data = it
                    })
                    finish()
                }
            }

        })
    }

    private fun calculateColorCoordinators(controlPoints: ArrayList<PointF>, viewWidth: Float, viewHeight: Float): ArrayList<com.zomato.photofilters.geometry.Point> {
        val result = arrayListOf<com.zomato.photofilters.geometry.Point>()
        controlPoints.forEachIndexed { index, pointF ->
            when (index) {
                0 -> {
                    result.add(com.zomato.photofilters.geometry.Point(MIN_COLOR_COORDINATE, MIN_COLOR_COORDINATE))
                }
                controlPoints.size - 1 -> {
                    result.add(com.zomato.photofilters.geometry.Point(MAX_COLOR_COORDINATE, MAX_COLOR_COORDINATE))
                }
                else -> {
                    val colorX = (MAX_COLOR_COORDINATE * pointF.x) / viewWidth
                    val colorY = MAX_COLOR_COORDINATE - ( (MAX_COLOR_COORDINATE * pointF.y) / viewHeight)

                    result.add(com.zomato.photofilters.geometry.Point(colorX, colorY))
                }
            }
        }

        return result
    }

    private val adjustCallback = object : AdjustBottomSheetDialog.Callback {
        override fun applyCurve(
            curveConfigs: ArrayList<CurveConfig>?,
            width: Float,
            height: Float,
            doneDeal: Boolean
        ) {
            this@PhotoActivity.curveConfig = curveConfigs
            lifecycleScope.launch(Dispatchers.Main) {
                val rgbKnots = curveConfigs?.firstOrNull {
                    it.id == CurveBottomSheetFragment.RGB
                }?.points?.let { points ->
                    calculateColorCoordinators(points, width, height)
                }

                val redKnots = curveConfigs?.firstOrNull {
                    it.id == CurveBottomSheetFragment.R
                }?.points?.let { points ->
                    calculateColorCoordinators(points, width, height)
                }

                val greenKnots = curveConfigs?.firstOrNull {
                    it.id == CurveBottomSheetFragment.G
                }?.points?.let { points ->
                    calculateColorCoordinators(points, width, height)
                }

                val blueKnots = curveConfigs?.firstOrNull {
                    it.id == CurveBottomSheetFragment.B
                }?.points?.let { points ->
                    calculateColorCoordinators(points, width, height)
                }

                val filter = Filter().apply {
                    addSubFilter(
                        ToneCurveSubFilter(
                            rgbKnots?.toTypedArray(),
                            redKnots?.toTypedArray(),
                            greenKnots?.toTypedArray(),
                            blueKnots?.toTypedArray()
                        )
                    )
                }

                if (doneDeal) {
                    binding.ivPreview.visibility = View.GONE
                    binding.photoEditorView.sourceBitmap?.let {
                        val newBitmap = filter.processFilter(it)
                        binding.photoEditorView.source.setImageBitmap(newBitmap)
                    }
                } else {
                    binding.ivPreview.visibility = View.VISIBLE
                    binding.photoEditorView.sourceBitmap?.let {
                        val newBitmap = filter.processFilter(it)
                        binding.ivPreview.setImageBitmap(newBitmap)
                    }
                }

            }

        }

        override fun applyAdjust(
            adjust: AdjustConfig,
            id: Int,
            doneDeal: Boolean,
            discard: Boolean
        ) {
            lifecycleScope.launch(Dispatchers.Main) {
                when (id) {
                    BRIGHTNESS -> {
                        applyBrightness(adjust.brightness, doneDeal, discard)
                    }

                    CONTRAST -> {
                        applyContrast(adjust.constrast, doneDeal, discard)
                    }

                    WARMTH -> {
                        applyWarmth(adjust.warmth, doneDeal, discard)
                    }

                    TINT -> {
                        applyTint(adjust.tint, doneDeal, discard)
                    }

                    SHADOW -> {
                        applyShadow(adjust.showdow, doneDeal, discard)
                    }

                    HSL -> {
                        applyHLS(adjust.hslModel, doneDeal, discard)
                    }
                }

            }

        }
    }

    private val textConfigCallback = object : TextBottomSheetDialog.Callback {
        override fun onTextUpdated(textConfig: TextConfig, isNew: Boolean) {
            if (isNew) {
                mPhotoEditor.addText(textConfig.text, textConfig.textStyleBuilder, this@PhotoActivity, false)
            } else {
                currentTextEditor?.let {
                    mPhotoEditor.editText(it, textConfig.text, textConfig.textStyleBuilder)
                }
            }
        }

        override fun onSaveConfig(textConfig: TextConfig) {
            currentTextEditor?.saveConfig(textConfig)
        }
    }

    private val toolbarAdapter = ToolbarAdapter(object : ToolbarAdapter.OnItemSelected {
        override fun onToolSelected(toolType: ToolType) {
            when (toolType) {
                ToolType.FILTER -> {
                    if (binding.ivPreview.drawable != null) {
                        FilterBottomSheetDialog(currentFilter).showFilterDialog(null, binding.ivPreview.drawable.toBitmap(), supportFragmentManager, filterCallback)
                    } else {
                        FilterBottomSheetDialog(currentFilter).showFilterDialog(sourceUri, null, supportFragmentManager, filterCallback)
                    }
                }
                ToolType.TEXT -> {
                    TextBottomSheetDialog(currentTextEditor?.getTextConfig()).showDialog(supportFragmentManager, textConfigCallback)
                }
                ToolType.ADJUST -> {
                    AdjustBottomSheetDialog(curveConfig, adjustConfig).showDialog(supportFragmentManager, adjustCallback)
                }
                else -> {
                    startCrop()
                }
            }
        }
    })

    private fun startCrop() {
        binding.photoEditorView.sourceBitmap?.let { bitmap ->
            sourceUri?.let { uri ->
                BitmapUtils.saveBitmapToUri(this, bitmap, uri)
                Log.d("Cropping", "startCrop sourceUri=$sourceUri, cropUri=$uri")
                cropLauncher.launch(
                    Intent(
                        this,
                        CropperActivity::class.java
                    ).apply {
                        putExtra(PHOTO_CROPPING, uri)
                    }
                )
            }
        }
    }

    private fun updateWindowForBlurs(blursEnabled: Boolean) {
        binding.bvBackground.updateWindowForBlurs(this, blursEnabled)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPhotoBinding.inflate(layoutInflater)
        //makeFullScreen()
        setContentView(binding.root)
        setupEditPhoto()
        setupToolbar()

        mPhotoEditor = PhotoEditor.Builder(this, binding.photoEditorView)
            .setPinchTextScalable(true) // set flag to make text scalable when pinch
            //.setDefaultTextTypeface(mTextRobotoTf)
            //.setDefaultEmojiTypeface(mEmojiTypeFace)
            .build() // build photo editor sdk

        mPhotoEditor.setOnPhotoEditorListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        confirmToSave()
    }

    private fun setupEditPhoto() {
        (intent?.getParcelableExtra(PhotoEditProvider.PHOTO_EDIT_PATH) as? Uri).let { pathUri ->
            sourceUri = pathUri
            binding.photoEditorView.setClipSourceImage(false)
            binding.photoEditorView.source.setImageURI(sourceUri)
            binding.ivPreview.setImageURI(sourceUri)
        }
    }

    private fun setupToolbar() {
        binding.rvConstraintTools.run {
            addItemDecoration(StartSpaceItemDecoration(context, R.dimen.long_24))
            layoutManager = LinearLayoutManager(this@PhotoActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = toolbarAdapter
        }
    }

    override fun onEditTextChangeListener(rootView: View, text: String, colorCode: Int) {

    }

    override fun onAddViewListener(viewType: ViewType, graphic: Graphic, x: Int, y: Int) {
        if (graphic is Text) {
            currentTextEditor = graphic
        }
    }

    override fun onRemoveViewListener(viewType: ViewType, numberOfAddedViews: Int) {

    }

    override fun onStartViewChangeListener(viewType: ViewType) {

    }

    override fun onStopViewChangeListener(viewType: ViewType) {

    }

    override fun onTouchSourceImage(event: MotionEvent) {
        currentTextEditor = null
    }

    override fun onGraphicClicked(graphic: Graphic) {
        if (graphic is Text) {
            currentTextEditor = graphic
        }
    }

    override fun onGraphicMoved(x: Int, y: Int) {
    }

    override fun onGraphicScaled(x: Float, y: Float) {
    }

    override fun onGraphicRotated(angle: Float) {
    }

    override fun onGraphicRemoved() {
        currentTextEditor = null
    }

    override fun onGraphicDuplicate() {
        val textConfig = currentTextEditor?.getTextConfig()
        mPhotoEditor.addText(textConfig?.text ?: "", textConfig?.textStyleBuilder, this@PhotoActivity, true)
    }

    override fun onGraphicRect(point1: Point, point2: Point, point3: Point, point4: Point) {
    }

    override fun applyBrightness(value: Int, doneDeal: Boolean, discard: Boolean) {
        val filter = Filter().apply {
            addSubFilter(
                BrightnessSubFilter(
                    if (discard) 0
                    else value - 50
                )
            )
        }

        if (doneDeal) {
            binding.ivPreview.visibility = View.GONE
            binding.photoEditorView.sourceBitmap?.let {
                val newBitmap = filter.processFilter(it)
                binding.photoEditorView.source.setImageBitmap(newBitmap)
            }
            adjustConfig.brightness = value
        } else {
            binding.ivPreview.visibility = View.VISIBLE
            binding.photoEditorView.sourceBitmap?.let {
                val newBitmap = filter.processFilter(it)
                binding.ivPreview.setImageBitmap(newBitmap)
            }
        }
    }

    override fun applyContrast(value: Int, doneDeal: Boolean, discard: Boolean) {

        val filter = Filter().apply {
            addSubFilter(
                ContrastSubFilter(
                    if (discard) 1f
                    else value / 50f
                )
            )
        }

        if (doneDeal) {
            binding.ivPreview.visibility = View.GONE
            binding.photoEditorView.sourceBitmap?.let {
                val newBitmap = filter.processFilter(it)
                binding.photoEditorView.source.setImageBitmap(newBitmap)
            }
            adjustConfig.constrast = value
        } else {
            binding.ivPreview.visibility = View.VISIBLE
            binding.photoEditorView.sourceBitmap?.let {
                val newBitmap = filter.processFilter(it)
                binding.ivPreview.setImageBitmap(newBitmap)
            }
        }
    }

    override fun applyWarmth(value: Int, doneDeal: Boolean, discard: Boolean) {
        val blueKnots = arrayOf(
            com.zomato.photofilters.geometry.Point(0f, 0f),
            com.zomato.photofilters.geometry.Point(255f / 2, abs(255f - ((value * 255f)  / 100))),
            com.zomato.photofilters.geometry.Point(255f, 255f)
        )

        val filter = Filter().apply {
            addSubFilter(
                ToneCurveSubFilter(
                    null,
                    null,
                    null,
                    blueKnots
                )
            )
        }

        if (doneDeal) {
            binding.ivPreview.visibility = View.GONE
            binding.photoEditorView.sourceBitmap?.let {
                val newBitmap = filter.processFilter(it)
                binding.photoEditorView.source.setImageBitmap(newBitmap)
            }
            adjustConfig.warmth = value
        } else {
            binding.ivPreview.visibility = View.VISIBLE
            binding.photoEditorView.sourceBitmap?.let {
                val newBitmap = filter.processFilter(it)
                binding.ivPreview.setImageBitmap(newBitmap)
            }
        }
    }

    override fun applyTint(value: Int, doneDeal: Boolean, discard: Boolean) {
        val greenKnots = arrayOf(
            com.zomato.photofilters.geometry.Point(0f, 0f),
            com.zomato.photofilters.geometry.Point(255f / 2, abs(255f - ((value * 255f)  / 100))),
            com.zomato.photofilters.geometry.Point(255f, 255f)
        )

        val filter = Filter().apply {
            addSubFilter(
                ToneCurveSubFilter(
                    null,
                    null,
                    greenKnots,
                    null
                )
            )
        }

        if (doneDeal) {
            binding.ivPreview.visibility = View.GONE
            binding.photoEditorView.sourceBitmap?.let {
                val newBitmap = filter.processFilter(it)
                binding.photoEditorView.source.setImageBitmap(newBitmap)
            }
            adjustConfig.tint = value
        } else {
            binding.ivPreview.visibility = View.VISIBLE
            binding.photoEditorView.sourceBitmap?.let {
                val newBitmap = filter.processFilter(it)
                binding.ivPreview.setImageBitmap(newBitmap)
            }
        }
    }

    override fun applyShadow(value: Int, doneDeal: Boolean, discard: Boolean) {
        val rgbKnots = arrayOf(
            com.zomato.photofilters.geometry.Point(0f, 0f),
            com.zomato.photofilters.geometry.Point(255f / 2, (value * 255f)  / 100),
            com.zomato.photofilters.geometry.Point(255f, 255f)
        )

        val filter = Filter().apply {
            addSubFilter(
                ToneCurveSubFilter(
                    rgbKnots,
                    null,
                    null,
                    null
                )
            )
        }

        if (doneDeal) {
            binding.ivPreview.visibility = View.GONE
            binding.photoEditorView.sourceBitmap?.let {
                val newBitmap = filter.processFilter(it)
                binding.photoEditorView.source.setImageBitmap(newBitmap)
            }
            adjustConfig.showdow = value
        } else {
            binding.ivPreview.visibility = View.VISIBLE
            binding.photoEditorView.sourceBitmap?.let {
                val newBitmap = filter.processFilter(it)
                binding.ivPreview.setImageBitmap(newBitmap)
            }
        }
    }

    override fun applyHLS(value: HSLModel, doneDeal: Boolean, discard: Boolean) {
        if (value == adjustConfig.hslModel) return
        binding.photoEditorView.sourceBitmap?.let {
            val colorFilter = ColorFilterGenerator.Builder()
                .setHue(value.hue / 5)
                .setSaturation(value.saturation / 5)
                .setBrightness(value.luminance)
                .setBaseColor(value.baseColor)
                .build()

            if (binding.ivPreview.drawable == null) {
                binding.ivPreview.setImageBitmap(it)
            }
            if (doneDeal) {
                binding.ivPreview.visibility = View.GONE
                binding.photoEditorView.source.colorFilter = colorFilter
                adjustConfig.hslModel = value
            } else {
                binding.ivPreview.visibility = View.VISIBLE
                binding.ivPreview.colorFilter = colorFilter
            }
        }
    }
}
package com.poc.photoeditor.provider.ui.feature.crop

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.drawToBitmap
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import com.canhub.cropper.CropImageView
import com.poc.photoeditor.R
import com.poc.photoeditor.common.ASPECT_RATIO_FOUR_THREE
import com.poc.photoeditor.common.ASPECT_RATIO_FREE
import com.poc.photoeditor.common.ASPECT_RATIO_ONE_ONE
import com.poc.photoeditor.common.ASPECT_RATIO_OTHER
import com.poc.photoeditor.common.ASPECT_RATIO_SIXTEEN_NINE
import com.poc.photoeditor.common.ASPECT_RATIO_THREE_FOUR
import com.poc.photoeditor.common.copyNonDimensionAttributesTo
import com.poc.photoeditor.databinding.ActivityCropperBinding
import com.poc.photoeditor.provider.ui.PhotoActivity
import com.poc.photoeditor.provider.ui.feature.crop.model.OptionModel
import com.poc.photoeditor.provider.ui.feature.text.configs.ConfigsAdapter
import com.poc.photoeditor.provider.ui.model.ConfigUIModel
import com.poc.photoeditor.provider.ui.utils.BitmapUtils
import com.poc.photoeditor.provider.ui.utils.BitmapUtils.cornerPin
import com.poc.photoeditor.provider.ui.utils.config
import com.poc.photoeditor.provider.widget.RulerView
import com.poc.photoeditor.provider.widget.StartSpaceItemDecoration
import com.simplemobiletools.commons.extensions.getCompressionFormat
import com.simplemobiletools.commons.extensions.getCurrentFormattedDateTime
import com.simplemobiletools.commons.extensions.getFilenameExtension
import com.simplemobiletools.commons.extensions.getFilenameFromContentUri
import com.simplemobiletools.commons.extensions.getFilenameFromPath
import com.simplemobiletools.commons.extensions.getParentPath
import com.simplemobiletools.commons.extensions.getRealPathFromURI
import com.simplemobiletools.commons.extensions.internalStoragePath
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.REAL_FILE_PATH
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.helpers.isNougatPlus
import com.simplemobiletools.commons.models.FileDirItem
import getFileOutputStream
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class CropperActivity : AppCompatActivity(),
    CropImageView.OnCropImageCompleteListener {
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
        private const val CROP_ROTATE_NONE = 0
        private const val CROP_ROTATE_ASPECT_RATIO = 1
        private const val ASPECT_X = "aspectX"
        private const val ASPECT_Y = "aspectY"

        init {
            System.loadLibrary("NativeImageProcessor")
        }
    }

    private lateinit var binding: ActivityCropperBinding
    private var lastOtherAspectRatio: Pair<Float, Float>? = null
    private var currAspectRatio = ASPECT_RATIO_FREE
    private var isCropIntent = false
    private var oldExif: ExifInterface? = null
    private var sourceUri: Uri? = null
    private var resizeWidth = 0
    private var resizeHeight = 0
    private var currentRotateDegree = 0
    private var currentPerspectiveDegree = 0
    private var isHorizontalPerspective: Boolean? = null
    private var perspectiveBitmap: Bitmap? = null

    /*override fun getAppIconIDs(): ArrayList<Int> {
        return arrayListOf()
    }

    override fun getAppLauncherName(): String {
        return ""
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropperBinding.inflate(layoutInflater)
        //makeFullScreen()
        setContentView(binding.root)
        setupConfigs()
        initCropping()
    }

    private val options = arrayListOf(
        OptionModel(0, "Crop"),
        OptionModel(1, "Rotate"),
        OptionModel(2, "Perspective")
    )

    private val configsAdapter = ConfigsAdapter {
        when (it.id) {
            CROP_ORIGINAL -> {
                resetCropRatio()
            }
            CROP_FREE -> {
                updateAspectRatio(ASPECT_RATIO_FREE)
            }
            CROP_SQUARE -> {
                updateAspectRatio(ASPECT_RATIO_ONE_ONE)
            }
            CROP_3_4 -> {
                updateAspectRatio(ASPECT_RATIO_THREE_FOUR)
            }
            CROP_4_3 -> {
                updateAspectRatio(ASPECT_RATIO_FOUR_THREE)
            }
            ROTATE_LEFT -> {
                binding.civCropper.rotateImage(-90)
            }
            ROTATE_RIGHT -> {
                binding.civCropper.rotateImage(90)
            }
            ROTATE_MIRROR -> {
                binding.civCropper.flipImageHorizontally()
            }
            ROTATE_FLIP -> {
                binding.civCropper.flipImageVertically()
            }
            PERSPECTIVE_HORIZONTAL -> {
                isHorizontalPerspective = true
                currentPerspectiveDegree = 0
                transformHorizontalPerspective(0)
                binding.rlView.scrollToCenter()
            }
            PERSPECTIVE_VERTICAL -> {
                isHorizontalPerspective = false
                currentPerspectiveDegree = 0
                transformVerticalPerspective(0)
                binding.rlView.scrollToCenter()
            }
        }
    }

    private var selectedOption = options.first()

    private val cropConfigData = arrayListOf(
        ConfigUIModel(CROP_ORIGINAL, R.drawable.ic_crop_original, "Origin", false),
        ConfigUIModel(CROP_FREE, R.drawable.ic_crop_free, "Free", true),
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

    private fun initCropping() {
        loadCropImageView()
        if (config.lastEditorCropAspectRatio == ASPECT_RATIO_OTHER) {
            if (config.lastEditorCropOtherAspectRatioX == 0f) {
                config.lastEditorCropOtherAspectRatioX = 1f
            }

            if (config.lastEditorCropOtherAspectRatioY == 0f) {
                config.lastEditorCropOtherAspectRatioY = 1f
            }

            lastOtherAspectRatio = Pair(config.lastEditorCropOtherAspectRatioX, config.lastEditorCropOtherAspectRatioY)
        }
        updateAspectRatio(config.lastEditorCropAspectRatio)
    }

    override fun onDestroy() {
        super.onDestroy()
        config.lastEditorCropAspectRatio = ASPECT_RATIO_FREE
    }

    private fun updateAspectRatio(aspectRatio: Int) {
        currAspectRatio = aspectRatio
        config.lastEditorCropAspectRatio = aspectRatio

        binding.civCropper.apply {
            if (aspectRatio == ASPECT_RATIO_FREE) {
                setFixedAspectRatio(false)
            } else {
                val newAspectRatio = when (aspectRatio) {
                    ASPECT_RATIO_ONE_ONE -> Pair(1f, 1f)
                    ASPECT_RATIO_FOUR_THREE -> Pair(4f, 3f)
                    ASPECT_RATIO_THREE_FOUR -> Pair(3f, 4f)
                    ASPECT_RATIO_SIXTEEN_NINE -> Pair(16f, 9f)
                    else -> Pair(lastOtherAspectRatio!!.first, lastOtherAspectRatio!!.second)
                }

                setAspectRatio(newAspectRatio.first.toInt(), newAspectRatio.second.toInt())
            }
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
        binding.clOptions.visibility = View.VISIBLE
        binding.clOptions.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up))
        setupOptions()
    }

    private fun transformHorizontalPerspective(degree: Int) {
        perspectiveBitmap?.let { bitmap ->
            val w: Int = bitmap.getWidth()
            val h: Int = bitmap.getHeight()
            if (degree < 0) {
                val src = floatArrayOf(
                    0f, 0f,  // Coordinate of top left point
                    0f, h.toFloat(),  // Coordinate of bottom left point
                    w.toFloat(), h.toFloat(),  // Coordinate of bottom right point
                    w.toFloat(), 0f // Coordinate of top right point
                )

                val dst = floatArrayOf(
                    0f, (degree * (0.2f / RulerView.MAX_DEGREES)) * h,  // Desired coordinate of top left point
                    0f, (1 - (degree * (0.2f / RulerView.MAX_DEGREES))) * h,  // Desired coordinate of bottom left point
                    w.toFloat(), h.toFloat(),  // Coordinate of bottom right point
                    w.toFloat(), 0f // Coordinate of top right point
                )

                val transformed: Bitmap = cornerPin(bitmap, src, dst)
                binding.ivBackground.setImageBitmap(transformed)
            } else {
                val src = floatArrayOf(
                    0f, 0f,  // Coordinate of top left point
                    0f, h.toFloat(),  // Coordinate of bottom left point
                    w.toFloat(), h.toFloat(),  // Coordinate of bottom right point
                    w.toFloat(), 0f // Coordinate of top right point
                )

                val dst = floatArrayOf(
                    0f, 0f,  // Desired coordinate of top left point
                    0f, h.toFloat(),  // Desired coordinate of bottom left point
                    w.toFloat(), (1 - (-degree * (0.2f / RulerView.MAX_DEGREES))) * h,  // Coordinate of bottom right point
                    w.toFloat(), (-degree * (0.2f / RulerView.MAX_DEGREES)) * h // Coordinate of top right point
                )

                val transformed: Bitmap = cornerPin(bitmap, src, dst)
                binding.ivBackground.setImageBitmap(transformed)
            }
        }

    }

    private fun transformVerticalPerspective(degree: Int) {
        perspectiveBitmap?.let { bitmap ->
            val w: Int = bitmap.getWidth()
            val h: Int = bitmap.getHeight()
            if (degree < 0) {
                val src = floatArrayOf(
                    0f, 0f,  // Coordinate of top left point
                    0f, h.toFloat(),  // Coordinate of bottom left point
                    w.toFloat(), h.toFloat(),  // Coordinate of bottom right point
                    w.toFloat(), 0f // Coordinate of top right point
                )

                val dst = floatArrayOf(
                    0f, 0f,  // Desired coordinate of top left point
                    (degree * (0.2f / RulerView.MAX_DEGREES)) * w, h.toFloat(),  // Desired coordinate of bottom left point
                    (1 - (degree * (0.2f / RulerView.MAX_DEGREES))) * w, h.toFloat(),  // Coordinate of bottom right point
                    w.toFloat(), 0f // Coordinate of top right point
                )

                val transformed: Bitmap = cornerPin(bitmap, src, dst)
                binding.ivBackground.setImageBitmap(transformed)
            } else {
                val src = floatArrayOf(
                    0f, 0f,  // Coordinate of top left point
                    0f, h.toFloat(),  // Coordinate of bottom left point
                    w.toFloat(), h.toFloat(),  // Coordinate of bottom right point
                    w.toFloat(), 0f // Coordinate of top right point
                )

                val dst = floatArrayOf(
                    (-degree * (0.2f / RulerView.MAX_DEGREES)) * w, 0f,  // Desired coordinate of top left point
                    0f, h.toFloat(),  // Desired coordinate of bottom left point
                    w.toFloat(), h.toFloat(),  // Coordinate of bottom right point
                    (1 - (-degree * (0.2f / RulerView.MAX_DEGREES))) * w, 0f // Coordinate of top right point
                )

                val transformed: Bitmap = cornerPin(bitmap, src, dst)
                binding.ivBackground.setImageBitmap(transformed)
            }
        }

    }

    private fun setupOptionEvents() {
        binding.tvOption.setOnClickListener {
            selectOption()
        }

        binding.ivArrow.setOnClickListener {
            selectOption()
        }

        binding.tvCrop.setOnClickListener {
            binding.civCropper.isShowCropOverlay = true
            binding.civCropper.visibility = View.VISIBLE
            binding.clOptions.visibility = View.GONE
            binding.clOptions.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_down))
            selectedOption = options[0]
            binding.tvOption.text = getString(R.string.common_crop)

            submitConfigs()
            binding.rlView.visibility = View.INVISIBLE
        }

        binding.tvRotate.setOnClickListener {
            binding.civCropper.isShowCropOverlay = false
            binding.civCropper.visibility = View.VISIBLE
            binding.ivBackground.visibility = View.GONE
            binding.clOptions.visibility = View.GONE
            binding.clOptions.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_down))
            selectedOption = options[1]
            binding.tvOption.text = getString(R.string.common_rotate)
            submitConfigs()

            binding.rlView.visibility = View.VISIBLE
            if (currentRotateDegree == 0) {
                binding.rlView.scrollToCenter()
            }
        }

        binding.tvPerspective.setOnClickListener {
            binding.civCropper.isShowCropOverlay = false
            binding.civCropper.visibility = View.GONE
            binding.ivBackground.visibility = View.VISIBLE
            binding.clOptions.visibility = View.GONE
            binding.clOptions.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_down))
            selectedOption = options[2]
            binding.tvOption.text = getString(R.string.common_perspective)
            submitConfigs()

            binding.rlView.visibility = View.VISIBLE
            if (currentRotateDegree == 0) {
                binding.rlView.scrollToCenter()
            }
        }

        binding.rlView.setRulerListener { degree ->
            if (selectedOption.id == 1 && degree != currentRotateDegree) {
                binding.civCropper.rotateImage(degree - currentRotateDegree)
                currentRotateDegree = degree
            } else if (selectedOption.id == 2 &&
                isHorizontalPerspective == true &&
                degree != currentPerspectiveDegree) {
                currentPerspectiveDegree = degree
                transformHorizontalPerspective(degree)
            } else if (selectedOption.id == 2 &&
                isHorizontalPerspective == false &&
                degree != currentPerspectiveDegree) {
                currentPerspectiveDegree = degree
                transformVerticalPerspective(degree)
            }
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
            finish()
        }

        binding.ivCheck.setOnClickListener {
            if (currentPerspectiveDegree != 0) {
                val perspectiveBitmap = binding.ivBackground.drawToBitmap()
                Intent().apply {
                    data = BitmapUtils.getImageUriFromBitmap(this@CropperActivity, perspectiveBitmap)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setResult(RESULT_OK, this)
                }
                finish()
            } else {
                lifecycleScope.launch {
                    binding.civCropper.croppedImageAsync()
                }
            }
        }

        submitConfigs()
        setupOptionEvents()
        setupOptions()
    }

    private fun shouldCropSquare(): Boolean {
        val extras = intent.extras
        return if (extras != null && extras.containsKey(ASPECT_X) && extras.containsKey(ASPECT_Y)) {
            extras.getInt(ASPECT_X) == extras.getInt(ASPECT_Y)
        } else {
            false
        }
    }

    private fun resetCropRatio() {
        binding.civCropper.resetCropRect()
        currentRotateDegree = 0
        rotateConfigData.firstOrNull { it.selected }?.selected = false
        perspectiveConfigData.firstOrNull { it.selected }?.selected = false
    }

    private fun loadCropImageView() {
        (intent?.getParcelableExtra(PhotoActivity.PHOTO_CROPPING) as? Uri)?.let { pathUri ->
            sourceUri = pathUri
            binding.ivBackground.setImageURI(pathUri)

            try {
                val parcelFileDescriptor = contentResolver.openFileDescriptor(pathUri, "r")
                val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
                perspectiveBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)

                parcelFileDescriptor.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            //binding.ivBackground.visibility = View.VISIBLE
            binding.civCropper.run {
                visibility = View.VISIBLE
                setOnCropImageCompleteListener(this@CropperActivity)
                setImageUriAsync(sourceUri)
                //setImageBitmap(binding.ivPreview.drawable?.toBitmapOrNull())

                guidelines = CropImageView.Guidelines.ON

                if (isCropIntent && shouldCropSquare()) {
                    currAspectRatio = ASPECT_RATIO_ONE_ONE
                    setFixedAspectRatio(true)
                }
            }
        }
    }

    private fun setOldExif() {
        var inputStream: InputStream? = null
        try {
            inputStream = contentResolver.openInputStream(sourceUri!!)
            oldExif = ExifInterface(inputStream!!)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
    }

    override fun onCropImageComplete(view: CropImageView, result: CropImageView.CropResult) {
        if (result.error == null && result.bitmap != null) {
            setOldExif()

            val bitmap = result.bitmap!!
            Intent().apply {
                data = BitmapUtils.getImageUriFromBitmap(this@CropperActivity, bitmap)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setResult(RESULT_OK, this)
            }
            finish()
        } else {
            Log.e(CropperActivity::class.java.name, "${result.error?.message}")
        }
    }
}
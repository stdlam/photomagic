package com.poc.photoeditor.editor

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.text.TextUtils
import android.view.GestureDetector
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresPermission
import com.poc.photoeditor.R
import com.poc.photoeditor.editor.builder.TextStyleBuilder
import com.poc.photoeditor.editor.graphic.Graphic
import com.poc.photoeditor.editor.graphic.GraphicManager
import com.poc.photoeditor.editor.graphic.Text
import com.poc.photoeditor.editor.helper.BoxHelper
import com.poc.photoeditor.editor.helper.MultiTouchListener
import com.poc.photoeditor.editor.task.PhotoSaverTask
import com.poc.photoeditor.editor.view.PhotoEditorView
import com.poc.photoeditor.editor.view.SaveSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("ClickableViewAccessibility")
internal class PhotoEditorImpl(builder: PhotoEditor.Builder) : PhotoEditor {
    private val photoEditorView: PhotoEditorView = builder.photoEditorView
    private val viewState: PhotoEditorViewState = PhotoEditorViewState()
    private val imageView: ImageView = builder.imageView
    private val deleteView: View? = builder.deleteView
    private val mBoxHelper: BoxHelper = BoxHelper(builder.photoEditorView, viewState)
    private var mOnPhotoEditorListener: OnPhotoEditorListener? = null
    private val isTextPinchScalable: Boolean = builder.isTextPinchScalable
    private val mDefaultTextTypeface: Typeface? = builder.textTypeface
    private val mGraphicManager: GraphicManager = GraphicManager(builder.photoEditorView, viewState)
    private val context: Context = builder.context

    override fun addText(
        text: String,
        styleBuilder: TextStyleBuilder?,
        listener: Graphic.GraphicListener,
        isDuplicate: Boolean
    ) {
        val multiTouchListener = getMultiTouchListener(isTextPinchScalable)
        val textGraphic = Text(
            photoEditorView,
            multiTouchListener,
            viewState,
            mDefaultTextTypeface,
            mGraphicManager,
            listener
        )
        textGraphic.buildView(text, styleBuilder)
        addToEditor(textGraphic, isDuplicate)
    }

    override fun editText(text: Text, inputText: String, styleBuilder: TextStyleBuilder?) {
        val view = text.rootView
        val inputTextView = view.findViewById<TextView>(R.id.tvPhotoEditorText)
        if (inputTextView != null && viewState.containsAddedView(view) && !TextUtils.isEmpty(
                inputText
            )
        ) {
            inputTextView.text = inputText
            styleBuilder?.applyStyle(inputTextView)
            mGraphicManager.updateView(view)
        }
    }

    override fun addToEditor(graphic: Graphic, isDuplicate: Boolean) {
        clearHelperBox()
        mGraphicManager.addView(graphic, isDuplicate)
        // Change the in-focus view
        viewState.currentSelectedView = graphic.rootView
    }

    /**
     * Create a new instance and scalable touchview
     *
     * @param isPinchScalable true if make pinch-scalable, false otherwise.
     * @return scalable multitouch listener
     */
    private fun getMultiTouchListener(isPinchScalable: Boolean): MultiTouchListener {
        return MultiTouchListener(
            deleteView,
            photoEditorView,
            imageView,
            isPinchScalable,
            mOnPhotoEditorListener,
            viewState
        )
    }

    override fun clearAllViews() {
        mBoxHelper.clearAllViews()
    }

    override fun clearHelperBox() {
        mBoxHelper.clearHelperBox()
    }

    override fun setFilterEffect(customEffect: CustomEffect?) {
        photoEditorView.setFilterEffect(customEffect)
    }

    override fun setFilterEffect(filterType: PhotoFilter) {
        photoEditorView.setFilterEffect(filterType)
    }

    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    override suspend fun saveAsFile(
        imagePath: String,
        saveSettings: SaveSettings
    ): SaveFileResult = withContext(Dispatchers.Main) {
        photoEditorView.saveFilter()
        val photoSaverTask = PhotoSaverTask(photoEditorView, mBoxHelper, saveSettings)
        return@withContext photoSaverTask.saveImageAsFile(imagePath)
    }

    override suspend fun saveAsBitmap(
        saveSettings: SaveSettings
    ): Bitmap = withContext(Dispatchers.Main) {
        photoEditorView.saveFilter()
        val photoSaverTask = PhotoSaverTask(photoEditorView, mBoxHelper, saveSettings)
        return@withContext photoSaverTask.saveImageAsBitmap()
    }

    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    override fun saveAsFile(
        imagePath: String,
        saveSettings: SaveSettings,
        onSaveListener: PhotoEditor.OnSaveListener
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            when (val result = saveAsFile(imagePath, saveSettings)) {
                is SaveFileResult.Success -> onSaveListener.onSuccess(imagePath)
                is SaveFileResult.Failure -> onSaveListener.onFailure(result.exception)
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    override fun saveAsFile(imagePath: String, onSaveListener: PhotoEditor.OnSaveListener) {
        saveAsFile(imagePath, SaveSettings.Builder().build(), onSaveListener)
    }

    override fun saveAsBitmap(saveSettings: SaveSettings, onSaveBitmap: OnSaveBitmap) {
        CoroutineScope(Dispatchers.Default).launch {
            val bitmap = saveAsBitmap(saveSettings)
            onSaveBitmap.onBitmapReady(bitmap)
        }
    }

    override fun saveAsBitmap(onSaveBitmap: OnSaveBitmap) {
        saveAsBitmap(SaveSettings.Builder().build(), onSaveBitmap)
    }

    override fun setOnPhotoEditorListener(onPhotoEditorListener: OnPhotoEditorListener) {
        mOnPhotoEditorListener = onPhotoEditorListener
        mGraphicManager.onPhotoEditorListener = mOnPhotoEditorListener
    }

    override val isCacheEmpty: Boolean
        get() = viewState.addedViewsCount == 0 && viewState.redoViewsCount == 0

    companion object {
        private const val TAG = "PhotoEditor"
    }

    init {
        val mDetector = GestureDetector(
            context,
            PhotoEditorImageViewListener(
                viewState,
                object : PhotoEditorImageViewListener.OnSingleTapUpCallback {
                    override fun onSingleTapUp() {
                        clearHelperBox()
                    }
                }
            )
        )
        imageView.setOnTouchListener { _, event ->
            mOnPhotoEditorListener?.onTouchSourceImage(event)
            mDetector.onTouchEvent(event)
        }
        //photoEditorView.setClipSourceImage(builder.clipSourceImage)
    }
}


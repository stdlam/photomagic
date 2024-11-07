package com.poc.photoeditor.editor.graphic

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.poc.photoeditor.R
import com.poc.photoeditor.editor.PhotoEditorViewState
import com.poc.photoeditor.editor.builder.TextStyleBuilder
import com.poc.photoeditor.editor.helper.MultiTouchListener
import com.poc.photoeditor.editor.view.PhotoEditorView
import com.poc.photoeditor.editor.view.ViewType
import com.poc.photoeditor.provider.ui.model.TextConfig

class Text(
    private val mPhotoEditorView: PhotoEditorView,
    private val mMultiTouchListener: MultiTouchListener,
    private val mViewState: PhotoEditorViewState,
    private val mDefaultTextTypeface: Typeface?,
    private val mGraphicManager: GraphicManager,
    private val mGraphicListener: GraphicListener
) : Graphic(
    context = mPhotoEditorView.context,
    graphicManager = mGraphicManager,
    viewType = ViewType.TEXT,
    layoutId = R.layout.view_photo_editor_view
) {
    private var mTextView: TextView? = null
    private var mTextStyleBuilder: TextStyleBuilder? = null
    private var mScaleView: ImageView? = null

    fun getTextConfig() = TextConfig(
        mTextView?.text?.toString() ?: "",
        mTextStyleBuilder
    )

    fun saveConfig(config: TextConfig) {
        mTextView?.text = config.text
        mTextStyleBuilder = config.textStyleBuilder
    }

    fun buildView(text: String?, styleBuilder: TextStyleBuilder?) {
        mTextView?.apply {
            this.text = text
            styleBuilder?.applyStyle(this)
        }
        mTextStyleBuilder = styleBuilder
        setGraphicListener(mGraphicListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGesture() {
        val onGestureControl = buildGestureController(mPhotoEditorView, mViewState)
        val scaleView = rootView.findViewById<ImageView>(R.id.ivScale)
        mMultiTouchListener.setScaleView(scaleView, rootView)
        mMultiTouchListener.setOnGestureControl(onGestureControl)
        rootView.setOnTouchListener(mMultiTouchListener)
    }

    override fun setupView(rootView: View) {
        mTextView = rootView.findViewById(R.id.tvPhotoEditorText)
        mTextView?.run {
            gravity = Gravity.CENTER
            typeface = mDefaultTextTypeface
        }
    }

    override fun updateView(view: View) {
        val textInput = mTextView?.text.toString()
        val currentTextColor = mTextView?.currentTextColor ?: 0
        val photoEditorListener = mGraphicManager.onPhotoEditorListener
        photoEditorListener?.onEditTextChangeListener(view, textInput, currentTextColor)
    }

    init {
        setupGesture()
    }
}
package com.poc.photoeditor.editor.graphic

import android.content.Context
import android.graphics.Point
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.poc.photoeditor.R
import com.poc.photoeditor.editor.PhotoEditorViewState
import com.poc.photoeditor.editor.helper.BoxHelper
import com.poc.photoeditor.editor.helper.MultiTouchListener
import com.poc.photoeditor.editor.view.PhotoEditorView
import com.poc.photoeditor.editor.view.ViewType
import java.io.Serializable

abstract class Graphic(
    val context: Context,
    val layoutId: Int,
    val viewType: ViewType,
    val graphicManager: GraphicManager?) : Serializable {
    interface GraphicListener {
        fun onGraphicClicked(graphic: Graphic)
        fun onGraphicMoved(x: Int, y: Int)
        fun onGraphicScaled(x: Float, y: Float)
        fun onGraphicRotated(angle: Float)
        fun onGraphicRemoved()
        fun onGraphicDuplicate()
        fun onGraphicRect(point1: Point, point2: Point, point3: Point, point4: Point)
    }

    val rootView: View
    private var graphicListener: GraphicListener? = null

    open fun updateView(view: View) {
        //Optional for subclass to override
    }

    fun setGraphicListener(listener: GraphicListener) {
        this.graphicListener = listener
    }

    init {
        if (layoutId == 0) {
            throw UnsupportedOperationException("Layout id cannot be zero. Please define a layout")
        }
        rootView = LayoutInflater.from(context).inflate(layoutId, null)
        setupView(rootView)
        setupDuplicateView(rootView)
        setupRemoveView(rootView)
    }

    private fun setupDuplicateView(rootView: View) {
        val ivDuplicate = rootView.findViewById<ImageView>(R.id.ivDuplicate)
        ivDuplicate?.setOnClickListener {
            graphicListener?.onGraphicDuplicate()
        }
    }

    private fun setupRemoveView(rootView: View) {
        //We are setting tag as ViewType to identify what type of the view it is
        //when we remove the view from stack i.e onRemoveViewListener(ViewType viewType, int numberOfAddedViews);
        rootView.tag = viewType
        val imgClose = rootView.findViewById<ImageView>(R.id.ivDelete)
        imgClose?.setOnClickListener {
            graphicManager?.removeView(this@Graphic)
            graphicListener?.onGraphicRemoved()
        }
    }

    protected fun toggleSelection() {
        val frmBorder = rootView.findViewById<View>(R.id.frmBorder)
        val toolbar = rootView.findViewById<LinearLayout>(R.id.llToolbar)
        val scale = rootView.findViewById<ImageView>(R.id.ivScale)
        if (frmBorder != null) {
            frmBorder.setBackgroundResource(R.drawable.bg_text_graphic)
            frmBorder.tag = true
        }
        if (toolbar != null) {
            toolbar.visibility = View.VISIBLE
        }

        if (scale != null) {
            scale.visibility = View.VISIBLE
        }
    }

    protected fun buildGestureController(
        photoEditorView: PhotoEditorView,
        viewState: PhotoEditorViewState
    ): MultiTouchListener.OnGestureControl {
        val boxHelper = BoxHelper(photoEditorView, viewState)
        return object : MultiTouchListener.OnGestureControl {
            override fun onClick() {
                boxHelper.clearHelperBox()
                toggleSelection()
                // Change the in-focus view
                viewState.currentSelectedView = rootView
                graphicListener?.onGraphicClicked(this@Graphic)
            }

            override fun onLongClick() {
                updateView(rootView)
            }

            override fun onMove(x: Int, y: Int) {
                graphicListener?.onGraphicMoved(x, y)
            }

            override fun onRect(point1: Point, point2: Point, point3: Point, point4: Point) {
                graphicListener?.onGraphicRect(point1, point2, point3, point4)
            }

            override fun onScaled(x: Float, y: Float) {
                graphicListener?.onGraphicScaled(x, y)
            }

            override fun onRotated(angle: Float) {
                graphicListener?.onGraphicRotated(angle)
            }
        }
    }

    open fun setupView(rootView: View) {}
}
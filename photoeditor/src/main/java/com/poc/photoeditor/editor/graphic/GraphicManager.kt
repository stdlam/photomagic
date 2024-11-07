package com.poc.photoeditor.editor.graphic

import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.poc.photoeditor.editor.OnPhotoEditorListener
import com.poc.photoeditor.editor.PhotoEditorViewState
import com.poc.photoeditor.editor.view.PhotoEditorView
import com.poc.photoeditor.editor.view.ViewType

class GraphicManager(
    private val mPhotoEditorView: PhotoEditorView,
    private val mViewState: PhotoEditorViewState
) {
    var onPhotoEditorListener: OnPhotoEditorListener? = null
    fun addView(graphic: Graphic, isDuplicate: Boolean) {
        val view = graphic.rootView
        if (isDuplicate) {
            view.x += 10
            view.y += 10
        }

        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        params.addRule(RelativeLayout.ALIGN_PARENT_TOP and RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
        mPhotoEditorView.addView(view, params)
        mViewState.addAddedView(view)

        if (mViewState.redoViewsCount > 0) {
            mViewState.clearRedoViews()
        }

        onPhotoEditorListener?.onAddViewListener(
            graphic.viewType,
            graphic,
            view.translationX.toInt(),
            view.translationY.toInt()
        )
    }

    fun removeView(graphic: Graphic) {
        val view = graphic.rootView
        if (mViewState.containsAddedView(view)) {
            mPhotoEditorView.removeView(view)
            mViewState.removeAddedView(view)
            mViewState.pushRedoView(view)
            onPhotoEditorListener?.onRemoveViewListener(
                graphic.viewType,
                mViewState.addedViewsCount
            )
        }
    }

    fun updateView(view: View) {
        mPhotoEditorView.updateViewLayout(view, view.layoutParams)
        mViewState.replaceAddedView(view)
    }

    fun undoView(): Boolean {
        if (mViewState.addedViewsCount > 0) {
            val removeView = mViewState.getAddedView(
                mViewState.addedViewsCount - 1
            )
            mViewState.removeAddedView(mViewState.addedViewsCount - 1)
            mPhotoEditorView.removeView(removeView)
            mViewState.pushRedoView(removeView)
            when (val viewTag = removeView.tag) {
                is ViewType -> onPhotoEditorListener?.onRemoveViewListener(
                    viewTag,
                    mViewState.addedViewsCount
                )
            }
        }
        return mViewState.addedViewsCount != 0
    }

    fun redoView(): Boolean {
        if (mViewState.redoViewsCount > 0) {
            val redoView = mViewState.getRedoView(
                mViewState.redoViewsCount - 1
            )
            mViewState.popRedoView()
            mPhotoEditorView.addView(redoView)
            mViewState.addAddedView(redoView)
            when (redoView.tag) {
                is ViewType -> {
                    val location = IntArray(2)
                    redoView.getLocationOnScreen(location)
                    /*onPhotoEditorListener?.onAddViewListener(
                        viewTag,
                        location[0],
                        location[1]
                    )*/
                }
            }
        }
        return mViewState.redoViewsCount != 0
    }
}
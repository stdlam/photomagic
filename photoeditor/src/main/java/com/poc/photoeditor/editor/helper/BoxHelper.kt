package com.poc.photoeditor.editor.helper

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.poc.photoeditor.R
import com.poc.photoeditor.editor.PhotoEditorViewState
import com.poc.photoeditor.editor.view.PhotoEditorView

internal class BoxHelper(
    private val mPhotoEditorView: PhotoEditorView,
    private val mViewState: PhotoEditorViewState
) {
    fun clearHelperBox() {
        for (i in 0 until mPhotoEditorView.childCount) {
            val childAt = mPhotoEditorView.getChildAt(i)
            val frmBorder = childAt.findViewById<FrameLayout>(R.id.frmBorder)
            frmBorder?.setBackgroundResource(0)
            val toolbar = childAt.findViewById<LinearLayout>(R.id.llToolbar)
            val scale = childAt.findViewById<ImageView>(R.id.ivScale)
            toolbar?.visibility = View.GONE
            scale?.visibility = View.GONE
        }
        mViewState.clearCurrentSelectedView()
    }

    fun clearAllViews() {
        for (i in 0 until mViewState.addedViewsCount) {
            mPhotoEditorView.removeView(mViewState.getAddedView(i))
        }

        mViewState.clearAddedViews()
        mViewState.clearRedoViews()
    }
}
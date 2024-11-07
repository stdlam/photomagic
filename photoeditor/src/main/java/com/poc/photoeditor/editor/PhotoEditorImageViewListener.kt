package com.poc.photoeditor.editor

import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent

internal class PhotoEditorImageViewListener(
    private val viewState: PhotoEditorViewState,
    private val onSingleTapUpCallback: OnSingleTapUpCallback
) : SimpleOnGestureListener() {
    internal interface OnSingleTapUpCallback {
        fun onSingleTapUp()
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        onSingleTapUpCallback.onSingleTapUp()
        // Returning false when there is no in focus view will pass the
        // touch event to the zoom layout logic.
        return viewState.currentSelectedView != null
    }

    override fun onDown(e: MotionEvent) = viewState.currentSelectedView != null

    override fun onFling(
        e1: MotionEvent?,
        event1: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean = viewState.currentSelectedView != null

    override fun onScroll(
        e1: MotionEvent?,
        event1: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean = viewState.currentSelectedView != null

    override fun onDoubleTap(event: MotionEvent) = viewState.currentSelectedView != null

    override fun onDoubleTapEvent(event: MotionEvent) = viewState.currentSelectedView != null

    override fun onSingleTapConfirmed(event: MotionEvent) = viewState.currentSelectedView != null
}
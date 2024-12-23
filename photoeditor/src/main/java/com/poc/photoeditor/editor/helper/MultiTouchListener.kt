package com.poc.photoeditor.editor.helper

import android.annotation.SuppressLint
import android.graphics.Point
import android.graphics.Rect
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageView
import com.poc.photoeditor.editor.OnPhotoEditorListener
import com.poc.photoeditor.editor.PhotoEditorViewState
import com.poc.photoeditor.editor.view.PhotoEditorView
import com.poc.photoeditor.editor.view.ViewType
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min


class MultiTouchListener(
    deleteView: View?,
    photoEditorView: PhotoEditorView,
    photoEditImageView: ImageView?,
    private val mIsPinchScalable: Boolean,
    onPhotoEditorListener: OnPhotoEditorListener?,
    viewState: PhotoEditorViewState
) : OnTouchListener {
    private val mGestureListener: GestureDetector
    private var isRotateEnabled = true
    private val isTranslateEnabled = true
    private val isScaleEnabled = true
    private val minimumScale = 0.5f
    private val maximumScale = 10.0f
    private var mActivePointerId = INVALID_POINTER_ID
    private var mPrevX = 0f
    private var mPrevY = 0f
    private var mPrevRawX = 0f
    private var mPrevRawY = 0f
    private val mScaleGestureDetector: ScaleGestureDetector
    private val location = IntArray(2)
    private var outRect: Rect? = null
    private val deleteView: View?
    private val photoEditImageView: ImageView?
    private val photoEditorView: PhotoEditorView
    private var onMultiTouchListener: OnMultiTouchListener? = null
    private var mOnGestureControl: OnGestureControl? = null
    private val mOnPhotoEditorListener: OnPhotoEditorListener?
    private val viewState: PhotoEditorViewState
    private var anchorView: ImageView? = null

    fun disableRotation() {
        isRotateEnabled = false
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setScaleView(view: ImageView, outBound: View) {
        anchorView = view
        var centerX: Float
        var centerY: Float
        var startX = 0f
        var startY = 0f
        var startR = 0f
        var startScale = 0f

        anchorView?.setOnTouchListener { v, e ->
            Log.e("Touching", "onTouch scaleView - event=${e.action}")
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    // calculate center of image

                    centerX = (outBound.left + outBound.right) / 2f
                    centerY = (outBound.top + outBound.bottom) / 2f

                    // recalculate coordinates of starting point
                    startX = e.rawX - v.x + centerX
                    startY = e.rawY - v.y + centerY


                    // get starting distance and scale
                    startR = hypot(e.rawX - startX, e.rawY - startY)
                    startScale = outBound.scaleX
                }
                MotionEvent.ACTION_MOVE -> {
                    // calculate new distance

                    val newR = hypot(e.rawX - startX, e.getRawY() - startY)

                    // set new scale
                    val newScale: Float = newR / startR * startScale
                    outBound.scaleX = newScale
                    outBound.scaleY = newScale
                }
                MotionEvent.ACTION_UP -> {

                }
            }

            true
        }
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        Log.e("Touching", "onTouch - (${anchorView?.x}, ${anchorView?.y}, ${anchorView?.z}), (${view.x}, ${view.y}, ${view.z})")

        mScaleGestureDetector.onTouchEvent(view, event)
        mGestureListener.onTouchEvent(event)
        if (!isTranslateEnabled) {
            return true
        }
        val action = event.action
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()
        when (action and event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mPrevX = event.x
                mPrevY = event.y
                mPrevRawX = event.rawX
                mPrevRawY = event.rawY
                mActivePointerId = event.getPointerId(0)
                if (deleteView != null) {
                    deleteView.visibility = View.VISIBLE
                }
                view.bringToFront()
                firePhotoEditorSDKListener(view, true)
            }
            MotionEvent.ACTION_MOVE ->
                // Only enable dragging on focused stickers.
                if (view === viewState.currentSelectedView) {
                    val pointerIndexMove = event.findPointerIndex(mActivePointerId)
                    if (pointerIndexMove != -1) {
                        val currX = event.getX(pointerIndexMove)
                        val currY = event.getY(pointerIndexMove)
                        if (!mScaleGestureDetector.isInProgress) {
                            adjustTranslation(view, currX - mPrevX, currY - mPrevY)
                        }
                    }
                }
            MotionEvent.ACTION_CANCEL -> mActivePointerId = INVALID_POINTER_ID
            MotionEvent.ACTION_UP -> {
                mActivePointerId = INVALID_POINTER_ID
                if (deleteView != null && isViewInBounds(deleteView, x, y)) {
                    onMultiTouchListener?.onRemoveViewListener(view)
                } else if (!isViewInBounds(photoEditImageView, x, y)) {
                    view.animate().translationY(0f).translationY(0f)
                }
                if (deleteView != null) {
                    deleteView.visibility = View.GONE
                }
                firePhotoEditorSDKListener(view, false)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndexPointerUp =
                    action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
                val pointerId = event.getPointerId(pointerIndexPointerUp)
                if (pointerId == mActivePointerId) {
                    val newPointerIndex = if (pointerIndexPointerUp == 0) 1 else 0
                    mPrevX = event.getX(newPointerIndex)
                    mPrevY = event.getY(newPointerIndex)
                    mActivePointerId = event.getPointerId(newPointerIndex)
                }
            }
        }
        return true
    }

    private fun firePhotoEditorSDKListener(view: View, isStart: Boolean) {
        val viewTag = view.tag
        if (mOnPhotoEditorListener != null && viewTag != null && viewTag is ViewType) {
            if (isStart) mOnPhotoEditorListener.onStartViewChangeListener(view.tag as ViewType) else mOnPhotoEditorListener.onStopViewChangeListener(
                view.tag as ViewType
            )
        }
    }

    private fun getLocationBounded(view: View) {
        view.run {
            getDrawingRect(outRect)
            getLocationOnScreen(location)
            outRect?.offset(location[0], location[1])
        }
    }

    private fun isViewInBounds(view: View?, x: Int, y: Int): Boolean {
        return view?.run {
            getLocationBounded(this)
            outRect?.contains(x, y)
        } ?: false
    }

    fun setOnMultiTouchListener(onMultiTouchListener: OnMultiTouchListener?) {
        this.onMultiTouchListener = onMultiTouchListener
    }

    private inner class ScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private var mPivotX = 0f
        private var mPivotY = 0f
        private val mPrevSpanVector = Vector2D()

        override fun onScaleBegin(view: View, detector: ScaleGestureDetector): Boolean {
            mPivotX = detector.getFocusX()
            mPivotY = detector.getFocusY()
            mPrevSpanVector.set(detector.getCurrentSpanVector())
            return mIsPinchScalable
        }

        override fun onScale(view: View, detector: ScaleGestureDetector): Boolean {
            val info = TransformInfo()
            info.deltaScale = if (isScaleEnabled) detector.getScaleFactor() else 1.0f
            info.deltaAngle = if (isRotateEnabled) Vector2D.getAngle(
                mPrevSpanVector,
                detector.getCurrentSpanVector()
            ) else 0.0f
            info.deltaX = if (isTranslateEnabled) detector.getFocusX() - mPivotX else 0.0f
            info.deltaY = if (isTranslateEnabled) detector.getFocusY() - mPivotY else 0.0f
            info.pivotX = mPivotX
            info.pivotY = mPivotY
            info.minimumScale = minimumScale
            info.maximumScale = maximumScale
            move(view, info)
            return !mIsPinchScalable
        }
    }

    private inner class TransformInfo {
        var deltaX = 0f
        var deltaY = 0f
        var deltaScale = 0f
        var deltaAngle = 0f
        var pivotX = 0f
        var pivotY = 0f
        var minimumScale = 0f
        var maximumScale = 0f
    }

    interface OnMultiTouchListener {
        fun onEditTextClickListener(text: String, colorCode: Int)
        fun onRemoveViewListener(removedView: View)
    }

    interface OnGestureControl {
        fun onClick()
        fun onLongClick()

        fun onMove(x: Int, y: Int)

        fun onRect(point1: Point, point2: Point, point3: Point, point4: Point)
        fun onScaled(x: Float, y: Float)
        fun onRotated(angle: Float)
    }

    fun setOnGestureControl(onGestureControl: OnGestureControl?) {
        mOnGestureControl = onGestureControl
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            mOnGestureControl?.onClick()

            return true
        }

        override fun onLongPress(e: MotionEvent) {
            super.onLongPress(e)
            mOnGestureControl?.onLongClick()
        }
    }

    private fun adjustAngle(degrees: Float): Float {
        return when {
            degrees > 180.0f -> {
                degrees - 360.0f
            }
            degrees < -180.0f -> {
                degrees + 360.0f
            }
            else -> degrees
        }
    }

    private fun move(view: View, info: TransformInfo) {
        computeRenderOffset(view, info.pivotX, info.pivotY)
        adjustTranslation(view, info.deltaX, info.deltaY)
        var scale = view.scaleX * info.deltaScale
        scale = max(info.minimumScale, min(info.maximumScale, scale))
        view.scaleX = scale
        view.scaleY = scale
        mOnGestureControl?.onScaled(scale, scale)
        val rotation = adjustAngle(view.rotation + info.deltaAngle)
        view.rotation = rotation
        mOnGestureControl?.onRotated(rotation)
    }

    private fun adjustTranslation(view: View, deltaX: Float, deltaY: Float) {

        val deltaVector = floatArrayOf(deltaX, deltaY)
        view.matrix.mapVectors(deltaVector)
        view.translationX += deltaVector[0]
        view.translationY += deltaVector[1]

        getLocationBounded(view)
        mOnGestureControl?.onMove(location[0], location[1])
        mOnGestureControl?.onRect(
            Point(outRect?.left ?: 0, outRect?.top ?: 0),
            Point(outRect?.right ?: 0, outRect?.top ?: 0),
            Point(outRect?.right ?: 0, outRect?.bottom ?: 0),
            Point(outRect?.left ?: 0, outRect?.bottom ?: 0)
        )
    }

    private fun computeRenderOffset(view: View, pivotX: Float, pivotY: Float) {
        if (view.pivotX == pivotX && view.pivotY == pivotY) {
            return
        }
        val prevPoint = floatArrayOf(0.0f, 0.0f)
        view.matrix.mapPoints(prevPoint)
        view.pivotX = pivotX
        view.pivotY = pivotY
        val currPoint = floatArrayOf(0.0f, 0.0f)
        view.matrix.mapPoints(currPoint)
        val offsetX = currPoint[0] - prevPoint[0]
        val offsetY = currPoint[1] - prevPoint[1]
        view.translationX = view.translationX - offsetX
        view.translationY = view.translationY - offsetY
    }

    companion object {
        private const val INVALID_POINTER_ID = -1

    }

    init {
        mScaleGestureDetector = ScaleGestureDetector(ScaleGestureListener())
        mGestureListener = GestureDetector(GestureListener())
        this.deleteView = deleteView
        this.photoEditorView = photoEditorView
        this.photoEditImageView = photoEditImageView
        mOnPhotoEditorListener = onPhotoEditorListener
        outRect = if (deleteView != null) {
            Rect(
                deleteView.left, deleteView.top,
                deleteView.right, deleteView.bottom
            )
        } else {
            Rect(0, 0, 0, 0)
        }
        this.viewState = viewState
    }
}
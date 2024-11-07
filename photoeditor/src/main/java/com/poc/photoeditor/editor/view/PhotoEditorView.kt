package com.poc.photoeditor.editor.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.poc.photoeditor.R
import com.poc.photoeditor.editor.CustomEffect
import com.poc.photoeditor.editor.PhotoFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class PhotoEditorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {
    private var mImgSource: ImageSourceView = ImageSourceView(context)

    private var mImageFilterView: ImageFilterView
    private var clipSourceImage = true
    private var mSourceBitmap: Bitmap? = null
    private var bitmapInitialized = false
    private var initializer: BitmapInitListener? = null

    interface BitmapInitListener {
        fun onBitmapInitialized(bitmap: Bitmap?)
    }

    init {
        //Setup image attributes
        val sourceParam = setupImageSource(attrs)
        //Setup GLSurface attributes
        mImageFilterView = ImageFilterView(context)
        //val filterParam = setupFilterView()

        mImgSource.setOnImageChangedListener(object : ImageSourceView.OnImageChangedListener {
            override fun onBitmapLoaded(sourceBitmap: Bitmap?) {
                if (!bitmapInitialized) {
                    bitmapInitialized = true
                    initializer?.onBitmapInitialized(sourceBitmap)
                }
                mImageFilterView.setFilterEffect(PhotoFilter.ORIGIN)
                mImageFilterView.setSourceBitmap(sourceBitmap)
                mSourceBitmap = sourceBitmap
                Log.d(TAG, "onBitmapLoaded() called with: sourceBitmap = [$sourceBitmap]")
            }
        })

        //Add image source
        addView(mImgSource, sourceParam)

        //Add Gl FilterView
        //addView(mImageFilterView, filterParam)
    }

    fun setInitializer(initListener: BitmapInitListener) {
        this.initializer = initListener
    }

    @SuppressLint("Recycle")
    private fun setupImageSource(attrs: AttributeSet?): LayoutParams {
        mImgSource.id = imgSrcId
        mImgSource.adjustViewBounds = true
        //mImgSource.scaleType = ImageView.ScaleType.FIT_XY

        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.PhotoEditorView)
            val imgSrcDrawable = a.getDrawable(R.styleable.PhotoEditorView_photo_src)
            if (imgSrcDrawable != null) {
                mImgSource.setImageDrawable(imgSrcDrawable)
            }
        }

        val widthParam = ViewGroup.LayoutParams.MATCH_PARENT
        /*if (clipSourceImage) {
            widthParam = ViewGroup.LayoutParams.WRAP_CONTENT
        }*/
        val params = LayoutParams(
            widthParam, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.addRule(CENTER_IN_PARENT, TRUE)
        return params
    }

    private fun setupFilterViewParams(): LayoutParams {
        mImageFilterView.visibility = GONE
        mImageFilterView.id = glFilterId

        //Align brush to the size of image view
        val params = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.addRule(CENTER_IN_PARENT, TRUE)
        params.addRule(ALIGN_TOP, imgSrcId)
        params.addRule(ALIGN_BOTTOM, imgSrcId)
        return params
    }

    /**
     * Source image which you want to edit
     *
     * @return source ImageView
     */
    val source: ImageView
        get() = mImgSource

    val sourceBitmap: Bitmap?
        get() = mSourceBitmap

    internal suspend fun saveFilter(): Bitmap {
        return if (mImageFilterView.visibility == VISIBLE) {
            val saveBitmap = try {
                mImageFilterView.saveBitmap()
            } catch (t: Throwable) {
                throw RuntimeException("Couldn't save bitmap with filter", t)
            }
            coroutineScope {
                withContext(Dispatchers.Main) {
                    mImgSource.setImageBitmap(saveBitmap)
                    mImageFilterView.visibility = GONE
                }
            }
            saveBitmap
        } else {
            mImgSource.bitmap!!
        }
    }

    internal fun setFilterEffect(filterType: PhotoFilter) {
        mImageFilterView.visibility = VISIBLE
        mImageFilterView.setFilterEffect(filterType)
    }

    internal fun setFilterEffect(customEffect: CustomEffect?) {
        mImageFilterView.visibility = VISIBLE
        mImageFilterView.setFilterEffect(customEffect)
    }

    private fun setupFilterView() {
        val filterParam = setupFilterViewParams()
        mImageFilterView.layoutParams = filterParam
        addView(mImageFilterView, filterParam)
    }

    internal fun setClipSourceImage(clip: Boolean) {
        clipSourceImage = clip
        val param = setupImageSource(null)
        mImgSource.layoutParams = param

        setupFilterView()
    } // endregion

    companion object {
        private const val TAG = "PhotoEditorView"
        private const val imgSrcId = 1
        private const val shapeSrcId = 2
        private const val glFilterId = 3
    }
}
package com.poc.photoeditor.provider.widget

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class StartSpaceItemDecoration(private val context: Context, @androidx.annotation.DimenRes private val startSpace: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position > 0) {
            outRect.left = context.resources.getDimensionPixelSize(startSpace)
        }
    }
}
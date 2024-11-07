package com.poc.photoeditor.provider.ui.feature.filter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.ItemFilterBinding
import com.poc.photoeditor.editor.PhotoFilter
import com.poc.photoeditor.editor.view.ImageFilterView
import com.poc.photoeditor.provider.ui.feature.filter.model.FilterModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FilterAdapter(private val applyFilter: (FilterModel) -> Unit
) : RecyclerView.Adapter<FilterAdapter.ViewHolder>() {
    companion object {
        private const val PAYLOADS_UPDATE_SELECTED = "PAYLOADS_UPDATE_SELECTED"
        private const val PAYLOADS_UPDATE_UNSELECTED = "PAYLOADS_UPDATE_UNSELECTED"
    }

    private val filters = arrayListOf<FilterModel>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitData(data: ArrayList<FilterModel>) {
        filters.clear()
        filters.addAll(data)
        notifyDataSetChanged()
    }

    fun getSelectedFilter() = filters.firstOrNull { it.selected }

    fun getNonFilter() = filters.firstOrNull { it.filter == PhotoFilter.ORIGIN }

    fun selectFilter(filter: FilterModel) {
        filters.firstOrNull { it.filter == filter.filter }?.let { target ->
            val targetIndex = filters.indexOf(target)
            filters.firstOrNull { it.selected }?.let { selectedFilter ->
                val selectedIndex = filters.indexOf(selectedFilter)
                filters[selectedIndex].selected = false
                notifyItemChanged(selectedIndex, PAYLOADS_UPDATE_UNSELECTED)

                filters[targetIndex].selected = true
                notifyItemChanged(targetIndex, PAYLOADS_UPDATE_SELECTED)
            }
        }
    }

    inner class ViewHolder(val binding: ItemFilterBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { _: View? ->
                filters.firstOrNull { it.selected }?.let {
                    val index = filters.indexOf(it)
                    if (index == layoutPosition) return@setOnClickListener

                    filters[index].selected = false
                    notifyItemChanged(index, PAYLOADS_UPDATE_UNSELECTED)

                    filters[layoutPosition].selected = true
                    notifyItemChanged(layoutPosition, PAYLOADS_UPDATE_SELECTED)

                    applyFilter.invoke(
                        filters[layoutPosition]
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return filters.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
           return super.onBindViewHolder(holder, position, payloads)
        }
        payloads.forEach { payload ->
            when (payload) {
                PAYLOADS_UPDATE_SELECTED -> {
                    holder.binding.flSample.foreground = AppCompatResources.getDrawable(holder.itemView.context, R.drawable.bg_green_round_8)
                }

                PAYLOADS_UPDATE_UNSELECTED -> {
                    holder.binding.flSample.foreground = AppCompatResources.getDrawable(holder.itemView.context, R.drawable.bg_black_round_8)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val filter = filters[position]
        val context = holder.itemView.context
        holder.binding.tvFilterName.text = filter.name
        filter.bitmap?.let {
            holder.binding.ivSample.setImageBitmap(it)
        }

        if (filter.selected) {
            holder.binding.flSample.foreground = AppCompatResources.getDrawable(context, R.drawable.bg_green_round_8)
        } else {
            holder.binding.flSample.foreground = AppCompatResources.getDrawable(context, R.drawable.bg_black_round_8)
        }
    }
}
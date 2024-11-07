package com.poc.photoeditor.provider.ui.feature.text.configs.font.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.ItemTagBinding
import com.poc.photoeditor.provider.ui.feature.text.configs.font.model.FontFilterModel

class FontFilterAdapter(private val selectFilter: (FontFilterModel) -> Unit) : RecyclerView.Adapter<FontFilterAdapter.ViewHolder>() {
    companion object {
        private const val PAYLOADS_UPDATE_SELECTED = "PAYLOADS_UPDATE_SELECTED"
        private const val PAYLOADS_UPDATE_UNSELECTED = "PAYLOADS_UPDATE_UNSELECTED"
    }

    private val fontFilters = arrayListOf<FontFilterModel>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitData(data: ArrayList<FontFilterModel>) {
        fontFilters.clear()
        fontFilters.addAll(data)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemTagBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (layoutPosition >= 0) {
                    selectFilter.invoke(
                        fontFilters[layoutPosition]
                    )
                }

                fontFilters.firstOrNull { it.selected }?.let {
                    val index = fontFilters.indexOf(it)
                    if (index == layoutPosition) return@setOnClickListener

                    fontFilters[index].selected = false
                    notifyItemChanged(index, PAYLOADS_UPDATE_UNSELECTED)

                    fontFilters[layoutPosition].selected = true
                    notifyItemChanged(layoutPosition, PAYLOADS_UPDATE_SELECTED)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemTagBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = fontFilters.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fontFilter = fontFilters[position]
        holder.binding.tvTag.text = fontFilter.name
        if (fontFilter.selected) {
            holder.binding.tvTag.setBackgroundResource(R.drawable.bg_green_border_rounded_15)
        } else {
            holder.binding.tvTag.setBackgroundResource(R.drawable.bg_rounded_15)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            return super.onBindViewHolder(holder, position, payloads)
        }
        payloads.forEach { payload ->
            when (payload) {
                PAYLOADS_UPDATE_SELECTED -> {
                    holder.binding.tvTag.setBackgroundResource(R.drawable.bg_green_border_rounded_15)
                }

                PAYLOADS_UPDATE_UNSELECTED -> {
                    holder.binding.tvTag.setBackgroundResource(R.drawable.bg_rounded_15)
                }
            }
        }
    }
}
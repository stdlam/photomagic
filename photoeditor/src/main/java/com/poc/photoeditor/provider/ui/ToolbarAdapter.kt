package com.poc.photoeditor.provider.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.ItemToolBinding

class ToolbarAdapter(private val onItemSelected: OnItemSelected) : RecyclerView.Adapter<ToolbarAdapter.ViewHolder>() {
    interface OnItemSelected {
        fun onToolSelected(toolType: ToolType)
    }

    data class ToolModel(
        val name: String,
        val icon: Int,
        val type: ToolType
    )

    private val toolbars = arrayListOf<ToolModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemToolBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = toolbars[position]
        holder.binding.tvName.text = item.name
        holder.binding.ivTool.setImageResource(item.icon)
    }

    override fun getItemCount(): Int {
        return toolbars.size
    }

    inner class ViewHolder(val binding: ItemToolBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener { _: View? ->
                onItemSelected.onToolSelected(
                    toolbars[layoutPosition].type
                )
            }
        }
    }

    init {
        toolbars.add(ToolModel("Filter", R.drawable.ic_filter, ToolType.FILTER))
        toolbars.add(ToolModel("Adjust", R.drawable.ic_adjust, ToolType.ADJUST))
        toolbars.add(ToolModel("Crop", R.drawable.ic_crop, ToolType.CROP))
        toolbars.add(ToolModel("Text", R.drawable.ic_text, ToolType.TEXT))
    }
}
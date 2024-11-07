package com.poc.photoeditor.provider.ui.feature.text.configs.color

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.ItemColorConfigBinding
import com.poc.photoeditor.provider.ui.feature.text.configs.color.ColorAdapter.ViewHolder
import com.poc.photoeditor.provider.ui.feature.text.configs.color.model.ColorModel
import com.poc.photoeditor.provider.ui.feature.text.configs.color.model.ColorType

class ColorAdapter(
    includeNone: Boolean = false,
    includePicker: Boolean = true,
    val selectedColor: (ColorModel) -> Unit) : RecyclerView.Adapter<ViewHolder>() {
    companion object {
        private const val PAYLOADS_UPDATE_SELECTED = "PAYLOADS_UPDATE_SELECTED"
        private const val PAYLOADS_UPDATE_UNSELECTED = "PAYLOADS_UPDATE_UNSELECTED"
        private const val PAYLOADS_UPDATE_COLOR = "PAYLOADS_UPDATE_COLOR"
    }

    private val colors = arrayListOf<ColorModel>()

    fun getSelectedColor() = colors.firstOrNull { it.selected }?.color

    fun updateSelectedColor(color: Int) {
        colors.firstOrNull { it.color == color }?.let { configColor ->
            val configIndex = colors.indexOf(configColor)
            colors.firstOrNull { it.selected }?.let {
                val index = colors.indexOf(it)

                colors[index].selected = false
                notifyItemChanged(index, PAYLOADS_UPDATE_UNSELECTED)

                colors[configIndex].selected = true
                notifyItemChanged(configIndex, PAYLOADS_UPDATE_SELECTED)
            }
        } ?: kotlin.run {
            if (color != -1) {
                // picker selected
                colors.firstOrNull { it.type == ColorType.PICKER }?.let {
                    val pickerIndex = colors.indexOf(it)
                    colors.firstOrNull { it.selected }?.let {
                        val index = colors.indexOf(it)

                        colors[index].selected = false
                        notifyItemChanged(index, PAYLOADS_UPDATE_UNSELECTED)

                        colors[pickerIndex].selected = true
                        notifyItemChanged(pickerIndex, PAYLOADS_UPDATE_SELECTED)
                    }
                }
            } else {
                // non chosen
                colors.firstOrNull { it.type == ColorType.NONE }?.let {
                    val nonIndex = colors.indexOf(it)
                    colors.firstOrNull { it.selected }?.let {
                        val index = colors.indexOf(it)

                        colors[index].selected = false
                        notifyItemChanged(index, PAYLOADS_UPDATE_UNSELECTED)

                        colors[nonIndex].selected = true
                        notifyItemChanged(nonIndex, PAYLOADS_UPDATE_SELECTED)
                    }
                }
            }
        }
    }

    inner class ViewHolder(itemView: ItemColorConfigBinding) : RecyclerView.ViewHolder(itemView.root) {
        val binding: ItemColorConfigBinding = itemView

        init {
            itemView.root.setOnClickListener {
                if (layoutPosition >= 0) {
                    selectedColor.invoke(colors[layoutPosition])
                }

                colors.firstOrNull { it.selected }?.let {
                    val index = colors.indexOf(it)
                    if (index == layoutPosition) return@setOnClickListener

                    colors[index].selected = false
                    notifyItemChanged(index, PAYLOADS_UPDATE_UNSELECTED)

                    colors[layoutPosition].selected = true
                    notifyItemChanged(layoutPosition, PAYLOADS_UPDATE_SELECTED)
                }
            }
        }
    }

    fun changeSelectedColor(color: Int) {
        colors.firstOrNull { it.selected }?.let {
            val index = colors.indexOf(it)
            colors[index].color = color
            notifyItemChanged(index, PAYLOADS_UPDATE_COLOR)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemColorConfigBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false)
        )
    }

    override fun getItemCount(): Int {
        return colors.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            return super.onBindViewHolder(holder, position, payloads)
        }
        payloads.forEach { payload ->
            when (payload) {
                PAYLOADS_UPDATE_SELECTED -> {
                    holder.binding.cvCircle.foreground = AppCompatResources.getDrawable(holder.itemView.context, R.drawable.bg_circle_green_stroke)
                }

                PAYLOADS_UPDATE_UNSELECTED -> {
                    holder.binding.cvCircle.foreground = null
                }

                PAYLOADS_UPDATE_COLOR -> {
                    colors[position].color?.let {
                        holder.binding.ivColor.setCardBackgroundColor(ColorStateList.valueOf(it))
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = colors[position]
        model.icon?.let { drawableId ->
            holder.binding.ivColor.visibility = View.INVISIBLE
            holder.binding.ivIcon.visibility = View.VISIBLE
            holder.binding.ivIcon.setImageResource(drawableId)
        }

        model.color?.let { color ->
            if (color != -1) {
                holder.binding.ivColor.visibility = View.VISIBLE
                holder.binding.ivIcon.visibility = View.INVISIBLE
                holder.binding.ivColor.setCardBackgroundColor(ColorStateList.valueOf(color))
            }
        }

        if (model.selected) {
            holder.binding.cvCircle.foreground = AppCompatResources.getDrawable(holder.itemView.context, R.drawable.bg_circle_green_stroke)
        } else {
            holder.binding.cvCircle.foreground = null
        }
    }

    init {
        if (includeNone) {
            colors.add(ColorModel(R.drawable.ic_none, -1, type = ColorType.NONE, selected = true))
        }
        if (includePicker) {
            colors.add(ColorModel(R.drawable.ic_inject, null, type = ColorType.PICKER, selected = false))
        }
        colors.addAll(
            arrayListOf(
                ColorModel(null, Color.parseColor("#FF3938"), selected = !includeNone),
                ColorModel(null, Color.parseColor("#67E432"), selected = false),
                ColorModel(null, Color.parseColor("#3781FC"), selected = false),
                ColorModel(null, Color.parseColor("#FD9939"), selected = false),
                ColorModel(null, Color.parseColor("#F3FF37"), selected = false),
                ColorModel(null, Color.parseColor("#37EA50"), selected = false)
            )
        )
    }
}
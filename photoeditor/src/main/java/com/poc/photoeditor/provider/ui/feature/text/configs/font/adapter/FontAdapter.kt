package com.poc.photoeditor.provider.ui.feature.text.configs.font.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.FragmentFontsBinding
import com.poc.photoeditor.databinding.ItemFontBinding
import com.poc.photoeditor.provider.ui.feature.text.configs.font.model.FontModel
import java.io.File

class FontAdapter(private val fontSelected: (FontModel) -> Unit,
                  private val updateOldSelected: () -> Unit) : RecyclerView.Adapter<FontAdapter.ViewHolder>() {

    val fonts = arrayListOf<FontModel>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitData(data: ArrayList<FontModel>) {
        fonts.clear()
        fonts.addAll(data)

        notifyDataSetChanged()
    }

    fun selectFont(file: File) {
        fonts.firstOrNull { it.fontFile == file }?.let {
            val selectIndex = fonts.indexOf(it)
            fonts.firstOrNull { it.selected }?.let { oldSelect ->
                val oldIndex = fonts.indexOf(oldSelect)

                fonts[oldIndex].selected = false
                notifyItemChanged(oldIndex, PAYLOADS_UPDATE_UNSELECTED)

                fonts[selectIndex].selected = true
                notifyItemChanged(selectIndex, PAYLOADS_UPDATE_SELECTED)
            }
        }
    }

    fun getSelectedFont() = fonts.firstOrNull { it.selected }

    inner class ViewHolder(itemView: ViewBinding) : RecyclerView.ViewHolder(itemView.root) {
        val binding: ItemFontBinding = itemView as ItemFontBinding
        init {
            itemView.root.setOnClickListener {
                if (layoutPosition >= 0) {
                    fontSelected.invoke(
                        fonts[layoutPosition]
                    )
                }

                fonts.firstOrNull { it.selected }?.let {
                    val index = fonts.indexOf(it)
                    if (index == layoutPosition) return@setOnClickListener

                    fonts[index].selected = false
                    notifyItemChanged(index, PAYLOADS_UPDATE_UNSELECTED)

                    fonts[layoutPosition].selected = true
                    notifyItemChanged(layoutPosition, PAYLOADS_UPDATE_SELECTED)
                } ?: kotlin.run {
                    fonts[layoutPosition].selected = true
                    notifyItemChanged(layoutPosition, PAYLOADS_UPDATE_SELECTED)

                    updateOldSelected.invoke()
                }
            }
        }
    }

    companion object {
        private const val PAYLOADS_UPDATE_SELECTED = "PAYLOADS_UPDATE_SELECTED"
        private const val PAYLOADS_UPDATE_UNSELECTED = "PAYLOADS_UPDATE_UNSELECTED"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFontBinding.inflate(LayoutInflater.from(parent.context),
                parent,
                false))
    }

    override fun getItemCount(): Int {
        return fonts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            return super.onBindViewHolder(holder, position, payloads)
        }
        payloads.forEach { payload ->
            when (payload) {
                PAYLOADS_UPDATE_SELECTED -> {
                    val context = holder.itemView.context
                    holder.binding.tvFont.setTextColor(ResourcesCompat.getColor(context.resources, R.color.primary_green, null))
                }

                PAYLOADS_UPDATE_UNSELECTED -> {
                    val fontColor = fonts.first().fontColor
                    holder.binding.tvFont.setTextColor(Color.parseColor(fontColor))
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val font = fonts[position]
        val context = holder.itemView.context
        holder.binding.tvFont.text = font.name
        holder.binding.tvFont.typeface = Typeface.createFromFile(font.fontFile)
        if (font.selected) {
            holder.binding.tvFont.setTextColor(ResourcesCompat.getColor(context.resources, R.color.primary_green, null))
        } else {
            holder.binding.tvFont.setTextColor(Color.parseColor(font.fontColor))
        }
    }
}
package com.poc.photoeditor.provider.ui.feature.text.configs.spacing

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.ItemTextConfigBinding
import com.poc.photoeditor.provider.ui.model.TextAlign

class AlignAdapter(private val selector: (TextAlign) -> Unit) : RecyclerView.Adapter<AlignAdapter.ViewHolder>() {
    data class AlignModel(
        var selected: Boolean,
        val name: String,
        val icon: Int,
        val align: TextAlign
    )

    private val aligns = arrayListOf<AlignModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTextConfigBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
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
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = aligns[position]
        val context = holder.binding.clRoot.context
        holder.binding.tvName.text = item.name
        holder.binding.ivIcon.setImageResource(item.icon)
        if (item.selected) {
            holder.binding.cvCircle.foreground = AppCompatResources.getDrawable(context, R.drawable.bg_circle_green_stroke)
        } else {
            holder.binding.cvCircle.foreground = null
        }
    }

    override fun getItemCount(): Int {
        return aligns.size
    }

    fun selectAlign(gravity: Int) {
        aligns.firstOrNull { it.selected }?.let { align ->
            val index = aligns.indexOf(align)
            aligns[index].selected = false
            notifyItemChanged(index, PAYLOADS_UPDATE_UNSELECTED)

            val selectIndex = when (gravity) {
                Gravity.START -> 0
                Gravity.CENTER -> 1
                else -> 2
            }
            aligns[selectIndex].selected = true
            notifyItemChanged(index, PAYLOADS_UPDATE_SELECTED)
        }
    }

    inner class ViewHolder(val binding: ItemTextConfigBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener { _: View? ->
                aligns.firstOrNull { it.selected }?.let {
                    val index = aligns.indexOf(it)
                    if (index == layoutPosition) return@setOnClickListener

                    aligns[index].selected = false
                    notifyItemChanged(index, PAYLOADS_UPDATE_UNSELECTED)

                    aligns[layoutPosition].selected = true
                    notifyItemChanged(layoutPosition, PAYLOADS_UPDATE_SELECTED)

                    selector.invoke(
                        aligns[layoutPosition].align
                    )
                }
            }
        }
    }

    init {
        aligns.add(AlignModel(true,"Left", R.drawable.ic_align_left, TextAlign.LEFT))
        aligns.add(AlignModel(false,"Middle", R.drawable.ic_align_center, TextAlign.MIDDLE))
        aligns.add(AlignModel(false,"Right", R.drawable.ic_align_right, TextAlign.RIGHT))
    }

    companion object {
        private const val PAYLOADS_UPDATE_SELECTED = "PAYLOADS_UPDATE_SELECTED"
        private const val PAYLOADS_UPDATE_UNSELECTED = "PAYLOADS_UPDATE_UNSELECTED"
    }

}
package com.poc.photoeditor.provider.ui.feature.text.configs

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.ItemTextConfigBinding
import com.poc.photoeditor.provider.ui.model.ConfigUIModel

class ConfigsAdapter(
    private val selectedConfig: (ConfigUIModel) -> Unit) : RecyclerView.Adapter<ConfigsAdapter.ViewHolder>() {
    companion object {
        private const val PAYLOADS_UPDATE_SELECTED = "PAYLOADS_UPDATE_SELECTED"
        private const val PAYLOADS_UPDATE_UNSELECTED = "PAYLOADS_UPDATE_UNSELECTED"
    }

    private val configs = arrayListOf<ConfigUIModel>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitData(configData: ArrayList<ConfigUIModel>) {
        configs.clear()
        configs.addAll(configData)

        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemTextConfigBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                if (layoutPosition >= 0) {
                    selectedConfig.invoke(
                        configs[layoutPosition]
                    )
                }

                configs.firstOrNull { it.selected }?.let {
                    val index = configs.indexOf(it)
                    if (index == layoutPosition) return@setOnClickListener

                    configs[index].selected = false
                    notifyItemChanged(index, PAYLOADS_UPDATE_UNSELECTED)

                    configs[layoutPosition].selected = true
                    notifyItemChanged(layoutPosition, PAYLOADS_UPDATE_SELECTED)
                } ?: kotlin.run {
                    configs[layoutPosition].selected = true
                    notifyItemChanged(layoutPosition, PAYLOADS_UPDATE_SELECTED)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemTextConfigBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val config = configs[position]
        val context = holder.binding.clRoot.context
        holder.binding.tvName.text = config.name
        config.icon?.let {
            holder.binding.ivColor.visibility = View.GONE
            holder.binding.ivIcon.visibility = View.VISIBLE
            holder.binding.ivIcon.setImageResource(it)
        }

        config.color?.let {
            holder.binding.ivIcon.visibility = View.GONE
            holder.binding.ivColor.visibility = View.VISIBLE
            holder.binding.ivColor.setCardBackgroundColor(it)
        }

        if (config.selected) {
            holder.binding.cvCircle.foreground = AppCompatResources.getDrawable(context, R.drawable.bg_circle_green_stroke)
        } else {
            holder.binding.cvCircle.foreground = null
        }
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

    override fun getItemCount(): Int {
        return configs.size
    }
}
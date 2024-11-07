package com.poc.photoeditor.provider.ui.feature.text.configs.spacing

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.res.ResourcesCompat
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.FragmentSpacingBinding
import com.poc.photoeditor.provider.ui.base.EditorBaseFragment
import com.poc.photoeditor.provider.ui.model.TextAlign
import com.poc.photoeditor.provider.ui.utils.InflateAlias
import com.poc.photoeditor.provider.widget.StartSpaceItemDecoration

class SpacingFragmentEditor : EditorBaseFragment<FragmentSpacingBinding>() {
    override val bindingInflater: InflateAlias<FragmentSpacingBinding>
        get() = FragmentSpacingBinding::inflate

    private var align = TextAlign.LEFT

    private val alignAdapter = AlignAdapter {
        align = it
        getBackResult()
    }

    private fun getBackResult() {
        getTextBottomSheetDialog()?.onUpdateSpacing(
            align,
            binding?.sbCharacter?.progress ?: 0,
            binding?.sbLine?.progress ?: 0
        )
    }

    override fun onResume() {
        super.onResume()
        binding?.root?.requestLayout()
    }

    private fun setupConfig() {
        getTextBottomSheetDialog()?.getGravity()?.let { config ->
            val gravity = config.first
            val characterSpacing = config.second
            val lineSpacing = config.third
            alignAdapter.selectAlign(gravity)
            binding?.sbLine?.progress = lineSpacing
            binding?.tvLineValue?.text = "$lineSpacing%"
            binding?.sbCharacter?.progress = characterSpacing
            binding?.tvCharacterValue?.text = "$characterSpacing%"
        }
    }

    private fun setupColorViews() {
        val enableColor = ResourcesCompat.getColor(resources, R.color.white, null)
        binding?.tvCharacter?.setTextColor(enableColor)
        binding?.tvCharacterValue?.setTextColor(enableColor)
        binding?.sbCharacter?.thumbTintList = ColorStateList.valueOf(enableColor)
        binding?.sbCharacter?.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_green_progress, null)
        binding?.sbCharacter?.isEnabled = true

        binding?.tvLine?.setTextColor(enableColor)
        binding?.tvLineValue?.setTextColor(enableColor)
        binding?.sbLine?.thumbTintList = ColorStateList.valueOf(enableColor)
        binding?.sbLine?.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_green_progress, null)
        binding?.sbLine?.isEnabled = true
    }

    override fun setupViews() {
        super.setupViews()
        setupColorViews()
        setupConfig()
        binding?.rvAligns?.run {
            adapter = alignAdapter
            addItemDecoration(StartSpaceItemDecoration(context, R.dimen.long_24))
        }

        binding?.sbCharacter?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding?.tvCharacterValue?.text = "$progress%"
                getBackResult()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding?.sbLine?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding?.tvLineValue?.text = "$progress%"
                getBackResult()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }

}
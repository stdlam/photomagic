package com.poc.photoeditor.provider.ui.feature.text.configs

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.poc.photoeditor.provider.ui.feature.text.configs.background.BackgroundFragmentEditor
import com.poc.photoeditor.provider.ui.feature.text.configs.color.ColorFragmentEditor
import com.poc.photoeditor.provider.ui.feature.text.configs.font.FontsFragmentEditor
import com.poc.photoeditor.provider.ui.feature.text.configs.spacing.SpacingFragmentEditor
import com.poc.photoeditor.provider.ui.feature.text.configs.strokecolor.StrokeColorFragmentEditor

class ConfigPageAdapter(fragmentManager: FragmentManager, lifecycleOwner: Lifecycle)
    : FragmentStateAdapter(fragmentManager, lifecycleOwner) {
    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FontsFragmentEditor()
            1 -> ColorFragmentEditor()
            2 -> StrokeColorFragmentEditor()
            3 -> BackgroundFragmentEditor()
            else -> SpacingFragmentEditor()
        }
    }

}
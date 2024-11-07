package com.poc.photoeditor.provider.ui.feature.text

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.poc.photoeditor.R
import com.poc.photoeditor.databinding.DialogTextBinding
import com.poc.photoeditor.editor.builder.TextStyleBuilder
import com.poc.photoeditor.editor.model.TextBorder
import com.poc.photoeditor.provider.ui.DiscardConfirmBottomSheetDialog
import com.poc.photoeditor.provider.ui.base.BaseBottomSheetDialogFragment
import com.poc.photoeditor.provider.ui.feature.text.configs.ConfigPageAdapter
import com.poc.photoeditor.provider.ui.feature.text.configs.ConfigsAdapter
import com.poc.photoeditor.provider.ui.model.ConfigUIModel
import com.poc.photoeditor.provider.ui.model.TextAlign
import com.poc.photoeditor.provider.ui.model.TextConfig
import com.poc.photoeditor.provider.ui.utils.BlurHelper
import com.poc.photoeditor.provider.ui.utils.InflateFragmentAlias
import com.poc.photoeditor.provider.widget.StartSpaceItemDecoration
import java.io.File

class TextBottomSheetDialog(private val textConfig: TextConfig? = null)
    : BaseBottomSheetDialogFragment<DialogTextBinding>(), TextConfigHub {

    interface Callback {
        fun onTextUpdated(textConfig: TextConfig, isNew: Boolean)
        fun onSaveConfig(textConfig: TextConfig)
    }

    private val configData = arrayListOf(
        ConfigUIModel(0, R.drawable.ic_keyboard, "Keyboard", false),
        ConfigUIModel(1, R.drawable.ic_font, "Font", true),
        ConfigUIModel(2, R.drawable.ic_color, "Color", false),
        ConfigUIModel(3, R.drawable.ic_stroke_color, "Stroke", false),
        ConfigUIModel(4, R.drawable.ic_background, "Background", false),
        ConfigUIModel(5, R.drawable.ic_spacing, "Spacing", false)
    )

    override val mInflater: InflateFragmentAlias<DialogTextBinding>
        get() = DialogTextBinding::inflate

    private var callback: Callback? = null

    private var tmpConfig: TextConfig? = null

    private val textInputCallback = object : TextInputDialog.Callback {
        override fun apply(text: String, isEdit: Boolean) {
            tmpConfig?.text = text
            emitTextConfigChange(!isEdit)
        }

    }

    private val configsAdapter = ConfigsAdapter { selectedConfig ->
        when (selectedConfig.id) {
            0 -> {
                TextInputDialog(textConfig?.text ?: "").showDialog(childFragmentManager, textInputCallback)
            }
            else -> {
                binding.vpDetail.currentItem = selectedConfig.id - 1
            }
        }
    }

    override fun getTheme(): Int {
        return R.style.Radius40BottomSheetBackgroundDialog
    }

    private fun emitTextConfigChange(isNew: Boolean = false) {
        tmpConfig?.let {
            callback?.onTextUpdated(it, isNew)
        }
    }

    fun showDialog(fragmentManager: FragmentManager, callback: Callback
    ) {
        this.callback = callback
        try {
            val tagDialog = TextBottomSheetDialog::class.java.name
            val fragment = fragmentManager.findFragmentByTag(tagDialog)
            if (fragment == null) {
                showNow(fragmentManager, tagDialog)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupConfiguration() {
        binding.rvConfigs.run {
            addItemDecoration(StartSpaceItemDecoration(context, R.dimen.long_24))
            adapter = configsAdapter
            configsAdapter.submitData(configData)
        }

        binding.vpDetail.run {
            isUserInputEnabled = false
            adapter = ConfigPageAdapter(childFragmentManager, lifecycle)
        }

        if (textConfig == null) {
            TextInputDialog().showDialog(childFragmentManager, textInputCallback)
        }
    }

    private fun updateBlurBackground(isBlur: Boolean) {
        if (isBlur) {
            val bitmap = BlurHelper.takeScreenShot(dialog?.ownerActivity?.window)
            val blurredBitmap = BlurHelper.fastBlur(bitmap, 20)
            val blurDrawable = BitmapDrawable(resources, blurredBitmap)
            dialog?.window?.setBackgroundDrawable(blurDrawable)
        } else {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

    }

    override fun setupUI(binding: DialogTextBinding) {
        tmpConfig = TextConfig(
            text = textConfig?.text ?: "",
            textStyleBuilder = TextStyleBuilder().apply {
                textConfig?.textStyleBuilder?.values?.get(TextStyleBuilder.TextStyle.COLOR)?.let {
                    withTextColor(it as Pair<Int, Int>)
                }

                textConfig?.textStyleBuilder?.values?.get(TextStyleBuilder.TextStyle.FONT_FAMILY)?.let {
                    withTextFontFile(it as File)
                }

                textConfig?.textStyleBuilder?.values?.get(TextStyleBuilder.TextStyle.BACKGROUND)?.let {
                    withBackgroundColor(it as Pair<Int, Int>)
                }

                textConfig?.textStyleBuilder?.values?.get(TextStyleBuilder.TextStyle.BORDER)?.let {
                    withTextBorder(it as TextBorder)
                }

                textConfig?.textStyleBuilder?.values?.get(TextStyleBuilder.TextStyle.GRAVITY)?.let {
                    withGravity(it as Int)
                }

                textConfig?.textStyleBuilder?.values?.get(TextStyleBuilder.TextStyle.LINE_SPACING)?.let {
                    withLineSpacing(it as Int)
                }

                textConfig?.textStyleBuilder?.values?.get(TextStyleBuilder.TextStyle.LETTER_SPACING)?.let {
                    withLetterSpacing(it as Int)
                }
            }
        )

        dialog?.run {
            setCanceledOnTouchOutside(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setDimAmount(0.6f)
        }

        setupConfiguration()
        binding.ivCheck.setOnClickListener {
            tmpConfig?.let {
                callback?.onSaveConfig(it)
            }

            dismiss()
        }

        binding.ivClose.setOnClickListener {
            if (checkIfConfigDifference()) {
                DiscardConfirmBottomSheetDialog().showDialog(
                    childFragmentManager,
                    object : DiscardConfirmBottomSheetDialog.Callback {
                        override fun onShowed() {
                            updateBlurBackground(true)
                        }

                        override fun onCancel() {
                            updateBlurBackground(false)
                        }

                        override fun onDiscard() {
                            discard()
                            updateBlurBackground(false)
                            textConfig?.let {
                                callback?.onTextUpdated(it, false)
                            }
                            dismiss()
                        }

                    }
                )
            } else {
                dismiss()
            }
        }
    }

    private fun checkIfConfigDifference(): Boolean {
        val oldTextConfig = textConfig?.textStyleBuilder?.values
        val newTextConfig = tmpConfig?.textStyleBuilder?.values
        return textConfig?.text != tmpConfig?.text ||
                oldTextConfig?.get(TextStyleBuilder.TextStyle.COLOR) != newTextConfig?.get(TextStyleBuilder.TextStyle.COLOR) ||
                oldTextConfig?.get(TextStyleBuilder.TextStyle.BORDER) != newTextConfig?.get(TextStyleBuilder.TextStyle.BORDER) ||
                oldTextConfig?.get(TextStyleBuilder.TextStyle.BACKGROUND) != newTextConfig?.get(TextStyleBuilder.TextStyle.BACKGROUND) ||
                oldTextConfig?.get(TextStyleBuilder.TextStyle.FONT_FAMILY) != newTextConfig?.get(TextStyleBuilder.TextStyle.FONT_FAMILY) ||
                oldTextConfig?.get(TextStyleBuilder.TextStyle.GRAVITY) != newTextConfig?.get(TextStyleBuilder.TextStyle.GRAVITY) ||
                oldTextConfig?.get(TextStyleBuilder.TextStyle.LINE_SPACING) != newTextConfig?.get(TextStyleBuilder.TextStyle.LINE_SPACING) ||
                oldTextConfig?.get(TextStyleBuilder.TextStyle.LETTER_SPACING) != newTextConfig?.get(TextStyleBuilder.TextStyle.LETTER_SPACING)
    }

    private fun discard() {
        val oldTextConfig = textConfig?.textStyleBuilder?.values
        val newTextConfig = tmpConfig?.textStyleBuilder?.values
        callback?.onTextUpdated(
            TextConfig(
                textConfig?.text ?: "",
                TextStyleBuilder().apply {
                    newTextConfig?.get(TextStyleBuilder.TextStyle.FONT_FAMILY)?.let { fontFile ->
                        withTextFontFile(fontFile as File)
                    }

                    withTextColor(oldTextConfig?.get(TextStyleBuilder.TextStyle.COLOR) as? Pair<Int, Int> ?: Pair(Color.WHITE, 100))
                    withTextBorder((oldTextConfig?.get(TextStyleBuilder.TextStyle.BORDER) as? TextBorder) ?: TextBorder(0f, 0, 5, Pair(0, 0)))
                    withBackgroundColor(oldTextConfig?.get(TextStyleBuilder.TextStyle.BACKGROUND) as? Pair<Int, Int> ?: Pair(-1, 100))
                    withGravity(oldTextConfig?.get(TextStyleBuilder.TextStyle.GRAVITY) as? Int ?: Gravity.START)
                    withLetterSpacing(oldTextConfig?.get(TextStyleBuilder.TextStyle.LETTER_SPACING) as? Int ?: 0)
                    withLineSpacing(oldTextConfig?.get(TextStyleBuilder.TextStyle.LINE_SPACING) as? Int ?: 0)
                }
            ), false)
    }

    override fun onUpdateFont(font: File) {
        tmpConfig?.textStyleBuilder?.withTextFontFile(font)
        emitTextConfigChange()
    }

    override fun onUpdateColor(color: Int, opacity: Int) {
        tmpConfig?.textStyleBuilder?.withTextColor(Pair(color, opacity))
        emitTextConfigChange()
    }

    override fun onUpdateStrokeColor(color: Int, size: Int, opacity: Int) {
        tmpConfig?.textStyleBuilder?.withTextBorder(
            TextBorder(
                0f,
                0,
                size,
                Pair(color, opacity)
            )
        )
        emitTextConfigChange()
    }

    override fun onUpdateBackgroundColor(color: Int, opacity: Int) {
        tmpConfig?.textStyleBuilder?.withBackgroundColor(Pair(color, opacity))
        emitTextConfigChange()
    }

    override fun onUpdateSpacing(align: TextAlign, characterSpace: Int, lineSpace: Int) {
        tmpConfig?.textStyleBuilder?.run {
            withGravity(
                when (align) {
                    TextAlign.LEFT -> Gravity.START
                    TextAlign.MIDDLE -> Gravity.CENTER
                    TextAlign.RIGHT -> Gravity.END
                }
            )
            withLineSpacing(lineSpace)
            withLetterSpacing(characterSpace)
        }
        emitTextConfigChange()
    }

    override fun getFont(): File? {
        return textConfig?.textStyleBuilder?.values?.get(TextStyleBuilder.TextStyle.FONT_FAMILY) as? File
    }

    override fun getTextColor(): Pair<Int, Int>? {
        val color = textConfig?.textStyleBuilder?.values?.get(TextStyleBuilder.TextStyle.COLOR) as? Pair<Int, Int>
        return color
    }

    override fun getStrokeColor(): Triple<Int, Int, Int>? {
        val border = textConfig?.textStyleBuilder?.values?.get(TextStyleBuilder.TextStyle.BORDER) as? TextBorder
        return if (border != null)
            Triple(border.strokeColor.first, border.strokeColor.second, border.strokeWidth)
        else null
    }

    override fun getBackgroundColor(): Pair<Int, Int>? {
        val color = textConfig?.textStyleBuilder?.values?.get(TextStyleBuilder.TextStyle.BACKGROUND) as? Pair<Int, Int>
        return color
    }

    override fun getGravity(): Triple<Int, Int, Int> {
        val gravity = textConfig?.textStyleBuilder?.values?.get(TextStyleBuilder.TextStyle.GRAVITY) as? Int
        val characterSpacing = textConfig?.textStyleBuilder?.values?.get(TextStyleBuilder.TextStyle.LETTER_SPACING) as? Int
        val lineSpacing = textConfig?.textStyleBuilder?.values?.get(TextStyleBuilder.TextStyle.LINE_SPACING) as? Int

        return Triple(gravity ?: Gravity.START, characterSpacing ?: 0, lineSpacing ?: 0)
    }
}
package com.poc.photoeditor.provider.ui.utils

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur

fun BlurView.updateWindowForBlurs(activity: Activity, blursEnabled: Boolean) {
    if (!blursEnabled) {
        setBlurEnabled(false)
        return
    }
    setBlurEnabled(true)
    val radius = 20f
    val decorView = activity.window?.decorView
    // ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
    decorView?.findViewById<ViewGroup>(android.R.id.content)?.let { rootView ->
        val windowBackground = decorView.background

        setupWith(rootView, RenderScriptBlur(rootView.context)) // or RenderEffectBlur
            .setFrameClearDrawable(windowBackground) // Optional
            .setBlurRadius(radius)
    }
}

fun BlurView.updateDialogForBlur(rootView: ViewGroup, blursEnabled: Boolean) {
    if (!blursEnabled) {
        setBlurEnabled(false)
        return
    }
    setBlurEnabled(true)
    val radius = 20f
    setupWith(rootView, RenderScriptBlur(rootView.context)) // or RenderEffectBlur
        .setFrameClearDrawable(rootView.background) // Optional
        .setBlurRadius(radius)
}

val Context.config: Config get() = Config.newInstance(applicationContext)
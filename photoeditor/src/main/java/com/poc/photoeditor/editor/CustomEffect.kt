package com.poc.photoeditor.editor

import android.text.TextUtils
import java.util.HashMap

class CustomEffect private constructor(builder: Builder) {
    /**
     * @return Custom effect name from [android.media.effect.EffectFactory.createEffect]
     */
    val effectName: String = builder.mEffectName

    /**
     * @return map of key and value of parameters for [android.media.effect.Effect.setParameter]
     */
    val parameters: Map<String, Any>

    init {
        parameters = builder.parametersMap
    }

    class Builder(effectName: String) {
        val mEffectName: String
        val parametersMap: MutableMap<String, Any> = HashMap()

        /**
         * set parameter to the attributes with its value
         *
         * @param paramKey   attribute key for [android.media.effect.Effect.setParameter]
         * @param paramValue value for [android.media.effect.Effect.setParameter]
         * @return builder instance to setup multiple parameters
         */
        fun setParameter(paramKey: String, paramValue: Any): Builder {
            parametersMap[paramKey] = paramValue
            return this
        }

        /**
         * @return instance for custom effect
         */
        fun build(): CustomEffect {
            return CustomEffect(this)
        }

        /**
         * Initiate your custom effect
         *
         * @param effectName custom effect name from [android.media.effect.EffectFactory.createEffect]
         * @throws RuntimeException exception when effect name is empty
         */
        init {
            if (TextUtils.isEmpty(effectName)) {
                throw RuntimeException("Effect name cannot be empty.Please provide effect name from EffectFactory")
            }
            mEffectName = effectName
        }
    }
}
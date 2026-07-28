package com.example.data

import android.content.Context
import android.content.SharedPreferences

class AiConfigManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ai_config_prefs", Context.MODE_PRIVATE)

    fun getAiConfig(): AiConfig {
        val providerStr = prefs.getString("provider_type", AiProviderType.GEMINI_OFFICIAL.name) ?: AiProviderType.GEMINI_OFFICIAL.name
        val provider = try {
            AiProviderType.valueOf(providerStr)
        } catch (e: Exception) {
            AiProviderType.GEMINI_OFFICIAL
        }
        val baseUrl = prefs.getString("custom_base_url", "") ?: ""
        val apiKey = prefs.getString("custom_api_key", "") ?: ""
        val modelName = prefs.getString("custom_model_name", "") ?: ""

        return AiConfig(
            providerType = provider,
            customBaseUrl = baseUrl,
            customApiKey = apiKey,
            customModelName = modelName
        )
    }

    fun saveAiConfig(config: AiConfig) {
        prefs.edit()
            .putString("provider_type", config.providerType.name)
            .putString("custom_base_url", config.customBaseUrl)
            .putString("custom_api_key", config.customApiKey)
            .putString("custom_model_name", config.customModelName)
            .apply()
    }
}

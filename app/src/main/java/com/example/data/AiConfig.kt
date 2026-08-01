package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

data class AiConfig(
    val apiKey: String,
    val baseUrl: String = "https://generativelanguage.googleapis.com/",
    val modelName: String = "gemini-1.5-flash",
    val protocolType: String = "GEMINI", // GEMINI or OPENAI
    val provider: String = "GEMINI"
)

class AiConfigManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ai_config_prefs", Context.MODE_PRIVATE)

    fun getApiKey(): String {
        val savedKey = prefs.getString("api_key", "") ?: ""
        if (savedKey.isNotBlank()) return savedKey
        return try {
            val buildKey = BuildConfig.GEMINI_API_KEY
            if (buildKey.isBlank() || buildKey == "MY_GEMINI_API_KEY") "" else buildKey
        } catch (e: Exception) {
            ""
        }
    }

    fun saveApiKey(key: String) {
        prefs.edit().putString("api_key", key.trim()).apply()
    }

    fun getBaseUrl(): String {
        val savedUrl = prefs.getString("base_url", "") ?: ""
        if (savedUrl.isNotBlank()) return savedUrl
        return "https://generativelanguage.googleapis.com/"
    }

    fun saveBaseUrl(url: String) {
        val formatted = if (url.isBlank()) "https://generativelanguage.googleapis.com/"
        else if (!url.endsWith("/")) "$url/"
        else url
        prefs.edit().putString("base_url", formatted.trim()).apply()
    }

    fun getModelName(): String {
        val savedModel = prefs.getString("model_name", "") ?: ""
        if (savedModel.isNotBlank()) return savedModel
        return "gemini-1.5-flash"
    }

    fun saveModelName(model: String) {
        val formatted = if (model.isBlank()) "gemini-1.5-flash" else model.trim()
        prefs.edit().putString("model_name", formatted).apply()
    }

    fun getProtocolType(): String {
        val savedProtocol = prefs.getString("protocol_type", "") ?: ""
        if (savedProtocol.isNotBlank()) return savedProtocol
        val url = getBaseUrl()
        return if (url.contains("googleapis.com")) "GEMINI" else "OPENAI"
    }

    fun saveProtocolType(protocol: String) {
        prefs.edit().putString("protocol_type", protocol).apply()
    }

    fun getProvider(): String {
        return prefs.getString("provider", "GEMINI") ?: "GEMINI"
    }

    fun saveProvider(provider: String) {
        prefs.edit().putString("provider", provider).apply()
    }

    fun getConfig(): AiConfig {
        return AiConfig(
            apiKey = getApiKey(),
            baseUrl = getBaseUrl(),
            modelName = getModelName(),
            protocolType = getProtocolType(),
            provider = getProvider()
        )
    }

    fun applyPreset(providerKey: String) {
        saveProvider(providerKey)
        when (providerKey) {
            "GEMINI" -> {
                saveBaseUrl("https://generativelanguage.googleapis.com/")
                saveModelName("gemini-1.5-flash")
                saveProtocolType("GEMINI")
            }
            "DEEPSEEK" -> {
                saveBaseUrl("https://api.deepseek.com/")
                saveModelName("deepseek-chat")
                saveProtocolType("OPENAI")
            }
            "SILICONFLOW" -> {
                saveBaseUrl("https://api.siliconflow.cn/v1/")
                saveModelName("deepseek-ai/DeepSeek-V3")
                saveProtocolType("OPENAI")
            }
            "QWEN" -> {
                saveBaseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1/")
                saveModelName("qwen-turbo")
                saveProtocolType("OPENAI")
            }
            "KIMI" -> {
                saveBaseUrl("https://api.moonshot.cn/v1/")
                saveModelName("moonshot-v1-8k")
                saveProtocolType("OPENAI")
            }
            "CUSTOM" -> {
                saveProtocolType("OPENAI")
            }
        }
    }
}

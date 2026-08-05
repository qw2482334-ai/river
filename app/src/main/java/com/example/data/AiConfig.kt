package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.UUID

data class AiConfig(
    val apiKey: String,
    val baseUrl: String = "https://generativelanguage.googleapis.com/",
    val modelName: String = "gemini-1.5-flash",
    val protocolType: String = "GEMINI", // GEMINI or OPENAI
    val provider: String = "GEMINI"
)

data class CustomApiProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val baseUrl: String,
    val apiKey: String,
    val modelName: String,
    val protocolType: String = "OPENAI"
)

class AiConfigManager(context: Context? = null) {
    private val prefs: SharedPreferences? =
        context?.getSharedPreferences("ai_config_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun getApiKey(): String {
        val savedKey = prefs?.getString("api_key", "") ?: ""
        if (savedKey.isNotBlank()) return savedKey
        return try {
            val buildKey = BuildConfig.GEMINI_API_KEY
            if (buildKey.isBlank() || buildKey == "MY_GEMINI_API_KEY") "" else buildKey
        } catch (e: Exception) {
            ""
        }
    }

    fun saveApiKey(key: String) {
        prefs?.edit()?.putString("api_key", key.trim())?.apply()
    }

    fun getBaseUrl(): String {
        val savedUrl = prefs?.getString("base_url", "") ?: ""
        if (savedUrl.isNotBlank()) return savedUrl
        return "https://generativelanguage.googleapis.com/"
    }

    fun saveBaseUrl(url: String) {
        val formatted = if (url.isBlank()) "https://generativelanguage.googleapis.com/"
        else if (!url.endsWith("/")) "$url/"
        else url
        prefs?.edit()?.putString("base_url", formatted.trim())?.apply()
    }

    fun getModelName(): String {
        val savedModel = prefs?.getString("model_name", "") ?: ""
        if (savedModel.isNotBlank()) return savedModel
        return "gemini-1.5-flash"
    }

    fun saveModelName(model: String) {
        val formatted = if (model.isBlank()) "gemini-1.5-flash" else model.trim()
        prefs?.edit()?.putString("model_name", formatted)?.apply()
    }

    fun getProtocolType(): String {
        val savedProtocol = prefs?.getString("protocol_type", "") ?: ""
        if (savedProtocol.isNotBlank()) return savedProtocol
        val url = getBaseUrl()
        return if (url.contains("googleapis.com")) "GEMINI" else "OPENAI"
    }

    fun saveProtocolType(protocol: String) {
        prefs?.edit()?.putString("protocol_type", protocol)?.apply()
    }

    fun getProvider(): String {
        return prefs?.getString("provider", "GEMINI") ?: "GEMINI"
    }

    fun saveProvider(provider: String) {
        prefs?.edit()?.putString("provider", provider)?.apply()
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

    fun getCachedModelList(baseUrl: String): List<String> {
        val cleanUrl = baseUrl.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
        val key = "cached_models_$cleanUrl"
        val json = prefs?.getString(key, "") ?: ""
        if (json.isBlank()) return emptyList()
        return try {
            val type = Types.newParameterizedType(List::class.java, String::class.java)
            val adapter = moshi.adapter<List<String>>(type)
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCachedModelList(baseUrl: String, models: List<String>) {
        if (baseUrl.isBlank() || models.isEmpty()) return
        val cleanUrl = baseUrl.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
        val key = "cached_models_$cleanUrl"
        try {
            val type = Types.newParameterizedType(List::class.java, String::class.java)
            val adapter = moshi.adapter<List<String>>(type)
            val json = adapter.toJson(models)
            prefs?.edit()?.putString(key, json)?.apply()
        } catch (e: Exception) {
            // ignore
        }
    }

    // --- Custom Multi-Profile Management ---
    fun getCustomProfiles(): List<CustomApiProfile> {
        val json = prefs?.getString("custom_profiles_json", "") ?: ""
        if (json.isBlank()) return emptyList()
        return try {
            val type = Types.newParameterizedType(List::class.java, CustomApiProfile::class.java)
            val adapter = moshi.adapter<List<CustomApiProfile>>(type)
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCustomProfiles(profiles: List<CustomApiProfile>) {
        try {
            val type = Types.newParameterizedType(List::class.java, CustomApiProfile::class.java)
            val adapter = moshi.adapter<List<CustomApiProfile>>(type)
            val json = adapter.toJson(profiles)
            prefs?.edit()?.putString("custom_profiles_json", json)?.apply()
        } catch (e: Exception) {
            // ignore
        }
    }

    fun addCustomProfile(profile: CustomApiProfile) {
        val current = getCustomProfiles().toMutableList()
        current.add(profile)
        saveCustomProfiles(current)
    }

    fun deleteCustomProfile(profileId: String) {
        val current = getCustomProfiles().filterNot { it.id == profileId }
        saveCustomProfiles(current)
    }

    fun applyCustomProfile(profile: CustomApiProfile) {
        saveProvider("CUSTOM_${profile.id}")
        saveBaseUrl(profile.baseUrl)
        saveApiKey(profile.apiKey)
        saveModelName(profile.modelName)
        saveProtocolType(profile.protocolType)
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
            "KIMI" -> {
                saveBaseUrl("https://api.moonshot.cn/v1/")
                saveModelName("moonshot-v1-8k")
                saveProtocolType("OPENAI")
            }
            "T8STAR" -> {
                saveBaseUrl("https://ai.t8star.org/v1/")
                saveModelName("gpt-3.5-turbo")
                saveProtocolType("OPENAI")
            }
            "AIBH" -> {
                saveBaseUrl("https://api.aibh.site/v1/")
                saveModelName("gpt-3.5-turbo")
                saveProtocolType("OPENAI")
            }
            "MOJIE" -> {
                saveBaseUrl("https://api.mojieai.top/v1/")
                saveModelName("gpt-3.5-turbo")
                saveProtocolType("OPENAI")
            }
            "IMAGELFK" -> {
                saveBaseUrl("https://imagelfk.cc.cd/v1/")
                saveModelName("gpt-3.5-turbo")
                saveProtocolType("OPENAI")
            }
            "VOLCENGINE" -> {
                saveBaseUrl("https://ark.cn-beijing.volces.com/api/v3/")
                saveModelName("doubao-lite-4k-240328")
                saveProtocolType("OPENAI")
            }
            "OPENROUTER" -> {
                saveBaseUrl("https://openrouter.ai/api/v1/")
                saveModelName("google/gemini-2.5-flash")
                saveProtocolType("OPENAI")
            }
            "MINIMAX" -> {
                saveBaseUrl("https://api.minimaxi.com/v1/")
                saveModelName("MiniMax-Text-01")
                saveProtocolType("OPENAI")
            }
            "ZHIPU" -> {
                saveBaseUrl("https://open.bigmodel.cn/api/paas/v4/")
                saveModelName("glm-4")
                saveProtocolType("OPENAI")
            }
            "QWEN" -> {
                saveBaseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1/")
                saveModelName("qwen-turbo")
                saveProtocolType("OPENAI")
            }
            "NVIDIA" -> {
                saveBaseUrl("https://integrate.api.nvidia.com/v1/")
                saveModelName("meta/llama-3.1-70b-instruct")
                saveProtocolType("OPENAI")
            }
            "APIMART_1" -> {
                saveBaseUrl("https://api.apimart.ai/v1/")
                saveModelName("gpt-4o")
                saveProtocolType("OPENAI")
            }
            "APIMART_2" -> {
                saveBaseUrl("https://api.apib.ai/v1/")
                saveModelName("gpt-4o")
                saveProtocolType("OPENAI")
            }
            "APIMART_3" -> {
                saveBaseUrl("https://api.aiuxu.com/v1/")
                saveModelName("gpt-4o")
                saveProtocolType("OPENAI")
            }
            "APIMART_4" -> {
                saveBaseUrl("https://api.aishuch.com/v1/")
                saveModelName("gpt-4o")
                saveProtocolType("OPENAI")
            }
            "CUSTOM" -> {
                saveProtocolType("OPENAI")
            }
        }
    }
}

package com.example.data

enum class AiProviderType(val displayName: String, val defaultUrl: String, val defaultModel: String) {
    GEMINI_OFFICIAL("Google Gemini 官方 (默认)", "https://generativelanguage.googleapis.com", "gemini-3.5-flash"),
    DEEPSEEK("DeepSeek 官方 API (国内推荐)", "https://api.deepseek.com/v1", "deepseek-chat"),
    SILICON_FLOW("硅基流动 SiliconFlow (国内极速)", "https://api.siliconflow.cn/v1", "deepseek-ai/DeepSeek-V3"),
    QWEN("阿里通义千问 Qwen", "https://dashscope.aliyuncs.com/compatible-mode/v1", "qwen-turbo"),
    MOONSHOT("Moonshot / Kimi 智能体", "https://api.moonshot.cn/v1", "moonshot-v1-8k"),
    CUSTOM_OPENAI("自定义 OpenAI / 代理中转站", "https://api.openai.com/v1", "gpt-3.5-turbo")
}

data class AiConfig(
    val providerType: AiProviderType = AiProviderType.GEMINI_OFFICIAL,
    val customBaseUrl: String = "",
    val customApiKey: String = "",
    val customModelName: String = ""
) {
    fun getEffectiveBaseUrl(): String {
        return if (customBaseUrl.isNotBlank()) {
            customBaseUrl.trimEnd('/')
        } else {
            providerType.defaultUrl
        }
    }

    fun getEffectiveApiKey(fallbackKey: String = ""): String {
        return if (customApiKey.isNotBlank()) {
            customApiKey.trim()
        } else {
            fallbackKey
        }
    }

    fun getEffectiveModelName(): String {
        return if (customModelName.isNotBlank()) {
            customModelName.trim()
        } else {
            providerType.defaultModel
        }
    }

    fun isOpenAiCompatible(): Boolean {
        return providerType != AiProviderType.GEMINI_OFFICIAL
    }
}

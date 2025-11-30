package com.dreamaichat.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "chat")
data class ModelProviderProperties(
    var defaultProvider: String = "deepseek",
    var providers: Map<String, Provider> = emptyMap()
) {
    data class Provider(
        var baseUrl: String = "",
        var apiKey: String = "",
        var model: String = "",
        var temperature: Double = 0.7
    )
}

package com.dreamaichat.backend

import com.dreamaichat.backend.config.ModelProviderProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(ModelProviderProperties::class)
class DreamAiChatBackendApplication

fun main(args: Array<String>) {
    runApplication<DreamAiChatBackendApplication>(*args)
}

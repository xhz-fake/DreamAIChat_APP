package com.dreamaichat.backend.service

import com.dreamaichat.backend.config.ModelProviderProperties
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.http.HttpStatus
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class ModelRouterService(
    private val properties: ModelProviderProperties,
    private val webClientBuilder: WebClient.Builder
) {

    companion object {
        private const val DEMO_KEY = "DEMO-KEY"
    }

    data class MessagePayload(
        val role: String,
        val content: String
    )

    data class ProviderResult(
        val provider: String,
        val reply: String,
        val latency: Long,
        val tokens: Int?
    )

    fun generateReply(providerKey: String, history: List<MessagePayload>): ProviderResult {
        val provider = properties.providers[providerKey]
            ?: throw IllegalArgumentException("Unknown model: $providerKey")

        if (provider.apiKey.isBlank() || provider.apiKey == DEMO_KEY) {
            val providerName = when (providerKey) {
                "deepseek" -> "DeepSeek"
                "doubao" -> "豆包"
                else -> providerKey
            }
            return buildDemoReply(providerKey, history, "当前模型未配置真实 API Key，已启用示例回复。请配置环境变量 ${providerKey.uppercase()}_API_KEY")
        }

        val payload = mapOf(
            "model" to provider.model,
            "temperature" to provider.temperature,
            "messages" to history.map { mapOf("role" to it.role, "content" to it.content) }
        )
        val start = System.currentTimeMillis()
        
        // 添加日志输出，帮助调试
        println("=== 调用模型: $providerKey ===")
        println("Base URL: ${provider.baseUrl}")
        println("Model: ${provider.model}")
        println("API Key 前10个字符: ${provider.apiKey.take(10)}...")
        
        val responseNode = try {
            val webClient = webClientBuilder.baseUrl(provider.baseUrl).build()
            val requestBuilder = webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
            
            // 根据不同的 provider 使用不同的认证方式
            when (providerKey) {
                "doubao" -> {
                    // 豆包可能使用不同的认证方式
                    // 尝试1: Bearer token（标准方式）
                    // 尝试2: 直接使用 API Key（如果 Bearer 不工作）
                    if (provider.apiKey.startsWith("Bearer ") || provider.apiKey.startsWith("bearer ")) {
                        // 如果 API Key 已经包含 Bearer 前缀，直接使用
                        requestBuilder.header(HttpHeaders.AUTHORIZATION, provider.apiKey)
                    } else {
                        // 否则添加 Bearer 前缀
                        requestBuilder.header(HttpHeaders.AUTHORIZATION, "Bearer ${provider.apiKey}")
                    }
                    println("使用认证方式: Bearer token")
                }
                else -> {
                    // DeepSeek 和其他模型使用 Bearer token
                    requestBuilder.header(HttpHeaders.AUTHORIZATION, "Bearer ${provider.apiKey}")
                    println("使用认证方式: Bearer token")
                }
            }
            
            println("发送请求到: ${provider.baseUrl}")
            requestBuilder
                .retrieve()
                .bodyToMono(JsonNode::class.java)
                .timeout(Duration.ofSeconds(120))  // 增加到 120 秒，支持复杂问题的长响应
                .onErrorResume {
                    println("请求错误: ${it.message}")
                    Mono.error(IllegalStateException("Model call failed: ${it.message}", it))
                }
                .block()
                ?: throw IllegalStateException("Model returned empty response")
        } catch (ex: WebClientResponseException) {
            val providerName = when (providerKey) {
                "deepseek" -> "DeepSeek"
                "doubao" -> "豆包"
                else -> providerKey
            }
            println("HTTP 错误: ${ex.statusCode} - ${ex.message}")
            println("响应体: ${ex.responseBodyAsString}")
            when (ex.statusCode) {
                HttpStatus.UNAUTHORIZED -> {
                    return buildDemoReply(
                        providerKey,
                        history,
                        "模型返回 401 未授权，请检查 ${providerName} API Key 是否正确配置。错误详情: ${ex.message}"
                    )
                }
                HttpStatus.NOT_FOUND -> {
                    return buildDemoReply(
                        providerKey,
                        history,
                        "模型返回 404 未找到，请检查 ${providerName} API 地址是否正确。当前地址: ${provider.baseUrl}"
                    )
                }
                else -> {
                    throw IllegalStateException("${providerName} API 调用失败: ${ex.statusCode} ${ex.message}. 响应: ${ex.responseBodyAsString}")
                }
            }
        } catch (ex: Exception) {
            val providerName = when (providerKey) {
                "deepseek" -> "DeepSeek"
                "doubao" -> "豆包"
                else -> providerKey
            }
            println("异常: ${ex.javaClass.simpleName} - ${ex.message}")
            ex.printStackTrace()
            return buildDemoReply(
                providerKey,
                history,
                "${providerName} API 调用异常: ${ex.message}"
            )
        }

        val reply = extractReply(responseNode)
        val tokens = responseNode.path("usage").path("total_tokens").asInt(0).takeIf { it > 0 }
        val latency = System.currentTimeMillis() - start
        return ProviderResult(
            provider = providerKey,
            reply = reply,
            latency = latency,
            tokens = tokens
        )
    }

    private fun extractReply(node: JsonNode): String {
        val content = node.path("choices").firstOrNull()
            ?.path("message")
            ?.path("content")
            ?.asText(null)
        return content?.takeIf { it.isNotBlank() } ?: node.toPrettyString()
    }

    private fun buildDemoReply(providerKey: String, history: List<MessagePayload>, reason: String): ProviderResult {
        val userPrompt = history.lastOrNull { it.role.equals("user", ignoreCase = true) }?.content
            ?: "（没有捕获到用户输入）"
        val providerName = when (providerKey) {
            "deepseek" -> "DeepSeek"
            "doubao" -> "豆包"
            else -> providerKey
        }
        val envVarName = "${providerKey.uppercase()}_API_KEY"
        val reply = """
            【演示模式】$reason

            你刚才说：$userPrompt
            
            提示：这是模拟的 ${providerName} 回答。要使用真实的 ${providerName} API，请在系统环境变量中配置 $envVarName。
        """.trimIndent()
        return ProviderResult(
            provider = providerKey,
            reply = reply,
            latency = 5,
            tokens = null
        )
    }
}

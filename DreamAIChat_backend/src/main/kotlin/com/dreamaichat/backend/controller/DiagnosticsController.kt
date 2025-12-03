package com.dreamaichat.backend.controller

import com.dreamaichat.backend.config.ModelProviderProperties
import com.dreamaichat.backend.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI

@RestController
@RequestMapping("/api/diagnostics")
class DiagnosticsController(
    private val properties: ModelProviderProperties
) {

    data class ProviderStatus(
        val provider: String,
        val baseUrl: String,
        val apiKeyConfigured: Boolean,
        val dnsResolved: Boolean,
        val reachable: Boolean,
        val message: String
    )

    @GetMapping("/providers")
    fun providerStatus(): ResponseEntity<ApiResponse<List<ProviderStatus>>> {
        val statuses = properties.providers.map { (key, provider) ->
            diagnoseProvider(key, provider.baseUrl, provider.apiKey)
        }
        return ResponseEntity.ok(ApiResponse.ok(statuses))
    }

    private fun diagnoseProvider(providerKey: String, baseUrl: String, apiKey: String): ProviderStatus {
        if (baseUrl.isBlank()) {
            return ProviderStatus(
                provider = providerKey,
                baseUrl = baseUrl,
                apiKeyConfigured = isApiKeyConfigured(apiKey),
                dnsResolved = false,
                reachable = false,
                message = "baseUrl 未配置"
            )
        }

        val trimmedUrl = baseUrl.trim()
        return try {
            val uri = URI(trimmedUrl)
            val host = uri.host ?: return ProviderStatus(
                provider = providerKey,
                baseUrl = trimmedUrl,
                apiKeyConfigured = isApiKeyConfigured(apiKey),
                dnsResolved = false,
                reachable = false,
                message = "baseUrl 缺少 host 部分"
            )
            val port = when {
                uri.port > 0 -> uri.port
                uri.scheme.equals("https", ignoreCase = true) -> 443
                else -> 80
            }

            val dnsResolved = try {
                InetAddress.getByName(host)
                true
            } catch (_: Exception) {
                false
            }

            val reachable = if (dnsResolved) {
                try {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress(host, port), 5000)
                    }
                    true
                } catch (_: Exception) {
                    false
                }
            } else {
                false
            }

            val message = when {
                !isApiKeyConfigured(apiKey) -> "API Key 未配置或仍为 DEMO-KEY"
                !dnsResolved -> "无法解析域名：$host"
                !reachable -> "无法在 5 秒内连接到 $host:$port"
                else -> "基础连通性正常"
            }

            ProviderStatus(
                provider = providerKey,
                baseUrl = trimmedUrl,
                apiKeyConfigured = isApiKeyConfigured(apiKey),
                dnsResolved = dnsResolved,
                reachable = reachable,
                message = message
            )
        } catch (ex: Exception) {
            ProviderStatus(
                provider = providerKey,
                baseUrl = trimmedUrl,
                apiKeyConfigured = isApiKeyConfigured(apiKey),
                dnsResolved = false,
                reachable = false,
                message = "baseUrl 解析失败：${ex.message}"
            )
        }
    }

    private fun isApiKeyConfigured(apiKey: String): Boolean {
        val value = apiKey.trim()
        return value.isNotEmpty() && !value.equals("DEMO-KEY", ignoreCase = true)
    }
}


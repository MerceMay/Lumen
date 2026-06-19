package dev.mercemay.lumen.data.worker

import java.net.URI

class WorkerUrlNormalizer {
    fun normalize(input: String): String {
        val trimmed = input.trim()
        require(trimmed.isNotEmpty()) { "Worker URL 不能为空" }
        val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed else "https://$trimmed"
        val uri = URI(withScheme)
        require(!uri.host.isNullOrBlank()) { "Worker URL 无效" }
        val port = if (uri.port >= 0) ":${uri.port}" else ""
        return "${uri.scheme}://${uri.host}$port"
    }
}

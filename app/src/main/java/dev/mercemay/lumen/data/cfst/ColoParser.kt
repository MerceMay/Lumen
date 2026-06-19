package dev.mercemay.lumen.data.cfst

import okhttp3.Headers

class ColoParser {
    private val iata = Regex("[A-Z]{3}")
    private val country = Regex("[A-Z]{2}")
    private val gcore = Regex("[a-z]{2,5}")

    fun parse(headers: Headers): String? {
        val server = headers["server"].orEmpty()
        if (server.equals("cloudflare", ignoreCase = true)) {
            return headers["cf-ray"]?.substringAfterLast('-')?.takeIf { iata.matches(it) }
        }
        if (server.contains("CDN77", ignoreCase = true)) {
            return headers["x-77-pop"]?.let { country.find(it)?.value }
        }
        if (server.contains("BunnyCDN", ignoreCase = true)) {
            return country.find(server)?.value
        }
        headers["x-amz-cf-pop"]?.let { value ->
            iata.find(value)?.value?.let { return it }
        }
        headers["x-served-by"]?.let { value ->
            iata.findAll(value).lastOrNull()?.value?.let { return it }
        }
        headers["x-id-fe"]?.let { value ->
            gcore.find(value)?.value?.uppercase()?.let { return it }
        }
        return null
    }
}

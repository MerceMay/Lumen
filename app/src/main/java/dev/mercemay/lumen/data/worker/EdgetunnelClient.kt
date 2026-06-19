package dev.mercemay.lumen.data.worker

import dev.mercemay.lumen.data.network.OkHttpClientFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EdgetunnelClient @Inject constructor(
    okHttpClientFactory: OkHttpClientFactory,
) {
    private val cookieJar = MemoryCookieJar()
    private val client = okHttpClientFactory.default().newBuilder()
        .cookieJar(cookieJar)
        .followRedirects(false)
        .followSslRedirects(false)
        .build()
    private val normalizer = WorkerUrlNormalizer()

    private var cachedPassword: String? = null

    suspend fun login(workerUrl: String, adminPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            cachedPassword = adminPassword
            val baseUrl = normalizer.normalize(workerUrl)
            val body = FormBody.Builder().add("password", adminPassword).build()
            val request = Request.Builder()
                .url("$baseUrl/login")
                .header("User-Agent", USER_AGENT)
                .post(body)
                .build()
            client.newCall(request).execute().use { response ->
                val hasCookie = cookieJar.hasCookiesFor(baseUrl)
                if (!hasCookie && !response.isSuccessful && !response.isRedirect) {
                    error("登录失败: HTTP ${response.code}")
                }
            }
        }
    }

    suspend fun uploadAddTxt(workerUrl: String, adminPassword: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        cachedPassword = adminPassword
        runCatching {
            val baseUrl = normalizer.normalize(workerUrl)
            val body = content.toRequestBody("text/plain; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("$baseUrl/admin/ADD.txt")
                .header("User-Agent", USER_AGENT)
                .post(body)
                .build()
            authenticatedCall(baseUrl, adminPassword, request)
        }
    }

    suspend fun readAddTxt(workerUrl: String, adminPassword: String): Result<String> = withContext(Dispatchers.IO) {
        cachedPassword = adminPassword
        runCatching {
            val baseUrl = normalizer.normalize(workerUrl)
            val request = Request.Builder()
                .url("$baseUrl/admin/ADD.txt")
                .header("User-Agent", USER_AGENT)
                .get()
                .build()
            authenticatedCallForBody(baseUrl, adminPassword, request)
        }
    }

    private fun authenticatedCall(baseUrl: String, password: String, request: Request) {
        val response = client.newCall(request).execute()
        response.use {
            if (it.isSuccessful) return
            if (it.isRedirect || it.code == 401 || it.code == 403) {
                doLogin(baseUrl, password)
                client.newCall(request).execute().use { retry ->
                    if (!retry.isSuccessful) error("操作失败: HTTP ${retry.code}")
                }
                return
            }
            error("操作失败: HTTP ${it.code}")
        }
    }

    private fun authenticatedCallForBody(baseUrl: String, password: String, request: Request): String {
        val response = client.newCall(request).execute()
        response.use {
            if (it.isSuccessful) return it.body.string()
            if (it.isRedirect || it.code == 401 || it.code == 403) {
                doLogin(baseUrl, password)
                client.newCall(request).execute().use { retry ->
                    if (!retry.isSuccessful) error("操作失败: HTTP ${retry.code}")
                    return retry.body.string()
                }
            }
            error("操作失败: HTTP ${it.code}")
        }
    }

    private fun doLogin(baseUrl: String, password: String) {
        val body = FormBody.Builder().add("password", password).build()
        val request = Request.Builder()
            .url("$baseUrl/login")
            .header("User-Agent", USER_AGENT)
            .post(body)
            .build()
        client.newCall(request).execute().close()
    }

    private val Response.isRedirect: Boolean get() = code in 301..399

    private class MemoryCookieJar : CookieJar {
        private val cookies = java.util.concurrent.ConcurrentHashMap<String, List<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            this.cookies[url.host] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> = cookies[url.host].orEmpty()

        fun hasCookiesFor(baseUrl: String): Boolean {
            val host = runCatching { HttpUrl.Builder().scheme("https").host(baseUrl.removePrefix("https://").removePrefix("http://").split("/")[0]).build().host }.getOrNull()
            return host != null && !cookies[host].isNullOrEmpty()
        }
    }

    private companion object {
        const val USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.80 Safari/537.36"
    }
}

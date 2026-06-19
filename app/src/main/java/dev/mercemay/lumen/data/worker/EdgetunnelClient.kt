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

    suspend fun login(workerUrl: String, adminPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val baseUrl = normalizer.normalize(workerUrl)
            val body = FormBody.Builder().add("password", adminPassword).build()
            val request = Request.Builder()
                .url("$baseUrl/login")
                .header("User-Agent", USER_AGENT)
                .post(body)
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("登录失败: HTTP ${response.code}")
            }
        }
    }

    suspend fun uploadAddTxt(workerUrl: String, adminPassword: String, content: String): Result<Unit> {
        val first = uploadAddTxtOnce(workerUrl, content)
        if (first.isSuccess) return first
        login(workerUrl, adminPassword).getOrElse { return Result.failure(it) }
        return uploadAddTxtOnce(workerUrl, content)
    }

    suspend fun readAddTxt(workerUrl: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val baseUrl = normalizer.normalize(workerUrl)
            val request = Request.Builder()
                .url("$baseUrl/admin/ADD.txt")
                .header("User-Agent", USER_AGENT)
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("读取 ADD.txt 失败: HTTP ${response.code}")
                response.body.string()
            }
        }
    }

    private suspend fun uploadAddTxtOnce(workerUrl: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val baseUrl = normalizer.normalize(workerUrl)
            val body = content.toRequestBody("text/plain; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("$baseUrl/admin/ADD.txt")
                .header("User-Agent", USER_AGENT)
                .post(body)
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("上传 ADD.txt 失败: HTTP ${response.code}")
            }
        }
    }

    private class MemoryCookieJar : CookieJar {
        private val cookies = java.util.concurrent.ConcurrentHashMap<String, List<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            this.cookies[url.host] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> = cookies[url.host].orEmpty()
    }

    private companion object {
        const val USER_AGENT = "LumenCFST/0.1 Android"
    }
}

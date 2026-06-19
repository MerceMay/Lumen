package dev.mercemay.lumen.workerjob

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.mercemay.lumen.data.network.OkHttpClientFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File

@HiltWorker
class GeoIpUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    okHttpClientFactory: OkHttpClientFactory,
) : CoroutineWorker(appContext, params) {
    private val client = okHttpClientFactory.default()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://github.com/v2fly/geoip/releases/latest/download/geoip.dat")
            .header("User-Agent", "LumenCFST/0.1 Android")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext Result.retry()
            val bytes = response.body.bytes()
            val dir = File(applicationContext.filesDir, "geoip").also { it.mkdirs() }
            val tmp = File(dir, "geoip.dat.tmp")
            val target = File(dir, "geoip.dat")
            tmp.writeBytes(bytes)
            if (!tmp.renameTo(target)) return@withContext Result.retry()
            Result.success()
        }
    }
}

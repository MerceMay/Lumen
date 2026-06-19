package dev.mercemay.lumen.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.mercemay.lumen.domain.model.IpMode
import dev.mercemay.lumen.domain.model.SpeedTestConfig
import dev.mercemay.lumen.domain.model.TestStrategy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val workerUrl: Flow<String> = context.settingsDataStore.data.map { it[Keys.workerUrl].orEmpty() }
    val adminPassword: Flow<String> = context.settingsDataStore.data.map { it[Keys.adminPassword].orEmpty() }

    val speedTestConfig: Flow<SpeedTestConfig> = context.settingsDataStore.data.map { preferences ->
        SpeedTestConfig(
            pingConcurrency = preferences[Keys.pingConcurrency] ?: 200,
            pingTimes = preferences[Keys.pingTimes] ?: 4,
            downloadTestCount = preferences[Keys.downloadTestCount] ?: 50,
            downloadTimeoutSeconds = preferences[Keys.downloadTimeoutSeconds] ?: 10,
            port = preferences[Keys.port] ?: 443,
            testUrl = preferences[Keys.testUrl] ?: "https://cf.xiu2.xyz/url",
            testStrategy = preferences[Keys.testStrategy]?.let { runCatching { TestStrategy.valueOf(it) }.getOrNull() } ?: TestStrategy.TCPing,
            httpingStatusCode = preferences[Keys.httpingStatusCode]?.takeIf { it in 100..599 },
            cfColoFilter = preferences[Keys.cfColoFilter].orEmpty().split(',', ' ', '\n', '\t').map { it.trim().uppercase() }.filter { it.isNotEmpty() }.toSet(),
            maxDelayMillis = preferences[Keys.maxDelayMillis] ?: 9999L,
            minDelayMillis = preferences[Keys.minDelayMillis] ?: 0L,
            maxLossRate = preferences[Keys.maxLossRate] ?: 1.0,
            minDownloadSpeedMbps = preferences[Keys.minDownloadSpeedMbps] ?: 0.0,
            disableDownload = preferences[Keys.disableDownload] ?: false,
            ipMode = preferences[Keys.ipMode]?.let { runCatching { IpMode.valueOf(it) }.getOrNull() } ?: IpMode.IPv4,
            ipText = preferences[Keys.ipText].orEmpty(),
        )
    }

    suspend fun setWorkerConfig(url: String, password: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.workerUrl] = url
            preferences[Keys.adminPassword] = password
        }
    }

    suspend fun setSpeedTestConfig(config: SpeedTestConfig) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.pingConcurrency] = config.pingConcurrency.coerceIn(1, 1000)
            preferences[Keys.pingTimes] = config.pingTimes.coerceIn(1, 10)
            preferences[Keys.downloadTestCount] = config.downloadTestCount.coerceIn(1, 100)
            preferences[Keys.downloadTimeoutSeconds] = config.downloadTimeoutSeconds.coerceIn(1, 120)
            preferences[Keys.port] = config.port.coerceIn(1, 65535)
            preferences[Keys.testUrl] = config.testUrl
            preferences[Keys.testStrategy] = config.testStrategy.name
            config.httpingStatusCode?.let { preferences[Keys.httpingStatusCode] = it } ?: preferences.remove(Keys.httpingStatusCode)
            preferences[Keys.cfColoFilter] = config.cfColoFilter.joinToString(",")
            preferences[Keys.maxDelayMillis] = config.maxDelayMillis.coerceAtLeast(0)
            preferences[Keys.minDelayMillis] = config.minDelayMillis.coerceAtLeast(0)
            preferences[Keys.maxLossRate] = config.maxLossRate.coerceIn(0.0, 1.0)
            preferences[Keys.minDownloadSpeedMbps] = config.minDownloadSpeedMbps.coerceAtLeast(0.0)
            preferences[Keys.disableDownload] = config.disableDownload
            preferences[Keys.ipMode] = config.ipMode.name
            preferences[Keys.ipText] = config.ipText
        }
    }

    private object Keys {
        val workerUrl: Preferences.Key<String> = stringPreferencesKey("worker_url")
        val adminPassword: Preferences.Key<String> = stringPreferencesKey("admin_password")
        val pingConcurrency: Preferences.Key<Int> = intPreferencesKey("ping_concurrency")
        val pingTimes: Preferences.Key<Int> = intPreferencesKey("ping_times")
        val downloadTestCount: Preferences.Key<Int> = intPreferencesKey("download_test_count")
        val downloadTimeoutSeconds: Preferences.Key<Int> = intPreferencesKey("download_timeout_seconds")
        val port: Preferences.Key<Int> = intPreferencesKey("port")
        val testUrl: Preferences.Key<String> = stringPreferencesKey("test_url")
        val testStrategy: Preferences.Key<String> = stringPreferencesKey("test_strategy")
        val httpingStatusCode: Preferences.Key<Int> = intPreferencesKey("httping_status_code")
        val cfColoFilter: Preferences.Key<String> = stringPreferencesKey("cf_colo_filter")
        val maxDelayMillis: Preferences.Key<Long> = longPreferencesKey("max_delay_millis")
        val minDelayMillis: Preferences.Key<Long> = longPreferencesKey("min_delay_millis")
        val maxLossRate: Preferences.Key<Double> = doublePreferencesKey("max_loss_rate")
        val minDownloadSpeedMbps: Preferences.Key<Double> = doublePreferencesKey("min_download_speed_mbps")
        val disableDownload: Preferences.Key<Boolean> = booleanPreferencesKey("disable_download")
        val ipMode: Preferences.Key<String> = stringPreferencesKey("ip_mode")
        val ipText: Preferences.Key<String> = stringPreferencesKey("ip_text")
    }
}

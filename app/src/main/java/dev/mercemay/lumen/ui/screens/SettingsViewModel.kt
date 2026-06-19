package dev.mercemay.lumen.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mercemay.lumen.data.db.SpeedTestDao
import dev.mercemay.lumen.data.settings.SettingsRepository
import dev.mercemay.lumen.data.worker.EdgetunnelClient
import dev.mercemay.lumen.data.worker.GeoIpClient
import dev.mercemay.lumen.domain.model.SpeedTestConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val edgetunnelClient: EdgetunnelClient,
    private val dao: SpeedTestDao,
    private val geoIpClient: GeoIpClient,
) : ViewModel() {
    private val operationState = MutableStateFlow(WorkerOperationState())

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.workerUrl,
        settingsRepository.adminPassword,
        dao.observeLatestResults(),
        operationState,
        settingsRepository.speedTestConfig,
    ) { values ->
        val url = values[0] as String
        val password = values[1] as String
        @Suppress("UNCHECKED_CAST")
        val results = values[2] as List<dev.mercemay.lumen.data.db.SpeedTestResultEntity>
        val operation = values[3] as WorkerOperationState
        val config = values[4] as SpeedTestConfig
        SettingsUiState(
            workerUrl = url,
            adminPassword = password,
            latestResultCount = results.size,
            operation = operation,
            speedTestConfig = config,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun saveWorkerConfig(url: String, password: String) {
        viewModelScope.launch { settingsRepository.setWorkerConfig(url, password) }
    }

    fun saveSpeedTestConfig(config: SpeedTestConfig) {
        viewModelScope.launch {
            settingsRepository.setSpeedTestConfig(config)
            operationState.update { it.copy(message = "测速参数已保存") }
        }
    }

    fun testLogin(url: String, password: String) {
        viewModelScope.launch {
            operationState.update { it.copy(running = true, message = "正在登录...") }
            val result = edgetunnelClient.login(url, password)
            operationState.update { it.copy(running = false, message = result.fold({ "登录成功" }, { "登录失败：${it.message}" })) }
        }
    }

    fun uploadAddTxt(url: String, password: String) {
        viewModelScope.launch {
            operationState.update { it.copy(running = true, message = "正在上传...") }
            val latest = dao.getLatestResultsOnce()
            val geoContent = buildAddTxt(latest)
            val result = edgetunnelClient.uploadAddTxt(url, password, geoContent)
            operationState.update { it.copy(running = false, message = result.fold({ "上传成功" }, { "上传失败：${it.message}" })) }
        }
    }

    fun readAddTxt(url: String) {
        viewModelScope.launch {
            operationState.update { it.copy(running = true, message = "正在读取...") }
            val result = edgetunnelClient.readAddTxt(url)
            operationState.update { state ->
                state.copy(
                    running = false,
                    message = result.fold({ "读取成功，长度 ${it.length}" }, { "读取失败：${it.message}" }),
                )
            }
        }
    }


    private suspend fun buildAddTxt(results: List<dev.mercemay.lumen.data.db.SpeedTestResultEntity>): String {
        val grouped = LinkedHashMap<String, MutableList<dev.mercemay.lumen.data.db.SpeedTestResultEntity>>()
        results.forEach { result ->
            val country = geoIpClient.countryCode(result.ip) ?: "Unknown"
            grouped.getOrPut(country) { mutableListOf() } += result
        }
        return grouped.flatMap { (country, items) ->
            items.mapIndexed { index, result ->
                val host = if (":" in result.ip && !result.ip.startsWith("[")) "[${result.ip}]" else result.ip
                "$host:${result.port}#$country${index + 1}"
            }
        }.joinToString("\n")
    }
}

data class SettingsUiState(
    val workerUrl: String = "",
    val adminPassword: String = "",
    val latestResultCount: Int = 0,
    val operation: WorkerOperationState = WorkerOperationState(),
    val speedTestConfig: SpeedTestConfig = SpeedTestConfig(),
)

data class WorkerOperationState(
    val running: Boolean = false,
    val message: String? = null,
)

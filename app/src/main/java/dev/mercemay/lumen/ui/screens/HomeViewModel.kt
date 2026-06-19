package dev.mercemay.lumen.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.mercemay.lumen.data.cfst.SpeedTestEvent
import dev.mercemay.lumen.data.cfst.SpeedTestRepository
import dev.mercemay.lumen.data.db.SpeedTestDao
import dev.mercemay.lumen.data.settings.SettingsRepository
import dev.mercemay.lumen.data.worker.EdgetunnelClient
import dev.mercemay.lumen.data.worker.GeoIpClient
import dev.mercemay.lumen.domain.model.SpeedTestResult
import dev.mercemay.lumen.domain.model.TestProgress
import dev.mercemay.lumen.service.SpeedTestService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val speedTestRepository: SpeedTestRepository,
    private val settingsRepository: SettingsRepository,
    private val edgetunnelClient: EdgetunnelClient,
    private val geoIpClient: GeoIpClient,
    private val dao: SpeedTestDao,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var speedTestJob: Job? = null

    fun startSpeedTest() {
        if (speedTestJob?.isActive == true) return
        SpeedTestService.start(appContext)
        speedTestJob = viewModelScope.launch {
            _uiState.update { it.copy(running = true, error = null, progress = TestProgress.Idle, results = emptyList()) }
            val config = settingsRepository.speedTestConfig.first()
            speedTestRepository.runSpeedTest(config).collect { event ->
                when (event) {
                    is SpeedTestEvent.Progress -> {
                        _uiState.update { it.copy(progress = event.progress) }
                        SpeedTestService.updateProgress(appContext, progressText(event.progress))
                    }
                    is SpeedTestEvent.Completed -> {
                        _uiState.update {
                            it.copy(running = false, progress = TestProgress.Completed(event.results.size), results = event.results)
                        }
                        if (config.autoUpload) {
                            autoUpload()
                        }
                    }
                }
            }
        }.also { job ->
            job.invokeOnCompletion { cause ->
                SpeedTestService.stop(appContext)
                if (cause != null && cause !is kotlinx.coroutines.CancellationException) {
                    _uiState.update { s -> s.copy(running = false, error = cause.message ?: "测速失败") }
                }
            }
        }
    }

    fun cancelSpeedTest() {
        speedTestJob?.cancel()
        _uiState.update { it.copy(running = false, progress = TestProgress.Idle) }
    }

    private fun autoUpload() {
        viewModelScope.launch {
            val workerUrl = settingsRepository.workerUrl.first()
            val password = settingsRepository.adminPassword.first()
            if (workerUrl.isBlank() || password.isBlank()) return@launch
            val results = withContext(Dispatchers.IO) { dao.getLatestResultsOnce() }
            if (results.isEmpty()) return@launch
            val content = withContext(Dispatchers.IO) { buildAddTxt(results) }
            val result = edgetunnelClient.uploadAddTxt(workerUrl, password, content)
            val msg = result.fold({ "自动上传成功（${results.size} 条）" }, { "自动上传失败：${it.message}" })
            _uiState.update { it.copy(uploadMessage = msg) }
        }
    }

    fun clearUploadMessage() {
        _uiState.update { it.copy(uploadMessage = null) }
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

    private fun progressText(progress: TestProgress): String = when (progress) {
        TestProgress.Idle -> "空闲"
        is TestProgress.LoadingIps -> "已加载 ${progress.total} 个 IP 段"
        is TestProgress.Pinging -> "延迟测速：${progress.completed}/${progress.total}"
        is TestProgress.Downloading -> "下载测速：${progress.completed}/${progress.total}"
        is TestProgress.Completed -> "完成：${progress.results} 条结果"
        is TestProgress.Failed -> progress.message
    }
}

data class HomeUiState(
    val running: Boolean = false,
    val progress: TestProgress = TestProgress.Idle,
    val results: List<SpeedTestResult> = emptyList(),
    val error: String? = null,
    val uploadMessage: String? = null,
)

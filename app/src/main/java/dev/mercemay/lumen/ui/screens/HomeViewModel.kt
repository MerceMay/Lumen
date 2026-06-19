package dev.mercemay.lumen.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.mercemay.lumen.data.cfst.SpeedTestEvent
import dev.mercemay.lumen.data.cfst.SpeedTestRepository
import dev.mercemay.lumen.data.settings.SettingsRepository
import dev.mercemay.lumen.domain.model.SpeedTestResult
import dev.mercemay.lumen.domain.model.TestProgress
import dev.mercemay.lumen.service.SpeedTestService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val speedTestRepository: SpeedTestRepository,
    private val settingsRepository: SettingsRepository,
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
                    is SpeedTestEvent.Completed -> _uiState.update {
                        it.copy(running = false, progress = TestProgress.Completed(event.results.size), results = event.results)
                    }
                }
            }
        }.also { job ->
            job.invokeOnCompletion {
                SpeedTestService.stop(appContext)
                if (it != null) {
                    _uiState.update { s -> s.copy(running = false, error = it.message ?: "测速失败") }
                }
            }
        }
    }

    fun cancelSpeedTest() {
        speedTestJob?.cancel()
        _uiState.update { it.copy(running = false, progress = TestProgress.Idle) }
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
)

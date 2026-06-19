package dev.mercemay.lumen.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mercemay.lumen.data.db.SpeedTestDao
import dev.mercemay.lumen.data.db.SpeedTestResultEntity
import dev.mercemay.lumen.data.db.SpeedTestRunEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val dao: SpeedTestDao,
) : ViewModel() {
    val runs: StateFlow<List<SpeedTestRunEntity>> = dao.observeRuns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun getResults(runId: Long): Flow<List<SpeedTestResultEntity>> = dao.observeResults(runId)

    fun deleteRun(runId: Long) {
        viewModelScope.launch { dao.deleteRun(runId) }
    }

    fun clearHistory() {
        viewModelScope.launch { dao.clearHistory() }
    }
}

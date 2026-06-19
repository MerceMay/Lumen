package dev.mercemay.lumen.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val runs by viewModel.runs.collectAsState()
    var selectedRunId by remember { mutableStateOf<Long?>(null) }

    LumenPage(title = "历史") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = viewModel::clearHistory, enabled = runs.isNotEmpty()) { Text("清空") }
        }
        if (runs.isEmpty()) {
            Text("暂无历史记录")
        } else {
            runs.forEach { run ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedRunId = run.id },
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(DateFormat.getDateTimeInstance().format(Date(run.startedAtMillis)))
                        Text("状态：${statusLabel(run.status)}  网络：${run.networkType.orEmpty()}")
                        run.errorMessage?.let { Text("错误：$it", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }

    selectedRunId?.let { runId ->
        val results by viewModel.getResults(runId).collectAsState(initial = emptyList())
        AlertDialog(
            onDismissRequest = { selectedRunId = null },
            title = { Text("测速结果") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (results.isEmpty()) {
                        Text("无测速结果")
                    } else {
                        results.forEach { result ->
                            Text(
                                "${result.ip}:${result.port}  " +
                                    String.format(Locale.US, "%.0fms  %.2fMB/s", result.averageDelayMillis, result.downloadSpeedMbps) +
                                    (result.colo?.let { "  $it" } ?: ""),
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { selectedRunId = null }) { Text("关闭") } },
            dismissButton = {
                OutlinedButton(onClick = {
                    viewModel.deleteRun(runId)
                    selectedRunId = null
                }) { Text("删除") }
            },
        )
    }
}

private fun statusLabel(status: String): String = when (status) {
    "running" -> "进行中"
    "completed" -> "已完成"
    "cancelled" -> "已取消"
    "failed" -> "失败"
    else -> status
}

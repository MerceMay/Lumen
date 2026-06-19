package dev.mercemay.lumen.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.mercemay.lumen.domain.model.SpeedTestResult
import dev.mercemay.lumen.domain.model.TestProgress
import java.util.Locale

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { viewModel.startSpeedTest() }

    val onStart: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.startSpeedTest()
        }
    }

    LumenPage(title = "测速") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onStart, enabled = !state.running, modifier = Modifier.weight(1f)) {
                Text(if (state.running) "测速" else "开始")
            }
            OutlinedButton(onClick = viewModel::cancelSpeedTest, enabled = state.running, modifier = Modifier.weight(1f)) { Text("停止") }
        }
        StatusCard(state)
        ResultsCard(state.results)
    }
}

@Composable
private fun StatusCard(state: HomeUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.running) CircularProgressIndicator()
                Text("当前状态", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(8.dp))
            Text(progressText(state.progress))
            state.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ResultsCard(results: List<SpeedTestResult>) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Top 结果", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(
                    onClick = {
                        val text = results.take(10).joinToString("\n") { result ->
                            "${result.candidate.hostAddress}:${result.port} ${String.format(Locale.US, "%.0fms %.2fMB/s", result.averageDelayMillis, result.downloadSpeedMbps)}"
                        }
                        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }, "分享优选结果"))
                    },
                    enabled = results.isNotEmpty(),
                ) { Text("分享") }
            }
            Spacer(Modifier.height(8.dp))
            if (results.isEmpty()) {
                Text("暂无结果")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    results.take(10).forEach { result ->
                        Text(
                            "${result.candidate.hostAddress}:${result.port}  " +
                                String.format(Locale.US, "%.0fms  %.2fMB/s", result.averageDelayMillis, result.downloadSpeedMbps) +
                                (result.colo?.let { "  $it" } ?: ""),
                        )
                    }
                }
            }
        }
    }
}

private fun progressText(progress: TestProgress): String = when (progress) {
    TestProgress.Idle -> "空闲"
    is TestProgress.LoadingIps -> "已加载 ${progress.total} 个 IP 段"
    is TestProgress.Pinging -> "延迟测速：${progress.completed}/${progress.total}，可用 ${progress.available}"
    is TestProgress.Downloading -> "下载测速：${progress.completed}/${progress.total} ${progress.currentIp.orEmpty()}"
    is TestProgress.Completed -> "完成：${progress.results} 条结果"
    is TestProgress.Failed -> progress.message
}

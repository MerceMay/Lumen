package dev.mercemay.lumen.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val runs by viewModel.runs.collectAsState()
    LumenPage(title = "历史") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = viewModel::clearHistory, enabled = runs.isNotEmpty()) { Text("清空") }
        }
        if (runs.isEmpty()) {
            Text("暂无历史记录")
        } else {
            runs.forEach { run ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(DateFormat.getDateTimeInstance().format(Date(run.startedAtMillis)))
                        Text("状态：${run.status}  网络：${run.networkType.orEmpty()}")
                        run.errorMessage?.let { Text("错误：$it", color = MaterialTheme.colorScheme.error) }
                        OutlinedButton(onClick = { viewModel.deleteRun(run.id) }) { Text("删除") }
                    }
                }
            }
        }
    }
}

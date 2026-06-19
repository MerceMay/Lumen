package dev.mercemay.lumen.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun WorkerSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var editing by remember { mutableStateOf(false) }
    var resultDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(state.operation.message, state.operation.running) {
        val msg = state.operation.message
        if (msg != null && !state.operation.running) {
            resultDialog = state.operation.title to msg
        }
    }

    LumenPage(title = "推送", onBack = onBack) {
        WorkerConfigItem(
            title = "Worker 配置",
            subtitle = if (state.workerUrl.isBlank()) "未配置域名" else state.workerUrl,
            onClick = { editing = true },
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { viewModel.testLogin(state.workerUrl, state.adminPassword) },
                modifier = Modifier.weight(1f),
                enabled = !state.operation.running,
            ) {
                if (state.operation.running && state.operation.title == "登录测试") {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("登录测试")
                }
            }
            OutlinedButton(
                onClick = { viewModel.readAddTxt(state.workerUrl) },
                modifier = Modifier.weight(1f),
                enabled = !state.operation.running,
            ) {
                if (state.operation.running && state.operation.title == "读取内容") {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("读取内容")
                }
            }
        }
        Button(
            onClick = { viewModel.uploadAddTxt(state.workerUrl, state.adminPassword) },
            enabled = state.latestResultCount > 0 && !state.operation.running,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.operation.running && state.operation.title == "上传结果") {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("上传测速结果")
            }
        }
    }

    if (editing) {
        WorkerConfigDialog(
            workerUrl = state.workerUrl,
            adminPassword = state.adminPassword,
            onDismiss = { editing = false },
            onSave = { url, password ->
                viewModel.saveWorkerConfig(url, password)
                editing = false
            },
        )
    }

    resultDialog?.let { (title, content) ->
        AlertDialog(
            onDismissRequest = {
                resultDialog = null
                viewModel.clearOperationMessage()
            },
            title = { Text(title) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(content, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = {
                    resultDialog = null
                    viewModel.clearOperationMessage()
                }) { Text("确定") }
            },
        )
    }
}

@Composable
private fun WorkerConfigItem(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(Icons.Default.Link, contentDescription = null)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun WorkerConfigDialog(workerUrl: String, adminPassword: String, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var url by remember(workerUrl) { mutableStateOf(workerUrl) }
    var password by remember(adminPassword) { mutableStateOf(adminPassword) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Worker 配置") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(url, { url = it }, label = { Text("Worker 域名") }, placeholder = { Text("example.com") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri))
                OutlinedTextField(password, { password = it }, label = { Text("管理员密码") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
            }
        },
        confirmButton = { Button(onClick = { onSave(url, password) }) { Text("保存") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("取消") } },
    )
}

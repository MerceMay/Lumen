package dev.mercemay.lumen.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.mercemay.lumen.domain.model.IpMode
import dev.mercemay.lumen.domain.model.SpeedTestConfig
import dev.mercemay.lumen.domain.model.TestStrategy

@Composable
fun AdvancedSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val config = state.speedTestConfig
    var editing by remember { mutableStateOf<EditField?>(null) }
    var optionDialog by remember { mutableStateOf<OptionDialog?>(null) }

    LumenPage(title = "高级设置", onBack = onBack) {
        SettingsSection("测速")
        ValueItem(Icons.Default.Speed, "延迟测速线程", config.pingConcurrency.toString()) { editing = EditField.PingConcurrency }
        ValueItem(Icons.Default.Repeat, "延迟测速次数", config.pingTimes.toString()) { editing = EditField.PingTimes }
        ValueItem(Icons.Default.Numbers, "测速端口", config.port.toString()) { editing = EditField.Port }
        ValueItem(Icons.Default.Link, "测速 URL", config.testUrl) { editing = EditField.TestUrl }
        ValueItem(Icons.Default.Public, "IP 模式", ipModeLabel(config.ipMode)) { optionDialog = OptionDialog.IpMode }
        HorizontalDivider()
        SettingsSection("过滤")
        ValueItem(Icons.AutoMirrored.Filled.List, "优选数量", config.downloadTestCount.toString()) { editing = EditField.BestCount }
        ValueItem(Icons.Default.FilterAlt, "平均延迟上限", "${config.maxDelayMillis} ms") { editing = EditField.MaxDelay }
        ValueItem(Icons.Default.Block, "丢包率上限", config.maxLossRate.toString()) { editing = EditField.MaxLossRate }
        HorizontalDivider()
        SettingsSection("下载测速")
        SwitchItem("禁用下载测速", "只按延迟和丢包排序", config.disableDownload) { viewModel.saveSpeedTestConfig(config.copy(disableDownload = it)) }
        ValueItem(Icons.Default.Download, "下载速度下限", "${config.minDownloadSpeedMbps} MB/s") { editing = EditField.MinSpeed }
        ValueItem(Icons.Default.Timer, "下载测速时间", "${config.downloadTimeoutSeconds} 秒") { editing = EditField.DownloadSeconds }
        HorizontalDivider()
        SettingsSection("测试策略")
        ValueItem(Icons.Default.Dns, "测试策略", testStrategyLabel(config.testStrategy)) { optionDialog = OptionDialog.TestStrategy }
        ValueItem(Icons.Default.Dns, "地区过滤", if (config.cfColoFilter.isEmpty()) "不过滤" else config.cfColoFilter.joinToString(", ")) { optionDialog = OptionDialog.Region }
        OutlinedButton(onClick = { viewModel.saveSpeedTestConfig(SpeedTestConfig()) }, modifier = Modifier.fillMaxWidth()) { Text("重置成默认") }
    }

    optionDialog?.let { dialog ->
        when (dialog) {
            OptionDialog.Region -> RegionDialog(
                selected = config.cfColoFilter,
                onDismiss = { optionDialog = null },
                onChange = { viewModel.saveSpeedTestConfig(config.copy(cfColoFilter = it)) },
            )
            OptionDialog.TestStrategy -> SingleOptionDialog(
                title = "测试策略",
                options = listOf("TCPing" to TestStrategy.TCPing.name, "HTTPing" to TestStrategy.HTTPing.name),
                selected = config.testStrategy.name,
                onDismiss = { optionDialog = null },
                onSelected = { viewModel.saveSpeedTestConfig(config.copy(testStrategy = TestStrategy.valueOf(it))); optionDialog = null },
            )
            OptionDialog.IpMode -> SingleOptionDialog(
                title = "IP 模式",
                options = listOf("IPv4" to IpMode.IPv4.name, "IPv6" to IpMode.IPv6.name, "双栈" to IpMode.DualStack.name),
                selected = config.ipMode.name,
                onDismiss = { optionDialog = null },
                onSelected = { viewModel.saveSpeedTestConfig(config.copy(ipMode = IpMode.valueOf(it))); optionDialog = null },
            )
        }
    }

    editing?.let { field ->
        EditValueDialog(
            title = field.title,
            initial = field.value(config),
            keyboardType = field.keyboardType,
            onDismiss = { editing = null },
            onConfirm = { value ->
                viewModel.saveSpeedTestConfig(field.apply(config, value))
                editing = null
            },
        )
    }
}

private enum class EditField(val title: String, val keyboardType: KeyboardType) {
    PingConcurrency("延迟测速线程", KeyboardType.Number),
    PingTimes("延迟测速次数", KeyboardType.Number),
    BestCount("优选数量", KeyboardType.Number),
    DownloadSeconds("下载测速时间", KeyboardType.Number),
    Port("测速端口", KeyboardType.Number),
    TestUrl("测速 URL", KeyboardType.Uri),
    MaxDelay("平均延迟上限", KeyboardType.Number),
    MaxLossRate("丢包率上限", KeyboardType.Decimal),
    MinSpeed("下载速度下限", KeyboardType.Decimal);

    fun value(config: SpeedTestConfig): String = when (this) {
        PingConcurrency -> config.pingConcurrency.toString()
        PingTimes -> config.pingTimes.toString()
        BestCount -> config.downloadTestCount.toString()
        DownloadSeconds -> config.downloadTimeoutSeconds.toString()
        Port -> config.port.toString()
        TestUrl -> config.testUrl
        MaxDelay -> config.maxDelayMillis.toString()
        MaxLossRate -> config.maxLossRate.toString()
        MinSpeed -> config.minDownloadSpeedMbps.toString()
    }

    fun apply(config: SpeedTestConfig, value: String): SpeedTestConfig = when (this) {
        PingConcurrency -> config.copy(pingConcurrency = value.toIntOrNull() ?: config.pingConcurrency)
        PingTimes -> config.copy(pingTimes = value.toIntOrNull() ?: config.pingTimes)
        BestCount -> config.copy(downloadTestCount = value.toIntOrNull() ?: config.downloadTestCount)
        DownloadSeconds -> config.copy(downloadTimeoutSeconds = value.toIntOrNull() ?: config.downloadTimeoutSeconds)
        Port -> config.copy(port = value.toIntOrNull() ?: config.port)
        TestUrl -> config.copy(testUrl = value)
        MaxDelay -> config.copy(maxDelayMillis = value.toLongOrNull() ?: config.maxDelayMillis)
        MaxLossRate -> config.copy(maxLossRate = value.toDoubleOrNull() ?: config.maxLossRate)
        MinSpeed -> config.copy(minDownloadSpeedMbps = value.toDoubleOrNull() ?: config.minDownloadSpeedMbps)
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun ValueItem(icon: ImageVector, title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(icon, contentDescription = null)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SwitchItem(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}


private enum class OptionDialog { Region, IpMode, TestStrategy }

private fun testStrategyLabel(strategy: TestStrategy): String = when (strategy) {
    TestStrategy.TCPing -> "TCPing"
    TestStrategy.HTTPing -> "HTTPing"
}

private fun ipModeLabel(mode: IpMode): String = when (mode) {
    IpMode.IPv4 -> "IPv4"
    IpMode.IPv6 -> "IPv6"
    IpMode.DualStack -> "双栈"
}


@Composable
private fun RegionDialog(selected: Set<String>, onDismiss: () -> Unit, onChange: (Set<String>) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("地区过滤") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("未选择时不过滤；选择后只保留选中地区。")
                listOf("HKG", "NRT", "SIN", "LAX", "SJC", "SEA", "FRA", "AMS").chunked(2).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { option ->
                            val checked = option in selected
                            val modifier = Modifier.weight(1f)
                            val click = { onChange(if (checked) selected - option else selected + option) }
                            if (checked) Button(onClick = click, modifier = modifier) { Text(option) }
                            else OutlinedButton(onClick = click, modifier = modifier) { Text(option) }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("完成") } },
    )
}

@Composable
private fun SingleOptionDialog(title: String, options: List<Pair<String, String>>, selected: String, onDismiss: () -> Unit, onSelected: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { (label, value) ->
                    if (value == selected) Button(onClick = { onSelected(value) }, modifier = Modifier.fillMaxWidth()) { Text(label) }
                    else OutlinedButton(onClick = { onSelected(value) }, modifier = Modifier.fillMaxWidth()) { Text(label) }
                }
            }
        },
        confirmButton = { OutlinedButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun EditValueDialog(title: String, initial: String, keyboardType: KeyboardType, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var value by remember(initial) { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value = value, onValueChange = { value = it }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = keyboardType)) },
        confirmButton = { Button(onClick = { onConfirm(value) }) { Text("确定") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("取消") } },
    )
}

package dev.mercemay.lumen.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable

@Composable
fun SettingsScreen(
    onOpenWorker: () -> Unit = {},
    onOpenAdvancedSettings: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
) {
    LumenPage(title = "设置") {
        LumenListItem(Icons.Default.CloudUpload, "推送", "配置 Worker 并上传结果", onClick = onOpenWorker)
        LumenDivider()
        LumenListItem(Icons.Default.Settings, "高级设置", "测速线程、端口、延迟、速度等参数", onClick = onOpenAdvancedSettings)
        LumenDivider()
        LumenListItem(Icons.Default.Info, "关于", "项目地址与鸣谢", onClick = onOpenAbout)
    }
}

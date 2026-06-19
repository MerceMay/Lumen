package dev.mercemay.lumen.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.mercemay.lumen.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val projectUrl = "https://github.com/MerceMay/Lumen"
    var updateMessage by remember { mutableStateOf<String?>(null) }

    LumenPage(title = "关于", onBack = onBack) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Image(painterResource(R.drawable.ic_launcher_foreground), contentDescription = null, modifier = Modifier.size(72.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Lumen", style = MaterialTheme.typography.headlineSmall)
                Text("Cloudflare 优选测速工具", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Text("项目地址", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Image(painterResource(R.drawable.ic_github), contentDescription = null, modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("GitHub", style = MaterialTheme.typography.bodyLarge)
                Text("MerceMay/Lumen", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { openUrl(context, projectUrl) }) { Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "打开") }
        }
        OutlinedButton(
            onClick = { scope.launch { updateMessage = withContext(Dispatchers.IO) { checkUpdate() } } },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("检查更新") }

        Text("鸣谢", style = MaterialTheme.typography.titleMedium)
        CreditItem("XIU2/CloudflareSpeedTest", "https://github.com/XIU2/CloudflareSpeedTest")
        CreditItem("cmliu/edgetunnel", "https://github.com/cmliu/edgetunnel")
        CreditItem("v2fly/geoip", "https://github.com/v2fly/geoip")
        CreditItem("Cloudflare", "https://www.cloudflare.com")
    }

    updateMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { updateMessage = null },
            title = { Text("检查更新") },
            text = { Text(message) },
            confirmButton = { Button(onClick = { updateMessage = null }) { Text("确定") } },
        )
    }
}

@Composable
private fun CreditItem(title: String, url: String) {
    val context = LocalContext.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
        }
        IconButton(onClick = { openUrl(context, url) }) { Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "打开") }
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

private fun checkUpdate(): String {
    return runCatching {
        val text = URL("https://api.github.com/repos/MerceMay/Lumen/releases/latest").readText()
        val tag = JSONObject(text).optString("tag_name")
        if (tag.isBlank()) "暂无发布版本" else "最新版本：$tag"
    }.getOrElse { "检查失败：${it.message}" }
}
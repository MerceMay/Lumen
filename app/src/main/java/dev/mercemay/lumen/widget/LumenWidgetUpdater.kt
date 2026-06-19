package dev.mercemay.lumen.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import dev.mercemay.lumen.MainActivity
import dev.mercemay.lumen.R
import dev.mercemay.lumen.domain.model.SpeedTestResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LumenWidgetUpdater {
    private const val PREFS = "widget_state"
    private const val KEY_TEXT = "text"

    fun saveLatestAndUpdate(context: Context, result: SpeedTestResult?) {
        val text = if (result == null) {
            "暂无测速结果"
        } else {
            val time = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date())
            "上次：$time\nTop1：${result.candidate.hostAddress}:${result.port}\n延迟：${result.averageDelayMillis.toInt()}ms  速度：${"%.2f".format(result.downloadSpeedMbps)}MB/s"
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_TEXT, text).apply()
        updateAll(context)
    }

    fun updateAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, LumenWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        ids.forEach { id -> manager.updateAppWidget(id, createViews(context)) }
    }

    fun createViews(context: Context): RemoteViews {
        val text = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TEXT, "暂无测速结果") ?: "暂无测速结果"
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return RemoteViews(context.packageName, R.layout.widget_lumen).apply {
            setTextViewText(R.id.widget_text, text)
            setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        }
    }
}

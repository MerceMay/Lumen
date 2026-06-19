package dev.mercemay.lumen.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dev.mercemay.lumen.MainActivity
import dev.mercemay.lumen.R

class SpeedTestService : Service() {

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification("测速中..."))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val progress = intent?.getStringExtra(EXTRA_PROGRESS)
        if (progress != null) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.notify(NOTIFICATION_ID, buildNotification(progress))
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "测速服务", NotificationManager.IMPORTANCE_LOW)
        channel.description = "测速运行中通知"
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(text: String): Notification {
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Lumen")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "speed_test_channel"
        private const val NOTIFICATION_ID = 1
        const val EXTRA_PROGRESS = "progress"

        fun start(context: Context) {
            context.startForegroundService(Intent(context, SpeedTestService::class.java))
        }

        fun updateProgress(context: Context, progress: String) {
            val intent = Intent(context, SpeedTestService::class.java)
            intent.putExtra(EXTRA_PROGRESS, progress)
            context.startService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, SpeedTestService::class.java))
        }
    }
}

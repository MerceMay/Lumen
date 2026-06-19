package dev.mercemay.lumen.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Constraints
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dev.mercemay.lumen.workerjob.GeoIpUpdateWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class LumenApp : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleGeoIpUpdates()
    }

    private fun scheduleGeoIpUpdates() {
        val request = PeriodicWorkRequestBuilder<GeoIpUpdateWorker>(7, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "geoip_update",
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}

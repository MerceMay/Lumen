package dev.mercemay.lumen.data.cfst

import android.content.Context
import dev.mercemay.lumen.data.db.SpeedTestDao
import dev.mercemay.lumen.data.db.SpeedTestResultEntity
import dev.mercemay.lumen.data.db.SpeedTestRunEntity
import dev.mercemay.lumen.domain.model.IpMode
import dev.mercemay.lumen.domain.model.SpeedTestConfig
import dev.mercemay.lumen.domain.model.SpeedTestResult
import dev.mercemay.lumen.widget.LumenWidgetUpdater
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeedTestRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val engine: SpeedTestEngine,
    private val dao: SpeedTestDao,
) {
    fun runSpeedTest(config: SpeedTestConfig): Flow<SpeedTestEvent> = flow {
        val ipText = withContext(Dispatchers.IO) { loadIpText(config.ipMode, config.ipText) }
        val runId = withContext(Dispatchers.IO) {
            dao.insertRun(
                SpeedTestRunEntity(
                    startedAtMillis = System.currentTimeMillis(),
                    status = "running",
                    networkType = "system",
                )
            )
        }

        try {
            engine.run(ipText, config, null).collect { event ->
                if (event is SpeedTestEvent.Completed) {
                    withContext(Dispatchers.IO) {
                        dao.insertResults(event.results.toEntities(runId))
                        dao.finishRun(runId, System.currentTimeMillis(), "completed")
                        LumenWidgetUpdater.saveLatestAndUpdate(context, event.results.firstOrNull())
                    }
                }
                emit(event)
            }
        } catch (e: CancellationException) {
            withContext(NonCancellable + Dispatchers.IO) {
                dao.finishRun(runId, System.currentTimeMillis(), "cancelled")
            }
            throw e
        } catch (throwable: Throwable) {
            withContext(NonCancellable + Dispatchers.IO) {
                dao.finishRun(runId, System.currentTimeMillis(), "failed", throwable.message)
            }
            throw throwable
        }
    }

    private fun loadIpText(ipMode: IpMode, overrideText: String): String {
        if (overrideText.isNotBlank()) return overrideText
        return BuiltInIpSources.load(context, ipMode)
    }

    private fun List<SpeedTestResult>.toEntities(runId: Long): List<SpeedTestResultEntity> = mapIndexed { index, result ->
        SpeedTestResultEntity(
            runId = runId,
            rank = index + 1,
            ip = result.candidate.hostAddress,
            port = result.port,
            sent = result.sent,
            received = result.received,
            lossRate = result.lossRate,
            averageDelayMillis = result.averageDelayMillis,
            downloadSpeedMbps = result.downloadSpeedMbps,
            colo = result.colo,
        )
    }
}

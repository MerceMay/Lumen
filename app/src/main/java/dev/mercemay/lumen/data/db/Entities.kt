package dev.mercemay.lumen.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "speed_test_runs")
data class SpeedTestRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAtMillis: Long,
    val finishedAtMillis: Long? = null,
    val status: String,
    val networkType: String? = null,
    val errorMessage: String? = null,
    val pushStatus: String? = null,
)

@Entity(
    tableName = "speed_test_results",
    foreignKeys = [
        ForeignKey(
            entity = SpeedTestRunEntity::class,
            parentColumns = ["id"],
            childColumns = ["runId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("runId")],
)
data class SpeedTestResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val runId: Long,
    val rank: Int,
    val ip: String,
    val port: Int,
    val sent: Int,
    val received: Int,
    val lossRate: Double,
    val averageDelayMillis: Double,
    val downloadSpeedMbps: Double,
    val colo: String? = null,
)

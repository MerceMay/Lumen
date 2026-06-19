package dev.mercemay.lumen.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedTestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: SpeedTestRunEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<SpeedTestResultEntity>)

    @Query("UPDATE speed_test_runs SET finishedAtMillis = :finishedAtMillis, status = :status, errorMessage = :errorMessage WHERE id = :runId")
    suspend fun finishRun(runId: Long, finishedAtMillis: Long, status: String, errorMessage: String? = null)

    @Query("SELECT * FROM speed_test_runs ORDER BY startedAtMillis DESC")
    fun observeRuns(): Flow<List<SpeedTestRunEntity>>

    @Query("SELECT * FROM speed_test_results WHERE runId = :runId ORDER BY rank ASC")
    fun observeResults(runId: Long): Flow<List<SpeedTestResultEntity>>

    @Query("SELECT * FROM speed_test_results WHERE runId = (SELECT id FROM speed_test_runs WHERE status = 'completed' ORDER BY startedAtMillis DESC LIMIT 1) ORDER BY rank ASC")
    fun observeLatestResults(): Flow<List<SpeedTestResultEntity>>

    @Query("SELECT * FROM speed_test_results WHERE runId = (SELECT id FROM speed_test_runs WHERE status = 'completed' ORDER BY startedAtMillis DESC LIMIT 1) ORDER BY rank ASC")
    suspend fun getLatestResultsOnce(): List<SpeedTestResultEntity>

    @Query("DELETE FROM speed_test_runs WHERE id = :runId")
    suspend fun deleteRun(runId: Long)

    @Query("DELETE FROM speed_test_runs")
    suspend fun clearHistory()
}

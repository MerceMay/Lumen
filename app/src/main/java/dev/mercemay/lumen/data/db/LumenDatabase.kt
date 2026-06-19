package dev.mercemay.lumen.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SpeedTestRunEntity::class, SpeedTestResultEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class LumenDatabase : RoomDatabase() {
    abstract fun speedTestDao(): SpeedTestDao
}

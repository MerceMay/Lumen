package dev.mercemay.lumen.app

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.mercemay.lumen.data.cfst.ColoParser
import dev.mercemay.lumen.data.cfst.DownloadTester
import dev.mercemay.lumen.data.cfst.HttpPinger
import dev.mercemay.lumen.data.cfst.IpRangeParser
import dev.mercemay.lumen.data.cfst.IpSampler
import dev.mercemay.lumen.data.cfst.ResultFilterSorter
import dev.mercemay.lumen.data.cfst.TcpPinger
import dev.mercemay.lumen.data.db.LumenDatabase
import dev.mercemay.lumen.data.db.SpeedTestDao
import dev.mercemay.lumen.data.network.OkHttpClientFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideIpRangeParser(): IpRangeParser = IpRangeParser()

    @Provides
    @Singleton
    fun provideIpSampler(): IpSampler = IpSampler()

    @Provides
    @Singleton
    fun provideTcpPinger(): TcpPinger = TcpPinger()

    @Provides
    @Singleton
    fun provideColoParser(): ColoParser = ColoParser()

    @Provides
    @Singleton
    fun provideOkHttpClientFactory(): OkHttpClientFactory = OkHttpClientFactory()

    @Provides
    @Singleton
    fun provideHttpPinger(factory: OkHttpClientFactory, coloParser: ColoParser): HttpPinger = HttpPinger(factory, coloParser)

    @Provides
    @Singleton
    fun provideDownloadTester(factory: OkHttpClientFactory, coloParser: ColoParser): DownloadTester = DownloadTester(factory, coloParser)

    @Provides
    @Singleton
    fun provideResultFilterSorter(): ResultFilterSorter = ResultFilterSorter()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LumenDatabase = Room.databaseBuilder(
        context,
        LumenDatabase::class.java,
        "lumen.db",
    ).addMigrations(MIGRATION_1_2).build()

    @Provides
    fun provideSpeedTestDao(database: LumenDatabase): SpeedTestDao = database.speedTestDao()

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS ip_files")
        }
    }
}

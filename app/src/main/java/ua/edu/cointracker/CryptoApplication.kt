package ua.edu.cointracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import ua.edu.cointracker.data.worker.CryptoSyncScheduler
import javax.inject.Inject
import javax.inject.Provider

// Підключає Hilt і передає WorkManager фабрику воркерів із залежностями.
@HiltAndroidApp
class CryptoApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var cryptoSyncScheduler: Provider<CryptoSyncScheduler>

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(this, workManagerConfiguration)
        cryptoSyncScheduler.get().schedulePeriodicSync()
    }
}

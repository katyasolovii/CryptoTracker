package ua.edu.cointracker.data.worker

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import ua.edu.cointracker.domain.model.SyncState
import ua.edu.cointracker.domain.model.SyncStatus
import ua.edu.cointracker.domain.repository.CryptoSyncScheduler as CryptoSyncSchedulerContract
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// Планує фонове оновлення та передає його стан інтерфейсу.
class CryptoSyncScheduler @Inject constructor(
    private val workManager: WorkManager,
    @ApplicationContext context: Context
) : CryptoSyncSchedulerContract {
    private val applicationContext = context.applicationContext

    fun schedulePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<CryptoSyncWorker>(1, TimeUnit.HOURS)
            .setConstraints(networkConstraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            CryptoSyncWorker.PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    override fun enqueueManualSync() {
        val request = OneTimeWorkRequestBuilder<CryptoSyncWorker>()
            .setConstraints(networkConstraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniqueWork(
            CryptoSyncWorker.MANUAL_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeSyncState() = combine(
        workManager.getWorkInfosForUniqueWorkFlow(CryptoSyncWorker.MANUAL_WORK_NAME),
        workManager.getWorkInfosForUniqueWorkFlow(CryptoSyncWorker.PERIODIC_WORK_NAME),
        observeNetworkConnection()
    ) { manualInfos, periodicInfos, isNetworkConnected ->
        val allWork = manualInfos + periodicInfos
        val running = allWork.firstOrNull { it.state == WorkInfo.State.RUNNING }
        val activeWork = manualInfos.firstOrNull { it.state == WorkInfo.State.ENQUEUED }
            ?: periodicInfos.firstOrNull {
                it.state == WorkInfo.State.ENQUEUED && it.runAttemptCount > 0
            }
        val failedWork = allWork
            .filter { it.state == WorkInfo.State.FAILED }
            .maxByOrNull {
                it.outputData.getLong(CryptoSyncWorker.ERROR_OCCURRED_AT_KEY, 0L)
            }
        val lastSuccessAt = allWork
            .filter { it.state == WorkInfo.State.SUCCEEDED }
            .maxOfOrNull {
                it.outputData.getLong(CryptoSyncWorker.SYNC_SUCCEEDED_AT_KEY, 0L)
            } ?: 0L
        val errorAt = failedWork?.outputData?.getLong(
            CryptoSyncWorker.ERROR_OCCURRED_AT_KEY,
            0L
        ) ?: 0L
        val hasCurrentFailure = failedWork != null && errorAt > lastSuccessAt
        val status = when {
            running != null -> SyncStatus.RUNNING
            activeWork != null && !isNetworkConnected -> SyncStatus.WAITING_FOR_NETWORK
            activeWork != null && activeWork.runAttemptCount > 0 -> SyncStatus.RETRYING
            activeWork != null -> SyncStatus.QUEUED
            hasCurrentFailure -> SyncStatus.FAILED
            else -> SyncStatus.IDLE
        }
        val statusWork = running ?: activeWork
        SyncState(
            status = status,
            message = statusWork?.progress?.getString(CryptoSyncWorker.SYNC_MESSAGE_KEY),
            error = failedWork?.outputData?.getString(CryptoSyncWorker.ERROR_MESSAGE_KEY)
                .takeIf { status == SyncStatus.FAILED },
            errorAt = errorAt,
            lastSuccessAt = lastSuccessAt
        )
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeNetworkConnection() = callbackFlow {
        val manager = applicationContext.getSystemService(ConnectivityManager::class.java)
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(hasNetworkConnection())
            }

            override fun onLost(network: Network) {
                trySend(hasNetworkConnection())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                trySend(
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                )
            }
        }
        manager.registerDefaultNetworkCallback(callback)
        trySend(hasNetworkConnection())
        awaitClose { manager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    private fun hasNetworkConnection(): Boolean {
        val manager = applicationContext.getSystemService(ConnectivityManager::class.java)
        val network = manager.activeNetwork ?: return false
        return manager.getNetworkCapabilities(network)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private val networkConstraints
        get() = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
}

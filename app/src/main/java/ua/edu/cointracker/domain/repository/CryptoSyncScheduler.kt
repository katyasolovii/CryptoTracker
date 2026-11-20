package ua.edu.cointracker.domain.repository

import kotlinx.coroutines.flow.Flow
import ua.edu.cointracker.domain.model.SyncState

interface CryptoSyncScheduler {
    fun observeSyncState(): Flow<SyncState>

    fun enqueueManualSync()
}

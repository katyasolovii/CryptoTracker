package ua.edu.cointracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import ua.edu.cointracker.domain.model.SyncStatus
import ua.edu.cointracker.domain.repository.CryptoSyncScheduler
import ua.edu.cointracker.domain.usecase.GetCryptosUseCase

// Збирає дані та стан синхронізації для екрана.
@HiltViewModel
class CryptoViewModel @Inject constructor(
    private val getCryptosUseCase: GetCryptosUseCase,
    private val syncScheduler: CryptoSyncScheduler
) : ViewModel() {
    val uiState = combine(
        getCryptosUseCase(),
        syncScheduler.observeSyncState()
    ) { assets, syncState ->
        val lastUpdated = assets.maxOfOrNull { it.updatedAt }
        CryptoUiState(
            assets = assets,
            syncStatus = if (
                syncState.status == SyncStatus.FAILED &&
                syncState.errorAt <= maxOf(lastUpdated ?: 0L, syncState.lastSuccessAt)
            ) {
                SyncStatus.IDLE
            } else {
                syncState.status
            },
            syncMessage = syncState.message,
            error = syncState.error.takeIf {
                syncState.status == SyncStatus.FAILED &&
                    syncState.errorAt > maxOf(lastUpdated ?: 0L, syncState.lastSuccessAt)
            },
            lastUpdated = lastUpdated
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CryptoUiState()
    )

    init {
        viewModelScope.launch {
            if (getCryptosUseCase().first().isEmpty()) {
                syncScheduler.enqueueManualSync()
            }
        }
    }

    fun requestUpdate() {
        syncScheduler.enqueueManualSync()
    }
}

package ua.edu.cointracker.presentation

import ua.edu.cointracker.domain.model.CryptoCurrency
import ua.edu.cointracker.domain.model.SyncStatus

data class CryptoUiState(
    val assets: List<CryptoCurrency> = emptyList(),
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val syncMessage: String? = null,
    val error: String? = null,
    val lastUpdated: Long? = null
)

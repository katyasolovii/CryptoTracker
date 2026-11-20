package ua.edu.cointracker.domain.model

enum class SyncStatus {
    IDLE,
    RUNNING,
    WAITING_FOR_NETWORK,
    RETRYING,
    QUEUED,
    FAILED
}

data class SyncState(
    val status: SyncStatus = SyncStatus.IDLE,
    val message: String? = null,
    val error: String? = null,
    val errorAt: Long = 0L,  // Навіщо два поля часу (errorAt і lastSuccessAt): воркер зберігає історію завершених задач. Порівнюючи errorAt і lastSuccessAt, ViewModel розуміє, чи помилка ще актуальна, чи після неї вже відбулося успішне оновлення і помилку треба сховати
    val lastSuccessAt: Long = 0L
)

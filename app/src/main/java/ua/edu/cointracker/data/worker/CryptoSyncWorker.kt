package ua.edu.cointracker.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import ua.edu.cointracker.domain.usecase.SyncCryptosUseCase
import java.io.IOException

// Виконує синхронізацію у фоні з залежностями, наданими Hilt.
@HiltWorker
class CryptoSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val syncCryptosUseCase: SyncCryptosUseCase
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        setProgress(workDataOf(SYNC_MESSAGE_KEY to "Завантаження даних"))

        return try {
            syncCryptosUseCase()
            setProgress(workDataOf())
            Result.success(workDataOf(SYNC_SUCCEEDED_AT_KEY to System.currentTimeMillis()))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: HttpException) {
            handleHttpError(exception.code())
        } catch (exception: IOException) {
            retry("Помилка мережі. WorkManager повторить синхронізацію.")
        } catch (exception: Exception) {
            failure(
                "Не вдалося оновити криптовалюти: " +
                    (exception.localizedMessage ?: "невідома помилка")
            )
        }
    }

    private suspend fun handleHttpError(code: Int): Result =
        when {
            code == 401 || code == 403 ->
                failure("Помилка ключа або доступу до CoinGecko (HTTP $code). Перевірте COINGECKO_API_KEY.")
            code == 429 ->
                retry("CoinGecko обмежив частоту запитів (HTTP 429). Очікується повторна спроба.")
            code == 408 || code >= 500 ->
                retry("Тимчасова помилка CoinGecko (HTTP $code). Очікується повторна спроба.")
            else -> failure("CoinGecko повернув помилку HTTP $code.")
        }

    private suspend fun retry(message: String): Result {
        setProgress(workDataOf(SYNC_MESSAGE_KEY to message))
        return Result.retry()
    }

    private fun failure(message: String): Result =
        Result.failure(
            workDataOf(
                ERROR_MESSAGE_KEY to message,
                ERROR_OCCURRED_AT_KEY to System.currentTimeMillis()
            )
        )

    companion object {
        const val PERIODIC_WORK_NAME = "crypto_periodic_sync"
        const val MANUAL_WORK_NAME = "crypto_manual_sync"
        const val ERROR_MESSAGE_KEY = "error_message"
        const val ERROR_OCCURRED_AT_KEY = "error_occurred_at"
        const val SYNC_MESSAGE_KEY = "sync_message"
        const val SYNC_SUCCEEDED_AT_KEY = "sync_succeeded_at"
    }
}
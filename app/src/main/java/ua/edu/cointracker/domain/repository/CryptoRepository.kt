package ua.edu.cointracker.domain.repository

import kotlinx.coroutines.flow.Flow
import ua.edu.cointracker.domain.model.CryptoCurrency

interface CryptoRepository {
    // Принцип інверсії залежностей (DIP): domain визначає інтерфейс (що потрібно зробити), а реалізація CryptoRepositoryImpl лежить у data і підставляється через Hilt (@Binds у RepositoryModule)
    fun observeCryptos(): Flow<List<CryptoCurrency>>  // холодний реактивний потік Flow

    suspend fun syncCryptos()
}

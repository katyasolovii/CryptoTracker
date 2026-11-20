package ua.edu.cointracker.domain.usecase

import javax.inject.Inject
import ua.edu.cointracker.domain.repository.CryptoRepository

// Запускає синхронізацію через контракт репозиторію.
class SyncCryptosUseCase @Inject constructor(private val repository: CryptoRepository) {
    suspend operator fun invoke() = repository.syncCryptos()  // синтаксичний цукор у Kotlin, який дозволяє викликати екземпляр класу як звичайну функцію
}

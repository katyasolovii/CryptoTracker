package ua.edu.cointracker.domain.usecase

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import ua.edu.cointracker.domain.model.CryptoCurrency
import ua.edu.cointracker.domain.repository.CryptoRepository

// Надає потік збережених криптовалют для екрана.
class GetCryptosUseCase @Inject constructor(private val repository: CryptoRepository) {
    operator fun invoke(): Flow<List<CryptoCurrency>> = repository.observeCryptos()
}

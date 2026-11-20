package ua.edu.cointracker.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import ua.edu.cointracker.data.local.CryptoDao
import ua.edu.cointracker.data.mapper.toDomain
import ua.edu.cointracker.data.mapper.toEntity
import ua.edu.cointracker.data.remote.CoinGeckoApi
import ua.edu.cointracker.domain.model.CryptoCurrency
import ua.edu.cointracker.domain.repository.CryptoRepository
import javax.inject.Inject

// Поєднує мережеві дані з локальним сховищем.
class CryptoRepositoryImpl @Inject constructor(
    private val api: CoinGeckoApi,
    private val cryptoDao: CryptoDao
) : CryptoRepository {
    override fun observeCryptos(): Flow<List<CryptoCurrency>> =
        cryptoDao.observeAll().map { assets -> assets.map { it.toDomain() } }

    override suspend fun syncCryptos() {
        val response = api.getTopAssets()
        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val responseAssets = response.body()
            ?: throw IllegalStateException("CoinGecko повернув порожню або некоректну відповідь.")
        if (responseAssets.isEmpty() || responseAssets.size > 30) {
            throw IllegalStateException("CoinGecko повернув некоректну кількість криптовалют.")
        }
        val updatedAt = System.currentTimeMillis()
        val assets = responseAssets.mapIndexedNotNull { index, item ->
            item.toEntity(updatedAt, index + 1)
        }
        val hasMissingAssets = assets.size != responseAssets.size
        val hasDuplicateIds = assets.map { it.id }.distinct().size != assets.size
        if (hasMissingAssets || hasDuplicateIds) {
            throw IllegalStateException("CoinGecko повернув некоректні дані криптовалют.")
        }

        cryptoDao.replaceAll(assets)
    }
}

package ua.edu.cointracker.data.mapper

import ua.edu.cointracker.data.local.CryptoEntity
import ua.edu.cointracker.data.remote.CoinGeckoMarketDto
import ua.edu.cointracker.domain.model.CryptoCurrency

fun CoinGeckoMarketDto.toEntity(updatedAt: Long, rank: Int): CryptoEntity? {
    val cryptoId = id?.takeIf(String::isNotBlank) ?: return null
    val cryptoName = name?.takeIf(String::isNotBlank) ?: return null
    val cryptoSymbol = symbol?.takeIf(String::isNotBlank) ?: return null

    return CryptoEntity(
        id = cryptoId,
        name = cryptoName,
        symbol = cryptoSymbol.uppercase(),
        logoUrl = image?.takeIf(String::isNotBlank),
        priceUsd = currentPrice,
        marketRank = rank,
        updatedAt = updatedAt
    )
}

fun CryptoEntity.toDomain() = CryptoCurrency(
    id = id,
    name = name,
    symbol = symbol,
    imageUrl = logoUrl,
    priceUsd = priceUsd,
    marketRank = marketRank,
    updatedAt = updatedAt
)

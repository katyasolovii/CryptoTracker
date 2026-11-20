package ua.edu.cointracker.data.remote

import com.google.gson.annotations.SerializedName

data class CoinGeckoMarketDto(
    val id: String?,
    val name: String?,
    val symbol: String?,
    val image: String?,
    @SerializedName("current_price") val currentPrice: Double?
)

package ua.edu.cointracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crypto_assets")
data class CryptoEntity(
    @PrimaryKey val id: String,
    val name: String,
    val symbol: String,
    val logoUrl: String?,
    val priceUsd: Double?,
    val marketRank: Int,
    val updatedAt: Long
)

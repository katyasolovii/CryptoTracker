package ua.edu.cointracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CryptoDao {
    @Query("SELECT * FROM crypto_assets ORDER BY marketRank ASC")
    fun observeAll(): Flow<List<CryptoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assets: List<CryptoEntity>)

    @Query("DELETE FROM crypto_assets")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(assets: List<CryptoEntity>) {
        deleteAll()
        insertAll(assets)
    }
}

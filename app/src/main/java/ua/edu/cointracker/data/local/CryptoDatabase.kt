package ua.edu.cointracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// Описує локальну базу даних і надає доступ до DAO.
@Database(entities = [CryptoEntity::class], version = 2, exportSchema = false)
abstract class CryptoDatabase : RoomDatabase() {
    abstract fun cryptoDao(): CryptoDao
}

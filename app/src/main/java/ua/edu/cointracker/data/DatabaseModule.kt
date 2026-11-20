package ua.edu.cointracker.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ua.edu.cointracker.data.local.CryptoDao
import ua.edu.cointracker.data.local.CryptoDatabase
import javax.inject.Singleton

// Створює базу даних та DAO для локального збереження.
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideCryptoDatabase(
        @ApplicationContext context: Context
    ): CryptoDatabase =
        Room.databaseBuilder(
            context,
            CryptoDatabase::class.java,
            "crypto_database"
        ).build()

    @Provides
    fun provideCryptoDao(database: CryptoDatabase): CryptoDao =
        database.cryptoDao()
}

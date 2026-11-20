package ua.edu.cointracker.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ua.edu.cointracker.data.repository.CryptoRepositoryImpl
import ua.edu.cointracker.data.worker.CryptoSyncScheduler
import ua.edu.cointracker.domain.repository.CryptoRepository
import ua.edu.cointracker.domain.repository.CryptoSyncScheduler as CryptoSyncSchedulerContract
import javax.inject.Singleton

// З'єднує контракти domain з їхніми реалізаціями у data.
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCryptoRepository(
        implementation: CryptoRepositoryImpl
    ): CryptoRepository

    @Binds
    @Singleton
    abstract fun bindCryptoSyncScheduler(
        implementation: CryptoSyncScheduler
    ): CryptoSyncSchedulerContract
}

package ua.edu.cointracker.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ua.edu.cointracker.BuildConfig
import ua.edu.cointracker.data.remote.CoinGeckoApi
import javax.inject.Singleton

// Надає мережеві залежності для завантаження криптовалют.
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val authorizationInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
            if (BuildConfig.COINGECKO_API_KEY.isNotBlank()) {
                request.header("x-cg-demo-api-key", BuildConfig.COINGECKO_API_KEY)
            }
            chain.proceed(request.build())
        }

        return OkHttpClient.Builder()
            .addInterceptor(authorizationInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.coingecko.com/api/v3/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideCoinGeckoApi(retrofit: Retrofit): CoinGeckoApi =
        retrofit.create(CoinGeckoApi::class.java)
}
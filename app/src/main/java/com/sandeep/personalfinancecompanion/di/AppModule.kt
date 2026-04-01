package com.sandeep.personalfinancecompanion.di

import com.sandeep.personalfinancecompanion.data.remote.FakeMockEngine
import com.sandeep.personalfinancecompanion.data.remote.TransactionApiService
import com.sandeep.personalfinancecompanion.data.repository.TransactionRepositoryImpl
import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(FakeMockEngine.engine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }
        }
    }

    @Provides
    @Singleton
    fun provideTransactionApiService(client: HttpClient): TransactionApiService {
        return TransactionApiService(client)
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(
        apiService: TransactionApiService
    ): TransactionRepository {
        return TransactionRepositoryImpl(apiService)
    }
}

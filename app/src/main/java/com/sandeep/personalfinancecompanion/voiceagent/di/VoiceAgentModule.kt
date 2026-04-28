package com.sandeep.personalfinancecompanion.voiceagent.di

import com.sandeep.personalfinancecompanion.BuildConfig

import com.sandeep.personalfinancecompanion.domain.parser.SmartTransactionParser
import com.sandeep.personalfinancecompanion.voiceagent.data.GeminiDataSource
import com.sandeep.personalfinancecompanion.voiceagent.data.VoiceAgentRepositoryImpl
import com.sandeep.personalfinancecompanion.voiceagent.domain.VoiceAgentParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VoiceAgentModule {

    @Provides
    @Singleton
    fun provideGeminiDataSource(): GeminiDataSource {
        return GeminiDataSource(BuildConfig.GEMINI_API_KEY)
    }

    @Provides
    @Singleton
    fun provideVoiceAgentParser(
        geminiSource: GeminiDataSource,
        mlKitParser: SmartTransactionParser
    ): VoiceAgentParser {
        return VoiceAgentRepositoryImpl(geminiSource, mlKitParser)
    }
}

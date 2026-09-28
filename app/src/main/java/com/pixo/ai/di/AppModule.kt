package com.pixo.ai.di

import com.pixo.ai.data.AiImageRepository
import com.pixo.ai.data.OpenAiImageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAiImageRepository(
        impl: OpenAiImageRepository
    ): AiImageRepository
}

package org.lightbox.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.lightbox.ai.AppPredictor
import org.lightbox.ai.AppPredictorImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppPredictorModule {
    @Provides @Singleton
    fun provideAppPredictor(): AppPredictor = AppPredictorImpl()
}

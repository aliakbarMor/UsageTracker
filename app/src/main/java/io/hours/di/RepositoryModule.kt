package io.hours.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.hours.model.UsageRepository
import io.hours.model.database.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUsageRepository(
        @ApplicationContext context: Context, appDatabase: AppDatabase
    ): UsageRepository {
        return UsageRepository(context, appDatabase.usageCellDao, appDatabase.appCellDao)
    }

}
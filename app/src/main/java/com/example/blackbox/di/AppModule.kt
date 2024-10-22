package com.example.blackbox.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.example.blackbox.common.NETWORK_BASE_URL
import com.example.blackbox.data.AppDatabase
import com.example.blackbox.data.repository.UserPreferencesRepositoryImpl
import com.example.blackbox.data.repository.AppUsageRepositoryImpl
import com.example.blackbox.data.manager.PermissionsManager
import com.example.blackbox.data.repository.RecordedUsageStatsRepositoryImpl
import com.example.blackbox.data.manager.AppUsageStatsManager
import com.example.blackbox.common.USER_PREFERENCES
import com.example.blackbox.data.remote.IOTAApi
import com.example.blackbox.data.repository.IOTARepositoryImpl
import com.example.blackbox.domain.repository.AppUsageRepository
import com.example.blackbox.domain.repository.IOTARepository
import com.example.blackbox.domain.repository.RecordedUsageStatsRepository
import com.example.blackbox.domain.repository.UserPreferencesRepository
import com.example.blackbox.domain.use_case.GetData
import com.example.blackbox.domain.use_case.GetRecords
import com.example.blackbox.domain.use_case.IOTAUseCases
import com.example.blackbox.domain.use_case.RecordingServiceUseCases
import com.example.blackbox.domain.use_case.RecordsUseCases
import com.example.blackbox.domain.use_case.SendData
import com.example.blackbox.domain.use_case.StartRecordingService
import com.example.blackbox.domain.use_case.StopRecordingService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideAppUsageRepository(db: AppDatabase): AppUsageRepository {
        return AppUsageRepositoryImpl(db.appUsageDao)
    }

    @Provides
    @Singleton
    fun provideRecordedUsageStatsRepository(db: AppDatabase): RecordedUsageStatsRepository {
        return RecordedUsageStatsRepositoryImpl(db.recordedUsageStatsDao)
    }

    @Provides
    @Singleton
    fun providePermissionManager(@ApplicationContext context: Context): PermissionsManager {
        return PermissionsManager(context)
    }

    @Provides
    @Singleton
    fun provideUsageStatsManager(@ApplicationContext context: Context, permissionsManager: PermissionsManager): AppUsageStatsManager {
        return AppUsageStatsManager(context, permissionsManager)
    }

    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            migrations = listOf(SharedPreferencesMigration(appContext, USER_PREFERENCES)),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { appContext.preferencesDataStoreFile(USER_PREFERENCES) }
        )
    }

    @Singleton
    @Provides
    fun provideUserPreferencesRepository(dataStore: DataStore<Preferences>): UserPreferencesRepository {
        return UserPreferencesRepositoryImpl(dataStore)
    }

    @Singleton
    @Provides
    fun provideRecordsUseCases(repository: RecordedUsageStatsRepository): RecordsUseCases {
        return RecordsUseCases(
            getRecords = GetRecords(repository),
        )
    }

    @Singleton
    @Provides
    fun provideRecordingServiceUseCases(@ApplicationContext context: Context): RecordingServiceUseCases {
        return RecordingServiceUseCases(
            startRecordingService = StartRecordingService(context),
            stopRecordingService = StopRecordingService(context)
        )
    }

    @Singleton
    @Provides
    fun provideIOTAApi(): IOTAApi {
        return Retrofit.Builder()
            .baseUrl(NETWORK_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IOTAApi::class.java)
    }

    @Singleton
    @Provides
    fun provideIOTARepository(api: IOTAApi): IOTARepository {
        return IOTARepositoryImpl(api)
    }

    @Singleton
    @Provides
    fun provideIOTAUseCases(repository: IOTARepository): IOTAUseCases {
        return IOTAUseCases(
            getData = GetData(repository),
            sendData = SendData(repository)
        )
    }
}
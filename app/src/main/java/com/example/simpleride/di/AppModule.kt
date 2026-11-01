package com.example.simpleride.di

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.simpleride.data.local.SimulatedVehicleDataSource
import com.example.simpleride.data.local.VehicleDataSource
import com.example.voltride.data.local.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideVehicleDataSource(): VehicleDataSource = SimulatedVehicleDataSource()

    @Provides
    @Singleton
    fun providePreferenceManager(
        @ApplicationContext context: Context
    ): PreferenceManager {
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("simple_ride_prefs") }
        )
        return PreferenceManager(dataStore)
    }



}
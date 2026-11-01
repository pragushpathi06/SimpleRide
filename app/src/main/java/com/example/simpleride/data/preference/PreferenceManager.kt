package com.example.voltride.data.local.preference

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    suspend fun saveDisplayTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DISPLAY_THEME] = theme
        }
    }

    fun getDisplayThemeFlow(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.DISPLAY_THEME] ?: "DARK"
        }
    }

    suspend fun saveSpeedUnit(unit: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SPEED_UNIT] = unit
        }
    }

    fun getSpeedUnitFlow(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SPEED_UNIT] ?: "KMH"
        }
    }

    suspend fun saveVehicle(
        vehicleId: String,
        vehicleName: String
    ) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.VEHICLE_ID] = vehicleId
            preferences[PreferencesKeys.VEHICLE_NAME] = vehicleName
        }
    }

    fun getVehicleIdFlow(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.VEHICLE_ID]
        }
    }

    fun getVehicleNameFlow(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.VEHICLE_NAME]
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    fun getSoundEnabledFlow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] ?: true
        }
    }

    suspend fun setWarningAlertsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.WARNING_ALERTS_ENABLED] = enabled
        }
    }

    fun getWarningAlertsEnabledFlow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.WARNING_ALERTS_ENABLED] ?: true
        }
    }

    suspend fun setDiagnosticsVisible(visible: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_DIAGNOSTICS] = visible
        }
    }

    fun getDiagnosticsVisibleFlow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SHOW_DIAGNOSTICS] ?: false
        }
    }

    // Save selected drive mode
    suspend fun saveDriveMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DRIVE_MODE] = mode
        }
    }

    fun getDriveModeFlow(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.DRIVE_MODE] ?: "NORMAL"
        }
    }

    suspend fun saveDisplayBrightness(brightness: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DISPLAY_BRIGHTNESS] =
                brightness.coerceIn(0f, 1f)
        }
    }

    fun getDisplayBrightnessFlow(): Flow<Float> {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.DISPLAY_BRIGHTNESS] ?: 1f
        }
    }

    suspend fun getSpeedUnit(): String {
        return dataStore.data.first()[
            PreferencesKeys.SPEED_UNIT
        ] ?: "KMH"
    }

    suspend fun clearAllPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
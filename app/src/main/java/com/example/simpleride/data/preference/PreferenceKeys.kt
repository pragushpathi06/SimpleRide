package com.example.voltride.data.local.preference

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {

    val DISPLAY_THEME = stringPreferencesKey("display_theme")
    val SPEED_UNIT = stringPreferencesKey("speed_unit")
    val DISPLAY_BRIGHTNESS = floatPreferencesKey("display_brightness")

    val VEHICLE_ID = stringPreferencesKey("vehicle_id")
    val VEHICLE_NAME = stringPreferencesKey("vehicle_name")

    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val WARNING_ALERTS_ENABLED =
        booleanPreferencesKey("warning_alerts_enabled")
    val SHOW_DIAGNOSTICS = booleanPreferencesKey("show_diagnostics")

    val DRIVE_MODE = stringPreferencesKey("drive_mode")
}

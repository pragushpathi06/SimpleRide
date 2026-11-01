package com.example.simpleride.data.local

import kotlinx.coroutines.flow.Flow

data class VehicleSignal(
    val speedKmh: Float?,
    val motorOn: Boolean?,
    val killSwitchActive: Boolean?,
)

interface VehicleDataSource {
    val signals: Flow<VehicleSignal>
}

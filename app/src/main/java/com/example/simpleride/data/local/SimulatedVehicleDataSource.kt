package com.example.simpleride.data.local

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


data class SimStep(
    val label: String,
    val speedKmh: Float?,
    val motorOn: Boolean?,
    val killActive: Boolean?,
    val ticks: Int = 3,
    val transmit: Boolean = true,
)

class SimulatedVehicleDataSource(
    private val tickMs: Long = 1_000L,
    private val script: List<SimStep> = DEFAULT_SCRIPT,
    private val loop: Boolean = true,
) : VehicleDataSource {

    override val signals: Flow<VehicleSignal> = flow {
        do {
            for (step in script) {
                repeat(step.ticks) {
                    if (step.transmit) {
                        emit(VehicleSignal(step.speedKmh, step.motorOn, step.killActive))
                    }
                    delay(tickMs)
                }
            }
        } while (loop)
    }

    companion object {
        val DEFAULT_SCRIPT = listOf(
            SimStep("Parked",         0f,   motorOn = false, killActive = false),
            SimStep("Ready",          0f,   motorOn = true,  killActive = false),
            SimStep("Riding",         25f,  motorOn = true,  killActive = false),
            SimStep("Accelerating",   50f,  motorOn = true,  killActive = false),
            SimStep("Motor disabled", 0f,   motorOn = false, killActive = true),
            SimStep("Invalid speed",  -12f, motorOn = true,  killActive = false, ticks = 2),
            SimStep("Data dropout",   null, motorOn = null,  killActive = null,  ticks = 4, transmit = false),
            SimStep("Motor flag lost", 25f, motorOn = null,  killActive = false, ticks = 2),
        )
    }
}

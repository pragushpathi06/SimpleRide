package com.example.simpleride.domain.mapper

import com.example.simpleride.domain.model.DataIssue
import com.example.simpleride.domain.model.DriveMode
import com.example.simpleride.domain.model.KillSwitchStatus
import com.example.simpleride.domain.model.MotorStatus
import com.example.simpleride.domain.model.VehicleState
import com.example.simpleride.data.local.VehicleSignal
import kotlin.math.roundToInt


object VehicleStateMapper {
    const val MAX_VALID_SPEED_KMH = 200f
    const val ACCELERATING_FROM_KMH = 40

    fun map(signal: VehicleSignal): VehicleState {
        val issues = mutableSetOf<DataIssue>()

        val raw = signal.speedKmh
        val speed: Int? = when {
            raw == null -> {
                issues += DataIssue.SPEED_MISSING
                null
            }
            raw.isNaN() || raw.isInfinite() || raw < 0f || raw > MAX_VALID_SPEED_KMH -> {
                issues += DataIssue.SPEED_INVALID
                null
            }
            else -> raw.roundToInt()
        }

        val motor = when (signal.motorOn) {
            true -> MotorStatus.ON
            false -> MotorStatus.OFF
            null -> {
                issues += DataIssue.MOTOR_MISSING
                MotorStatus.UNKNOWN
            }
        }

        val kill = when (signal.killSwitchActive) {
            true -> KillSwitchStatus.ACTIVE
            false -> KillSwitchStatus.INACTIVE
            null -> {
                issues += DataIssue.KILL_SWITCH_MISSING
                KillSwitchStatus.UNKNOWN
            }
        }

        if (kill == KillSwitchStatus.ACTIVE && motor == MotorStatus.ON) {
            issues += DataIssue.KILL_ACTIVE_WHILE_MOTOR_ON
        }

        return VehicleState(speed, motor, kill, deriveMode(speed, motor, kill), issues)
    }

    private fun deriveMode(speed: Int?, motor: MotorStatus, kill: KillSwitchStatus): DriveMode = when {
        kill == KillSwitchStatus.ACTIVE -> DriveMode.MOTOR_DISABLED
        speed == null || motor == MotorStatus.UNKNOWN || kill == KillSwitchStatus.UNKNOWN -> DriveMode.DEGRADED
        motor == MotorStatus.OFF -> if (speed == 0) DriveMode.PARKED else DriveMode.COASTING
        speed == 0 -> DriveMode.READY
        speed >= ACCELERATING_FROM_KMH -> DriveMode.ACCELERATING
        else -> DriveMode.RIDING
    }
}

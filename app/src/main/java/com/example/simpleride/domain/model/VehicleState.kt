package com.example.simpleride.domain.model

data class VehicleState(
    val speedKmh: Int?,
    val motor: MotorStatus,
    val killSwitch: KillSwitchStatus,
    val mode: DriveMode,
    val issues: Set<DataIssue> = emptySet(),
) {
    companion object {
        fun signalLost() = VehicleState(
            speedKmh = null,
            motor = MotorStatus.UNKNOWN,
            killSwitch = KillSwitchStatus.UNKNOWN,
            mode = DriveMode.SIGNAL_LOST,
            issues = setOf(DataIssue.STALE),
        )
    }
}



enum class MotorStatus { ON, OFF, UNKNOWN }

enum class KillSwitchStatus { ACTIVE, INACTIVE, UNKNOWN }

enum class DriveMode {
    PARKED,
    READY,
    RIDING,
    ACCELERATING, MOTOR_DISABLED,
    COASTING,
    DEGRADED,
    SIGNAL_LOST
}

enum class DataIssue {
    STALE,
    SPEED_MISSING,
    SPEED_INVALID,
    MOTOR_MISSING,
    KILL_SWITCH_MISSING,
    KILL_ACTIVE_WHILE_MOTOR_ON,
}
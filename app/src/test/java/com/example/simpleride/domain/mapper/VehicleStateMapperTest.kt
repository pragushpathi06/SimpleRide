package com.example.simpleride.domain.mapper

import com.example.simpleride.data.local.VehicleSignal
import com.example.simpleride.domain.model.DataIssue
import com.example.simpleride.domain.model.DriveMode
import com.example.simpleride.domain.model.KillSwitchStatus
import com.example.simpleride.domain.model.MotorStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleStateMapperTest {

    private fun map(speed: Float?, motor: Boolean?, kill: Boolean?) =
        VehicleStateMapper.map(VehicleSignal(speed, motor, kill))


    @Test fun parked() {
        val s = map(0f, false, false)
        assertEquals(DriveMode.PARKED, s.mode)
        assertEquals(0, s.speedKmh)
        assertTrue(s.issues.isEmpty())
    }

    @Test fun ready() = assertEquals(DriveMode.READY, map(0f, true, false).mode)

    @Test fun riding() {
        val s = map(25f, true, false)
        assertEquals(DriveMode.RIDING, s.mode)
        assertEquals(25, s.speedKmh)
    }

    @Test fun accelerating() = assertEquals(DriveMode.ACCELERATING, map(50f, true, false).mode)

    @Test fun motorDisabled() {
        val s = map(0f, false, true)
        assertEquals(DriveMode.MOTOR_DISABLED, s.mode)
        assertEquals(KillSwitchStatus.ACTIVE, s.killSwitch)
        assertEquals(MotorStatus.OFF, s.motor)
    }

    // ---- edge / failure cases ----

    @Test fun negativeSpeedIsDiscarded() {
        val s = map(-12f, true, false)
        assertNull(s.speedKmh)
        assertTrue(DataIssue.SPEED_INVALID in s.issues)
        assertEquals(DriveMode.DEGRADED, s.mode)
    }

    @Test fun nanAndOutOfRangeSpeedAreDiscarded() {
        assertNull(map(Float.NaN, true, false).speedKmh)
        assertNull(map(Float.POSITIVE_INFINITY, true, false).speedKmh)
        assertNull(map(VehicleStateMapper.MAX_VALID_SPEED_KMH + 1, true, false).speedKmh)
    }

    @Test fun missingSignalsAreFlaggedNotCrashing() {
        val s = map(null, null, null)
        assertEquals(MotorStatus.UNKNOWN, s.motor)
        assertEquals(KillSwitchStatus.UNKNOWN, s.killSwitch)
        assertEquals(
            setOf(DataIssue.SPEED_MISSING, DataIssue.MOTOR_MISSING, DataIssue.KILL_SWITCH_MISSING),
            s.issues
        )
    }

    @Test fun motorOffWhileMovingIsCoastingNotZeroSpeed() {
        val s = map(30f, false, false)
        assertEquals(DriveMode.COASTING, s.mode)
        assertEquals(30, s.speedKmh)
    }

    @Test fun killActiveWhileMotorOnIsFlagged() {
        val s = map(10f, true, true)
        assertTrue(DataIssue.KILL_ACTIVE_WHILE_MOTOR_ON in s.issues)
        assertEquals(DriveMode.MOTOR_DISABLED, s.mode)
    }
}
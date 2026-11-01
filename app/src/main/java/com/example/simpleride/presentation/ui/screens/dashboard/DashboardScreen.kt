package com.example.simpleride.presentation.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.simpleride.domain.model.DataIssue
import com.example.simpleride.domain.model.DriveMode
import com.example.simpleride.domain.model.KillSwitchStatus
import com.example.simpleride.domain.model.MotorStatus
import com.example.simpleride.domain.model.VehicleState
import com.example.simpleride.presentation.ui.design_system.Dot
import com.example.simpleride.presentation.ui.design_system.Hmi
import com.example.simpleride.presentation.ui.design_system.IconTile
import com.example.simpleride.presentation.ui.design_system.MonoText
import com.example.simpleride.presentation.ui.design_system.Panel
import com.example.simpleride.presentation.ui.design_system.SpeedGauge
import com.example.simpleride.presentation.ui.design_system.Tag

@Composable
fun DashboardScreen(
    viewModel: DashBoardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardContent(uiState)
}

@Composable
fun DashboardContent(uiState: DashBoardUiState, modifier: Modifier = Modifier) {
    val state = uiState.vehicle
    Column(
        modifier
            .fillMaxSize()
            .background(Hmi.Bg)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HmiFrame(state, Modifier.weight(1f))
    }
}


private fun DriveMode.label() = when (this) {
    DriveMode.PARKED -> "PARKED"
    DriveMode.READY -> "READY"
    DriveMode.RIDING -> "RIDING"
    DriveMode.ACCELERATING -> "ACCELERATING"
    DriveMode.MOTOR_DISABLED -> "MOTOR DISABLED"
    DriveMode.COASTING -> "COASTING"
    DriveMode.DEGRADED -> "DEGRADED DATA"
    DriveMode.SIGNAL_LOST -> "SIGNAL LOST"
}

private fun DriveMode.description() = when (this) {
    DriveMode.PARKED -> "Motor off • vehicle secured"
    DriveMode.READY -> "Motor on • stationary"
    DriveMode.RIDING -> "Motor on • cruising"
    DriveMode.ACCELERATING -> "Motor on • high speed"
    DriveMode.MOTOR_DISABLED -> "Kill switch active • run disabled"
    DriveMode.COASTING -> "Motor off • vehicle still moving"
    DriveMode.DEGRADED -> "Some signals invalid or missing"
    DriveMode.SIGNAL_LOST -> "No recent data from vehicle"
}

@Composable
private fun DriveMode.color(): Color = when (this) {
    DriveMode.MOTOR_DISABLED -> Hmi.Danger
    DriveMode.DEGRADED, DriveMode.SIGNAL_LOST -> Hmi.Warn
    DriveMode.PARKED -> Hmi.Muted
    else -> Hmi.Accent
}

private fun DataIssue.message() = when (this) {
    DataIssue.STALE -> "No update within timeout – values hidden"
    DataIssue.SPEED_MISSING -> "Speed signal missing"
    DataIssue.SPEED_INVALID -> "Speed invalid (negative / NaN / out of range) – discarded"
    DataIssue.MOTOR_MISSING -> "Motor status missing"
    DataIssue.KILL_SWITCH_MISSING -> "Kill-switch status missing"
    DataIssue.KILL_ACTIVE_WHILE_MOTOR_ON -> "Kill switch active but motor reports ON"
}



@Composable
private fun HmiFrame(state: VehicleState, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Hmi.Accent.copy(alpha = .10f).compositeOver(Hmi.FrameTop),
                        Hmi.FrameTop,
                        Hmi.FrameBottom
                    )
                )
            )
            .border(1.dp, Hmi.Accent.copy(alpha = .18f), RoundedCornerShape(26.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TopBar(state)
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LeftColumn(state, Modifier.weight(1f).fillMaxHeight())
            Box(Modifier.weight(1.25f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                SpeedDisplay(state)
            }
            RightColumn(state, Modifier.weight(1f).fillMaxHeight())
        }
        BottomBar(state)
    }
}

@Composable
private fun TopBar(state: VehicleState) {
    val live = DataIssue.STALE !in state.issues
    val c = state.mode.color()

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Hmi.Panel.copy(alpha = .8f))
            .border(1.dp, Hmi.Border, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Hmi.Accent.copy(alpha = .6f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MonoText("⚡ ", Hmi.Accent, 18.sp)
            MonoText("Simple", Hmi.Text, 18.sp, FontWeight.Black)
            MonoText("RIDE", Hmi.Accent, 18.sp, FontWeight.Black)
            Spacer(Modifier.width(8.dp))
            Tag("EV-900", Hmi.Accent)
        }
        Spacer(Modifier.width(14.dp))
        Row(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(Hmi.PanelHi)
                .border(1.dp, c.copy(alpha = .6f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Dot(c, 8.dp)
            Spacer(Modifier.width(6.dp))
            MonoText(state.mode.label(), Hmi.Text, 12.sp, FontWeight.Bold)
        }
        Spacer(Modifier.weight(1f))
        Dot(if (live) Hmi.Ok else Hmi.Warn, 8.dp)
        Spacer(Modifier.width(6.dp))
        MonoText(if (live) "SYS OK" else "NO SIGNAL", Hmi.Text, 11.sp)
    }
}


@Composable
private fun SpeedDisplay(state: VehicleState) {
    val color = state.mode.color()

    SpeedGauge(state.speedKmh) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Hmi.PanelHi)
                    .border(
                        width = 1.dp,
                        color = color.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 12.dp, vertical = 3.dp)
            ) {
                MonoText(
                    text = state.mode.label(),
                    color = color,
                    size = 10.sp,
                    weight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(0.dp))

            MonoText(
                text = state.speedKmh?.toString() ?: "--",
                color = if (state.speedKmh == null) Hmi.Warn else Hmi.Text,
                size = 60.sp,
                weight = FontWeight.Black
            )

            MonoText(
                text = "KM/H",
                color = Hmi.Accent,
                size = 12.sp,
                weight = FontWeight.Bold
            )
        }
    }
}


private data class Health(val text: String, val color: Color)

@Composable
private fun healthOf(state: VehicleState, vararg own: DataIssue): Health = when {
    DataIssue.STALE in state.issues -> Health("STALE", Hmi.Warn)
    own.any { it == DataIssue.SPEED_INVALID && it in state.issues } -> Health("INVALID", Hmi.Danger)
    own.any { it in state.issues } -> Health("MISSING", Hmi.Warn)
    else -> Health("OK", Hmi.Ok)
}

@Composable
private fun LeftColumn(state: VehicleState, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.Center) {
        Panel {
            MonoText("SIGNAL HEALTH", Hmi.Muted, 10.sp, FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            val rows = listOf(
                "SPEED" to healthOf(state, DataIssue.SPEED_MISSING, DataIssue.SPEED_INVALID),
                "MOTOR" to healthOf(state, DataIssue.MOTOR_MISSING),
                "KILL SWITCH" to healthOf(state, DataIssue.KILL_SWITCH_MISSING),
            )
            rows.forEach { (name, h) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    MonoText(name, Hmi.Muted, 12.sp)
                    Spacer(Modifier.weight(1f))
                    Dot(h.color, 7.dp)
                    Spacer(Modifier.width(6.dp))
                    MonoText(h.text, h.color, 12.sp, FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Panel {
            MonoText("DATA ISSUES", Hmi.Muted, 10.sp, FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            if (state.issues.isEmpty()) {
                MonoText("No active issues", Hmi.Ok, 12.sp)
            } else {
                state.issues.forEach { issue ->
                    Row(Modifier.padding(vertical = 2.dp)) {
                        MonoText("▲ ", Hmi.Warn, 11.sp)
                        MonoText(issue.message(), Hmi.Text, 11.sp)
                    }
                }
            }
        }
    }
}


@Composable
private fun RightColumn(state: VehicleState, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.Center) {
        MotorCard(state.motor)
        Spacer(Modifier.height(12.dp))
        KillSwitchCard(state.killSwitch)
    }
}

@Composable
private fun MotorCard(motor: MotorStatus) {
    val color = when (motor) {
        MotorStatus.ON -> Hmi.Ok
        MotorStatus.OFF -> Hmi.Muted
        MotorStatus.UNKNOWN -> Hmi.Warn
    }
    val (title, sub) = when (motor) {
        MotorStatus.ON -> "MOTOR ON" to "DRIVE ENABLED"
        MotorStatus.OFF -> "MOTOR OFF" to "STANDBY • ZERO TORQUE"
        MotorStatus.UNKNOWN -> "MOTOR --" to "STATUS UNAVAILABLE"
    }
    Panel {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            MonoText("PROPULSION UNIT", Hmi.Muted, 10.sp, FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Dot(color, 10.dp)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile("⚡", color, border = color.copy(alpha = .5f))
            Spacer(Modifier.width(12.dp))
            Column {
                MonoText(title, if (motor == MotorStatus.ON) Hmi.Ok else Hmi.Text, 22.sp, FontWeight.Black)
                MonoText(sub, Hmi.Muted, 10.sp)
            }
        }
    }
}

@Composable
private fun KillSwitchCard(kill: KillSwitchStatus) {
    val color = when (kill) {
        KillSwitchStatus.ACTIVE -> Hmi.Danger
        KillSwitchStatus.INACTIVE -> Hmi.Ok
        KillSwitchStatus.UNKNOWN -> Hmi.Warn
    }

    val title = when (kill) {
        KillSwitchStatus.ACTIVE -> "KILL SWITCH\nACTIVE"
        KillSwitchStatus.INACTIVE -> "KILL SWITCH\nINACTIVE"
        KillSwitchStatus.UNKNOWN -> "KILL SWITCH\nUNKNOWN"
    }

    val sub = when (kill) {
        KillSwitchStatus.ACTIVE -> "CIRCUIT OPEN • RUN DISABLED"
        KillSwitchStatus.INACTIVE -> "CIRCUIT CLOSED • RUN ENABLED"
        KillSwitchStatus.UNKNOWN -> "SIGNAL UNAVAILABLE"
    }

    val glyph = when (kill) {
        KillSwitchStatus.ACTIVE -> "⛔"
        KillSwitchStatus.INACTIVE -> "🛡"
        KillSwitchStatus.UNKNOWN -> "?"
    }

    val tag = when (kill) {
        KillSwitchStatus.ACTIVE -> "TRIP"
        KillSwitchStatus.INACTIVE -> "OK"
        KillSwitchStatus.UNKNOWN -> "N/A"
    }

    val container = if (kill == KillSwitchStatus.ACTIVE) {
        color.copy(alpha = 0.16f).compositeOver(Hmi.Panel)
    } else {
        Hmi.Panel
    }

    Panel(
        border = color.copy(
            alpha = if (kill == KillSwitchStatus.ACTIVE) 0.9f else 0.35f
        ),
        container = container
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonoText(
                    text = "SAFETY CUTOFF",
                    color = Hmi.Muted,
                    size = 10.sp,
                    weight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                Tag(tag, color)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconTile(
                    glyph = glyph,
                    tint = color,
                    border = color.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    MonoText(
                        text = title,
                        color = if (kill == KillSwitchStatus.ACTIVE) {
                            color
                        } else {
                            Hmi.Text
                        },
                        size = 14.sp,
                        weight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    MonoText(
                        text = sub,
                        color = Hmi.Muted,
                        size = 8.sp
                    )
                }
            }
        }
    }
}


@Composable
private fun BottomBar(state: VehicleState) {
    val live = DataIssue.STALE !in state.issues
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Hmi.Panel.copy(alpha = .8f))
            .border(1.dp, Hmi.Border, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MonoText("SIM STREAM: ", Hmi.Muted, 11.sp)
        Dot(if (live) Hmi.Ok else Hmi.Warn, 6.dp)
        Spacer(Modifier.width(4.dp))
        MonoText(if (live) "LIVE ~1 Hz" else "STALE", if (live) Hmi.Ok else Hmi.Warn, 12.sp, FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        MonoText("simulated data • no vehicle hardware", Hmi.Muted, 10.sp)
    }
}


@Preview(
    name = "Riding",
    device = "spec:width=1280dp,height=600dp,dpi=160",
    showBackground = true,

)
@Composable
private fun DashboardRidingPreview() {
    DashboardContent(
        uiState = DashBoardUiState(
            vehicle = VehicleState(
                speedKmh = 65,
                motor = MotorStatus.ON,
                killSwitch = KillSwitchStatus.INACTIVE,
                mode = DriveMode.RIDING,
                issues = emptySet()
            )
        )
    )
}
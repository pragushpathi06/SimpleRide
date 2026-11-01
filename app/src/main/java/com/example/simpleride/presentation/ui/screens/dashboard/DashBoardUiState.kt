package com.example.simpleride.presentation.ui.screens.dashboard

import com.example.simpleride.domain.model.VehicleState

data class DashBoardUiState (
    val vehicle: VehicleState = VehicleState.signalLost(),
)
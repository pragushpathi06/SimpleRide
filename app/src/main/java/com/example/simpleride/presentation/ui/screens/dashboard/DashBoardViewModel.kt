package com.example.simpleride.presentation.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simpleride.data.local.VehicleDataSource
import com.example.simpleride.domain.mapper.VehicleStateMapper
import com.example.simpleride.domain.model.VehicleState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashBoardViewModel @Inject constructor(
    private val dataSource: VehicleDataSource,
) : ViewModel() {

    private val _dashBoard = MutableStateFlow(DashBoardUiState())
    val uiState: StateFlow<DashBoardUiState> = _dashBoard.asStateFlow()

    init {
        dataSource.signals
            .map(VehicleStateMapper::map)
            .transformLatest { state ->
                emit(state)
                delay(STALE_TIMEOUT_MS.milliseconds)
                emit(VehicleState.signalLost())
            }
            .onEach { state -> _dashBoard.update { it.copy(vehicle = state) } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: DashBoardEvent) {}

    private companion object {
        const val STALE_TIMEOUT_MS = 2_500L
    }
}
package com.example.simpleride.presentation.ui.screens.dashboard


import com.example.simpleride.data.local.VehicleDataSource
import com.example.simpleride.data.local.VehicleSignal
import com.example.simpleride.domain.model.DataIssue
import com.example.simpleride.domain.model.DriveMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashBoardViewModelTest {

    private class FakeSource : VehicleDataSource {
        val flow = MutableSharedFlow<VehicleSignal>()
        override val signals = flow
    }

    private lateinit var source: FakeSource

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        source = FakeSource()
    }

    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun initialStateIsSignalLost() = runTest {
        val vm = DashBoardViewModel(source)
        assertEquals(DriveMode.SIGNAL_LOST, vm.uiState.value.vehicle.mode)
    }

    @Test
    fun newSignalUpdatesUiState() = runTest {
        val vm = DashBoardViewModel(source)
        source.flow.emit(VehicleSignal(25f, true, false))
        assertEquals(DriveMode.RIDING, vm.uiState.value.vehicle.mode)
        assertEquals(25, vm.uiState.value.vehicle.speedKmh)
    }

    @Test
    fun silenceBeyondTimeoutMarksSignalLost() = runTest {
        val vm = DashBoardViewModel(source)
        source.flow.emit(VehicleSignal(50f, true, false))
        assertEquals(DriveMode.ACCELERATING, vm.uiState.value.vehicle.mode)

        advanceTimeBy(2_600)
        runCurrent()

        val v = vm.uiState.value.vehicle
        assertEquals(DriveMode.SIGNAL_LOST, v.mode)
        assertEquals(null, v.speedKmh)
        assertTrue(DataIssue.STALE in v.issues)
    }

    @Test
    fun signalArrivingBeforeTimeoutKeepsStateLive() = runTest {
        val vm = DashBoardViewModel(source)
        source.flow.emit(VehicleSignal(25f, true, false))
        advanceTimeBy(1_000)
        source.flow.emit(VehicleSignal(50f, true, false))
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(DriveMode.ACCELERATING, vm.uiState.value.vehicle.mode)
    }
}
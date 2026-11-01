package com.example.simpleride.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.example.simpleride.presentation.ui.screens.dashboard.DashboardScreen
import com.example.simpleride.presentation.utils.LocalSnackBarState
import com.example.simpleride.presentation.utils.ProgressEvent
import com.example.simpleride.presentation.utils.ProgressManager
import com.example.simpleride.presentation.utils.SnackBar
import com.example.simpleride.presentation.utils.SnackBarData
import com.example.simpleride.presentation.utils.SnackBarManager
import com.example.simpleride.presentation.utils.SnackBarState
import com.example.simpleride.presentation.utils.observeEvents
import com.example.simpleride.presentation.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun App() {
    AppTheme {
        val appViewModel: AppViewModel = hiltViewModel()

        SnackBarManager.snackBarEvents.observeEvents { snackBarEvent ->
            appViewModel.showSnackBar(snackBarEvent)
        }

        ProgressManager.progressEvents.observeEvents { progressEvent ->
            appViewModel.showProgressBar.value = progressEvent == ProgressEvent.ShowProgressBar
        }

        CompositionLocalProvider(LocalSnackBarState provides appViewModel.snackBarState.value) {
            Scaffold(
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .imePadding()
                ) {
                    DashboardScreen()
                    if (appViewModel.showProgressBar.value) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                        )
                    }

                    SnackBar(
                        data = appViewModel.snackBarData.value,
                        modifier = Modifier.align(Alignment.TopCenter),
                        onSizeChange = { size ->
                            appViewModel.snackBarState.value =
                                appViewModel.snackBarState.value.copy(snackBarHeight = size.height.dp)
                        },
                        onDismiss = { appViewModel.dismissSnackBar() }
                    )
                }
            }
        }
    }
}


@HiltViewModel
class AppViewModel @Inject constructor() : ViewModel() {
    val snackBarData: MutableState<SnackBarData?> = mutableStateOf(null)
    val snackBarState = mutableStateOf(SnackBarState())
    val showProgressBar = mutableStateOf(false)

    suspend fun showSnackBar(snackBarData: SnackBarData) {
        this.snackBarData.value = snackBarData
        snackBarState.value = SnackBarState(isSnackBarVisible = true)
        delay(4000.milliseconds)
        if (snackBarState.value.isSnackBarVisible) dismissSnackBar()
    }

    fun dismissSnackBar() {
        snackBarState.value = SnackBarState(isSnackBarVisible = false, snackBarHeight = 0.dp)
        this.snackBarData.value = null
    }
}


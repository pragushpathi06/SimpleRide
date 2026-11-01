package com.example.simpleride.presentation.utils

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object ProgressManager {
    private val eventChannel = Channel<ProgressEvent>()
    val progressEvents = eventChannel.receiveAsFlow()

    suspend fun sendProgressEvent(event: ProgressEvent) {
        eventChannel.send(event)
    }

}

sealed interface ProgressEvent {
    data object ShowProgressBar: ProgressEvent
    data object HideProgressBar: ProgressEvent
}
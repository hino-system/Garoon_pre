package com.example.garoon_pre.feature.auth.presentation.gate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.garoon_pre.core.datastore.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed class GateState {
    data object Loading : GateState()
    data object LoggedIn : GateState()
    data object LoggedOut : GateState()
}

@HiltViewModel
class GateViewModel @Inject constructor(
    sessionStore: SessionStore
) : ViewModel() {

    val state = combine(
        sessionStore.tokenFlow,
        sessionStore.userIdFlow
    ) { token, userId ->
        if (token.isBlank() || userId.isBlank()) {
            GateState.LoggedOut
        } else {
            GateState.LoggedIn
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GateState.Loading
    )
}
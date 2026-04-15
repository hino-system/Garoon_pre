package com.example.garoon_pre.feature.auth.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.garoon_pre.core.datastore.ConnectionMode
import com.example.garoon_pre.core.datastore.ServerTarget
import com.example.garoon_pre.core.datastore.SessionStore
import com.example.garoon_pre.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    var userId by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var connectionMode by mutableStateOf(ConnectionMode.SERVER)
        private set

    var serverTarget by mutableStateOf(ServerTarget.EMULATOR)
        private set

    var loading by mutableStateOf(false)
        private set

    private val _saved = MutableSharedFlow<Unit>()
    val saved = _saved.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            val settings = sessionStore.getConnectionSettings()
            connectionMode = settings.mode
            serverTarget = settings.serverTarget
        }
    }

    fun onUserIdChanged(value: String) {
        userId = value
    }

    fun onPasswordChanged(value: String) {
        password = value
    }

    fun selectConnectionMode(value: ConnectionMode) {
        connectionMode = value
    }

    fun selectServerTarget(value: ServerTarget) {
        serverTarget = value
    }

    fun login() {
        if (loading) return

        val trimmedUserId = userId.trim()
        val trimmedPassword = password.trim()

        if (trimmedUserId.isBlank()) {
            viewModelScope.launch {
                _errorMessage.emit("ユーザーIDを入力してください")
            }
            return
        }

        if (trimmedPassword.isBlank()) {
            viewModelScope.launch {
                _errorMessage.emit("パスワードを入力してください")
            }
            return
        }

        viewModelScope.launch {
            loading = true

            runCatching {
                sessionStore.saveConnectionSettings(
                    mode = connectionMode,
                    serverTarget = serverTarget
                )

                authRepository.login(
                    userId = trimmedUserId,
                    password = trimmedPassword
                )
            }.onSuccess {
                _saved.emit(Unit)
            }.onFailure { e ->
                val message = when (e) {
                    is HttpException -> {
                        e.response()?.errorBody()?.string() ?: "HTTP ${e.code()} ${e.message()}"
                    }
                    else -> e.message ?: "ログインに失敗しました"
                }
                _errorMessage.emit(message)
            }

            loading = false
        }
    }
}
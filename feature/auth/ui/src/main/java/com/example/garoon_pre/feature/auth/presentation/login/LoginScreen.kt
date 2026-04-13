package com.example.garoon_pre.feature.auth.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginRoute(
    onLoginDone: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.saved.collectLatest {
            onLoginDone()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LoginScreen(
        snackbarHostState = snackbarHostState,
        userId = viewModel.userId,
        password = viewModel.password,
        loading = viewModel.loading,
        onUserIdChanged = viewModel::onUserIdChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onLogin = viewModel::login
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    snackbarHostState: SnackbarHostState,
    userId: String,
    password: String,
    loading: Boolean,
    onUserIdChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLogin: () -> Unit
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { Text("Pre_Groon ログイン") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("ユーザーIDとパスワードでログイン")

            OutlinedTextField(
                value = userId,
                onValueChange = onUserIdChanged,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                label = { Text("ユーザーID") },
                singleLine = true,
                enabled = !loading,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Text
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChanged,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                label = { Text("パスワード") },
                singleLine = true,
                enabled = !loading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onLogin() }
                )
            )

            Button(
                onClick = onLogin,
                enabled = !loading && userId.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator()
                } else {
                    Text("ログイン")
                }
            }
        }
    }
}
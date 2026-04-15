package com.example.garoon_pre.feature.auth.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
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
import com.example.garoon_pre.core.datastore.ConnectionMode
import com.example.garoon_pre.core.datastore.ServerTarget
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
        connectionMode = viewModel.connectionMode,
        serverTarget = viewModel.serverTarget,
        loading = viewModel.loading,
        onUserIdChanged = viewModel::onUserIdChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onSelectConnectionMode = viewModel::selectConnectionMode,
        onSelectServerTarget = viewModel::selectServerTarget,
        onLogin = viewModel::login
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    snackbarHostState: SnackbarHostState,
    userId: String,
    password: String,
    connectionMode: ConnectionMode,
    serverTarget: ServerTarget,
    loading: Boolean,
    onUserIdChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSelectConnectionMode: (ConnectionMode) -> Unit,
    onSelectServerTarget: (ServerTarget) -> Unit,
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

            Text(
                text = "利用モード",
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
            )

            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (connectionMode == ConnectionMode.SERVER) {
                    Button(
                        onClick = { onSelectConnectionMode(ConnectionMode.SERVER) },
                        enabled = !loading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("サーバーを使う")
                    }
                } else {
                    OutlinedButton(
                        onClick = { onSelectConnectionMode(ConnectionMode.SERVER) },
                        enabled = !loading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("サーバーを使う")
                    }
                }

                if (connectionMode == ConnectionMode.LOCAL) {
                    Button(
                        onClick = { onSelectConnectionMode(ConnectionMode.LOCAL) },
                        enabled = !loading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ローカルのみ")
                    }
                } else {
                    OutlinedButton(
                        onClick = { onSelectConnectionMode(ConnectionMode.LOCAL) },
                        enabled = !loading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ローカルのみ")
                    }
                }
            }

            if (connectionMode == ConnectionMode.SERVER) {
                Text(
                    text = "サーバー接続先",
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .widthIn(max = 420.dp)
                )

                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .widthIn(max = 420.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (serverTarget == ServerTarget.EMULATOR) {
                        Button(
                            onClick = { onSelectServerTarget(ServerTarget.EMULATOR) },
                            enabled = !loading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("エミュレーター")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onSelectServerTarget(ServerTarget.EMULATOR) },
                            enabled = !loading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("エミュレーター")
                        }
                    }

                    if (serverTarget == ServerTarget.USB) {
                        Button(
                            onClick = { onSelectServerTarget(ServerTarget.USB) },
                            enabled = !loading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("USB実機")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onSelectServerTarget(ServerTarget.USB) },
                            enabled = !loading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("USB実機")
                        }
                    }
                }

                Text(
                    text = "エミュレーターは 10.0.2.2:3000、USB実機は localhost:3000 を使います",
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .widthIn(max = 420.dp)
                )
            } else {
                Text(
                    text = "ローカルのみモードはサーバー不要です。アプリ内モックAPIで動作します。",
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .widthIn(max = 420.dp)
                )

                Text(
                    text = "ユーザーIDとパスワードは同じです\n（1・1 ～ 33・33）",
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .widthIn(max = 420.dp)
                )
            }

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
                    .widthIn(max = 420.dp)
                    .wrapContentHeight(),
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
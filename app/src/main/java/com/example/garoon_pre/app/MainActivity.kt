package com.example.garoon_pre.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.garoon_pre.app.navigation.AppNav
import com.example.garoon_pre.core.designsystem.theme.Garoon_preTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Garoon_preTheme(
                dynamicColor = false
            ) {
                AppNav()
            }
        }
    }
}
package com.connectapp.controlpharmaapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.connectapp.presentation.MainScreen

@Composable
fun App() {
    MaterialTheme {
        MainScreen()
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}
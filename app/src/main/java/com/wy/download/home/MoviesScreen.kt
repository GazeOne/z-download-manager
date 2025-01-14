package com.wy.download.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MoviesScreen() {
    Scaffold (modifier = Modifier.fillMaxWidth()) { innerPadding ->
        Text(
            text = "Hello MoviesScreen!",
            modifier = Modifier.padding(innerPadding)
        )
    }
}
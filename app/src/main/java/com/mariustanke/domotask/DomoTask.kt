package com.mariustanke.domotask

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mariustanke.domotask.core.NavigationWrapper
import com.mariustanke.domotask.ui.theme.DomoTaskTheme

@Composable
fun DomoApp() {
    DomoTaskTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavigationWrapper()
        }
    }
}


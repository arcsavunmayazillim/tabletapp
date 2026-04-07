package com.takipsanplus.rfidtablet.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.takipsanplus.rfidtablet.presentation.common.AppLanguage

@Composable
fun ModulePlaceholderScreen(
    language: AppLanguage,
    titleProvider: (AppLanguage) -> String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = titleProvider(language), style = MaterialTheme.typography.headlineMedium)
    }
}

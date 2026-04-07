package com.takipsanplus.rfidtablet.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.takipsanplus.rfidtablet.R
import com.takipsanplus.rfidtablet.presentation.common.AppLanguage
import com.takipsanplus.rfidtablet.presentation.common.localizedString
import com.takipsanplus.rfidtablet.presentation.theme.PrimaryBlue
import com.takipsanplus.rfidtablet.presentation.theme.SurfaceSoft
import com.takipsanplus.rfidtablet.presentation.theme.TextMuted

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRememberMeChanged: (Boolean) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onLoginClick: () -> Unit,
    onLoginSuccessConsumed: () -> Unit,
    navigateToDeviceSelection: (String) -> Unit
) {
    LaunchedEffect(uiState.token) {
        uiState.token?.let {
            navigateToDeviceSelection(it)
            onLoginSuccessConsumed()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CenterBackgroundVisual()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ulogslogo),
                    contentDescription = "Ulogs Logo",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(58.dp)
                        .fillMaxWidth(0.38f),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.username,
                    onValueChange = onUsernameChanged,
                    label = { Text(localizedString(R.string.username, uiState.selectedLanguage)) },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.password,
                    onValueChange = onPasswordChanged,
                    label = { Text(localizedString(R.string.password, uiState.selectedLanguage)) },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Checkbox(
                        checked = uiState.rememberMe,
                        onCheckedChange = { onRememberMeChanged(it) }
                    )
                    Text(
                        text = localizedString(R.string.remember_me, uiState.selectedLanguage),
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onLoginClick,
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = localizedString(R.string.login_action, uiState.selectedLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    AppLanguage.entries.forEach { language ->
                        FilterChip(
                            selected = uiState.selectedLanguage == language,
                            onClick = { onLanguageChanged(language) },
                            label = { Text(languageFlag(language)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SurfaceSoft,
                                selectedLabelColor = PrimaryBlue
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = uiState.selectedLanguage == language,
                                borderColor = Color(0x552A6FCF),
                                selectedBorderColor = Color(0xFF0B3D91)
                            )
                        )
                    }
                }
            }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CenterBackgroundVisual() {
    Box(
        modifier = Modifier
            .size(640.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFB8DBFF), Color(0xFFE4F1FF), Color(0x00FFFFFF))
                )
            )
    )

    Box(
        modifier = Modifier
            .size(width = 700.dp, height = 250.dp)
            .clip(RoundedCornerShape(120.dp))
            .background(
                brush = Brush.linearGradient(
                    listOf(Color(0x10FFFFFF), Color(0x55A8D4FF), Color(0x10FFFFFF))
                )
            )
    )

    Box(
        modifier = Modifier
            .padding(top = 220.dp, start = 320.dp)
            .size(180.dp)
            .clip(CircleShape)
            .background(Color(0x44C9E3FF))
    )

    Box(
        modifier = Modifier
            .padding(bottom = 180.dp, end = 300.dp)
            .size(width = 260.dp, height = 120.dp)
            .clip(RoundedCornerShape(80.dp))
            .background(Color(0x338FC5FF))
    )
}

private fun languageFlag(language: AppLanguage): String {
    return when (language) {
        AppLanguage.ENGLISH -> "\uD83C\uDDEC\uD83C\uDDE7"
        AppLanguage.TURKISH -> "\uD83C\uDDF9\uD83C\uDDF7"
        AppLanguage.SPANISH -> "\uD83C\uDDEA\uD83C\uDDF8"
        AppLanguage.FRENCH -> "\uD83C\uDDEB\uD83C\uDDF7"
        AppLanguage.VIETNAMESE -> "\uD83C\uDDFB\uD83C\uDDF3"
    }
}

package com.takipsanplus.rfidtablet.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.takipsanplus.rfidtablet.R
import com.takipsanplus.rfidtablet.presentation.common.AppLanguage
import com.takipsanplus.rfidtablet.presentation.common.localizedString

private val LoginDark = Color(0xFF000842)
private val LoginBlue = Color(0xFF2B7CB0)
private val LoginHint = Color(0xFF999999)
private val LoginBg = Color(0xFFF3F4F9)

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

    var passwordVisible by remember { mutableStateOf(false) }
    val isWide = LocalConfiguration.current.smallestScreenWidthDp >= 600

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LoginBg)
    ) {
        if (isWide) {
            TabletLayout(
                uiState = uiState,
                passwordVisible = passwordVisible,
                onPasswordVisibleToggle = { passwordVisible = !passwordVisible },
                onUsernameChanged = onUsernameChanged,
                onPasswordChanged = onPasswordChanged,
                onRememberMeChanged = onRememberMeChanged,
                onLanguageChanged = onLanguageChanged,
                onLoginClick = onLoginClick
            )
        } else {
            PhoneLayout(
                uiState = uiState,
                passwordVisible = passwordVisible,
                onPasswordVisibleToggle = { passwordVisible = !passwordVisible },
                onUsernameChanged = onUsernameChanged,
                onPasswordChanged = onPasswordChanged,
                onRememberMeChanged = onRememberMeChanged,
                onLanguageChanged = onLanguageChanged,
                onLoginClick = onLoginClick
            )
        }
    }
}

@Composable
private fun TabletLayout(
    uiState: LoginUiState,
    passwordVisible: Boolean,
    onPasswordVisibleToggle: () -> Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRememberMeChanged: (Boolean) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onLoginClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.27f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.warehouse_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(0.85f)
                    .fillMaxWidth(0.52f)
                    .shadow(elevation = 8.dp, shape = RectangleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF318CC6), Color(0xFF0E334B)),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(0f, Float.POSITIVE_INFINITY)
                        )
                    )
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_pat),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .fillMaxWidth(0.75f)
                    .graphicsLayer {
                        rotationZ = -20f
                        scaleX = 1.35f
                        scaleY = 1.35f
                    },
                contentScale = ContentScale.Crop,
                alpha = 0.55f
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.84f)
                    .fillMaxHeight(0.85f)
                    .shadow(elevation = 4.dp, shape = RectangleShape)
                    .background(Color(0xFFF3F4F9)),
                contentAlignment = Alignment.Center
            ) {
                LoginForm(
                    uiState = uiState,
                    passwordVisible = passwordVisible,
                    onPasswordVisibleToggle = onPasswordVisibleToggle,
                    onUsernameChanged = onUsernameChanged,
                    onPasswordChanged = onPasswordChanged,
                    onRememberMeChanged = onRememberMeChanged,
                    onLanguageChanged = onLanguageChanged,
                    onLoginClick = onLoginClick,
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxWidth(0.65f)
                )
            }
        }
    }
}

@Composable
private fun PhoneLayout(
    uiState: LoginUiState,
    passwordVisible: Boolean,
    onPasswordVisibleToggle: () -> Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRememberMeChanged: (Boolean) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onLoginClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LoginBg)
    ) {
        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp
        val screenWidth = configuration.screenWidthDp.dp

        val logoTopPadding = remember(screenHeight) { (screenHeight * 0.15f).coerceIn(64.dp, 120.dp) }
        val logoHeight = remember(screenWidth) { (screenWidth * 0.22f).coerceIn(78.dp, 110.dp) }
        val logoToFormGap = remember(screenHeight) { (screenHeight * 0.02f).coerceIn(10.dp, 18.dp) }
        val horizontalPadding = remember(screenWidth) { (screenWidth * 0.055f).coerceIn(18.dp, 28.dp) }

        Image(
            painter = painterResource(id = R.drawable.bg_pat4),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 1f
        )

        Image(
            painter = painterResource(id = R.drawable.u_logs_logo),
            contentDescription = "U-LOGS",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = logoTopPadding)
                .height(logoHeight)
                .fillMaxWidth(0.92f),
            contentScale = ContentScale.Fit
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(
                    top = logoTopPadding + logoHeight + logoToFormGap,
                    start = horizontalPadding,
                    end = horizontalPadding,
                    bottom = 22.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            LoginForm(
                uiState = uiState,
                passwordVisible = passwordVisible,
                onPasswordVisibleToggle = onPasswordVisibleToggle,
                onUsernameChanged = onUsernameChanged,
                onPasswordChanged = onPasswordChanged,
                onRememberMeChanged = onRememberMeChanged,
                onLanguageChanged = onLanguageChanged,
                onLoginClick = onLoginClick,
                compact = true,
                showLogo = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LoginForm(
    uiState: LoginUiState,
    passwordVisible: Boolean,
    onPasswordVisibleToggle: () -> Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRememberMeChanged: (Boolean) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    showLogo: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showLogo) {
            Image(
                painter = painterResource(id = R.drawable.u_logs_logo),
                contentDescription = "U-LOGS",
                modifier = Modifier
                    .height(if (compact) 42.dp else 72.dp)
                    .fillMaxWidth(if (compact) 0.5f else 0.6f),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(if (compact) 10.dp else 36.dp))
        }

        // Username field
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.username,
            onValueChange = onUsernameChanged,
            label = { Text(localizedString(R.string.username, uiState.selectedLanguage)) },
            placeholder = { Text(localizedString(R.string.username_hint, uiState.selectedLanguage)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = LoginDark,
                    modifier = Modifier.size(18.dp)
                )
            },
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(Modifier.height(if (compact) 6.dp else 16.dp))

        // Password field
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.password,
            onValueChange = onPasswordChanged,
            label = { Text(localizedString(R.string.password, uiState.selectedLanguage)) },
            placeholder = { Text(localizedString(R.string.password_hint, uiState.selectedLanguage)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = LoginDark,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = onPasswordVisibleToggle) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = LoginHint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(Modifier.height(if (compact) 2.dp else 16.dp))

        // Remember me
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = uiState.rememberMe,
                onCheckedChange = onRememberMeChanged,
                colors = CheckboxDefaults.colors(
                    uncheckedColor = LoginDark,
                    checkedColor = LoginBlue,
                    checkmarkColor = Color.White
                )
            )
            Text(
                text = localizedString(R.string.remember_me, uiState.selectedLanguage),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Light,
                color = LoginDark
            )
        }

        Spacer(Modifier.height(if (compact) 6.dp else 24.dp))

        // Login button
        Button(
            onClick = onLoginClick,
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 44.dp else 56.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LoginBlue),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = localizedString(R.string.login_action, uiState.selectedLanguage),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(if (compact) 6.dp else 20.dp))

        // Language chips
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
                        selectedContainerColor = Color(0xFFDEEBF7),
                        selectedLabelColor = LoginBlue
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = uiState.selectedLanguage == language,
                        borderColor = Color(0x552A6FCF),
                        selectedBorderColor = LoginBlue
                    )
                )
            }
        }
    }
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

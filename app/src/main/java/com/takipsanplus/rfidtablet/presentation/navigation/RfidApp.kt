package com.takipsanplus.rfidtablet.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.takipsanplus.rfidtablet.presentation.common.UiMessage
import com.takipsanplus.rfidtablet.presentation.common.ServiceLocator
import com.takipsanplus.rfidtablet.presentation.common.localizedString
import com.takipsanplus.rfidtablet.presentation.device.DeviceSelectionEvent
import com.takipsanplus.rfidtablet.presentation.device.DeviceSelectionScreen
import com.takipsanplus.rfidtablet.presentation.device.DeviceSelectionViewModel
import com.takipsanplus.rfidtablet.presentation.home.HomeScreen
import com.takipsanplus.rfidtablet.presentation.login.LoginScreen
import com.takipsanplus.rfidtablet.presentation.login.LoginViewModel
import com.takipsanplus.rfidtablet.R
import com.takipsanplus.rfidtablet.data.network.BridgePlusConnectionController
import com.takipsanplus.rfidtablet.presentation.settings.SettingsScreen
import com.takipsanplus.rfidtablet.presentation.counting.CountingScreen
import com.takipsanplus.rfidtablet.presentation.counting.CountingViewModel
import com.takipsanplus.rfidtablet.presentation.shipment.ShipmentScreen
import com.takipsanplus.rfidtablet.presentation.shipment.ShipmentViewModel

@Composable
fun RfidApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.Factory(
            ServiceLocator.provideLoginUseCase(),
            ServiceLocator.provideUserPreferences(context)
        )
    )
    val uiState by loginViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Unit key: dil değişse bile collector yeniden başlamaz, mesaj kaybolmaz
    LaunchedEffect(Unit) {
        BridgePlusConnectionController.messages.collect { message ->
            val text = when (message) {
                is UiMessage.Resource -> localizedString(context, message.resId, uiState.selectedLanguage)
                is UiMessage.Text -> message.value
            }
            snackbarHostState.showSnackbar(message = text)
        }
    }

    LaunchedEffect(loginViewModel) {
        loginViewModel.messages.collect { message ->
            val text = when (message) {
                is UiMessage.Resource -> localizedString(context, message.resId, uiState.selectedLanguage)
                is UiMessage.Text -> message.value
            }
            snackbarHostState.showSnackbar(message = text)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0)
    ) { _ ->
        NavHost(navController = navController, startDestination = NavRoutes.LOGIN) {
            composable(NavRoutes.LOGIN) {
                LoginScreen(
                    uiState = uiState,
                    onUsernameChanged = loginViewModel::onUsernameChange,
                    onPasswordChanged = loginViewModel::onPasswordChange,
                    onRememberMeChanged = loginViewModel::onRememberMeChanged,
                    onLanguageChanged = loginViewModel::onLanguageChanged,
                    onLoginClick = loginViewModel::login,
                    onLoginSuccessConsumed = loginViewModel::consumeNavigation,
                    navigateToDeviceSelection = { token ->
                        navController.navigate("${NavRoutes.DEVICE_SELECTION}?token=${Uri.encode(token)}") {
                            popUpTo(NavRoutes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "${NavRoutes.DEVICE_SELECTION}?token={token}",
                arguments = listOf(navArgument("token") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                })
            ) {
                val token = it.arguments?.getString("token").orEmpty()
                val deviceViewModel: DeviceSelectionViewModel = viewModel(
                    factory = DeviceSelectionViewModel.Factory(
                        token = token,
                        userPreferences = ServiceLocator.provideUserPreferences(context),
                        getUserInfoUseCase = ServiceLocator.provideGetUserInfoUseCase(),
                        getDeviceListUseCase = ServiceLocator.provideGetDeviceListUseCase()
                    )
                )
                val deviceUiState by deviceViewModel.uiState.collectAsState()

                LaunchedEffect(deviceViewModel) {
                    deviceViewModel.messages.collect { message ->
                        val text = when (message) {
                            is UiMessage.Resource -> localizedString(context, message.resId, uiState.selectedLanguage)
                            is UiMessage.Text -> message.value
                        }
                        snackbarHostState.showSnackbar(message = text)
                    }
                }
                LaunchedEffect(deviceViewModel) {
                    deviceViewModel.events.collect { event ->
                        when (event) {
                            is DeviceSelectionEvent.NavigateHome -> {
                                navController.navigate(
                                    "${NavRoutes.HOME}?device=${Uri.encode(event.selectedDeviceName)}"
                                ) {
                                    // Lisans ekranı bir kez geçilir; Home'dan back tuşu ile geri açılmamalı
                                    popUpTo(NavRoutes.DEVICE_SELECTION) { inclusive = true }
                                }
                            }
                        }
                    }
                }

                DeviceSelectionScreen(
                    uiState = deviceUiState,
                    language = uiState.selectedLanguage,
                    onDeviceSelected = deviceViewModel::onDeviceSelected,
                    onContinueClick = deviceViewModel::onContinueClicked
                )
            }

            composable(
                route = "${NavRoutes.HOME}?device={device}",
                arguments = listOf(navArgument("device") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                })
            ) {
                val selectedDevice = it.arguments?.getString("device").orEmpty()
                HomeScreen(
                    language = uiState.selectedLanguage,
                    selectedDeviceName = selectedDevice,
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        ServiceLocator.provideUserPreferences(context).clearSession()
                        BridgePlusConnectionController.disconnect()
                        navController.navigate(NavRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onCountingClick = { navController.navigate(NavRoutes.COUNTING) },
                    onShipmentClick = { navController.navigate(NavRoutes.SHIPMENT) },
                    onSettingsClick = { navController.navigate(NavRoutes.SETTINGS) }
                )
            }

            composable(NavRoutes.COUNTING) {
                val userPrefs = ServiceLocator.provideUserPreferences(context)
                val countingViewModel: CountingViewModel = viewModel(
                    factory = CountingViewModel.Factory(userPrefs)
                )
                val countingUiState by countingViewModel.uiState.collectAsState()

                CountingScreen(
                    uiState = countingUiState,
                    language = uiState.selectedLanguage,
                    onBack = { navController.popBackStack() },
                    onStartStopClicked = countingViewModel::onStartStopClicked,
                    onClearClicked = countingViewModel::onClearClicked
                )
            }
            composable(NavRoutes.SHIPMENT) {
                val shipmentViewModel: ShipmentViewModel = viewModel(
                    factory = ShipmentViewModel.Factory(
                        ServiceLocator.provideUserPreferences(context),
                        ServiceLocator.provideConsignmentRepository(),
                        ServiceLocator.providePackagesRepository()
                    )
                )
                val shipmentState by shipmentViewModel.state.collectAsState()
                ShipmentScreen(
                    uiState = shipmentState,
                    language = uiState.selectedLanguage,
                    onBack = { navController.popBackStack() },
                    onSelectShipment = shipmentViewModel::selectShipment,
                    onToggleExpand = shipmentViewModel::togglePackageExpanded,
                    onTogglePackageSelect = shipmentViewModel::togglePackageSelection,
                    onClearSelection = shipmentViewModel::clearPackageSelection,
                    onNewShipment = shipmentViewModel::showNewShipmentDialog,
                    onShowCloseConsignment = shipmentViewModel::showCloseConsignmentDialog,
                    onShowDeletePackage = shipmentViewModel::showDeletePackageDialog,
                    onShowDeleteSelected = shipmentViewModel::showDeleteSelectedPackagesDialog,
                    onSelectAllVisiblePackages = { ids -> shipmentViewModel.selectAllPackages(ids) },
                    onShowMerge = shipmentViewModel::showMergeDialog,
                    onToggleScan = shipmentViewModel::toggleScanning,
                    onDismissShipmentError = shipmentViewModel::clearShipmentError,
                    onDismissDialog = shipmentViewModel::dismissDialog,
                    onRetryConsignments = shipmentViewModel::refreshConsignments,
                    onClearListLoadError = shipmentViewModel::clearListLoadError,
                    onUpdateNewShipmentDraftName = shipmentViewModel::updateNewShipmentDraftName,
                    onUpdateNewShipmentDraftExpectedCount = shipmentViewModel::updateNewShipmentDraftExpectedCount,
                    onUpdateNewShipmentDraftDeliveryDate = shipmentViewModel::updateNewShipmentDraftDeliveryDate,
                    onUpdateNewShipmentSelectedConsigneeId = shipmentViewModel::updateNewShipmentSelectedConsigneeId,
                    onConfirmNew = shipmentViewModel::confirmNewShipment,
                    onConfirmDelete = shipmentViewModel::confirmDeletePackage,
                    onConfirmMerge = shipmentViewModel::confirmMergePackages,
                    onConfirmCloseConsignment = shipmentViewModel::confirmCloseConsignment,
                    onShowEditShipment = shipmentViewModel::showEditShipmentDialog,
                    onUpdateEditShipmentDraftName = shipmentViewModel::updateEditShipmentDraftName,
                    onUpdateEditShipmentDraftExpectedCount = shipmentViewModel::updateEditShipmentDraftExpectedCount,
                    onUpdateEditShipmentDraftDeliveryDate = shipmentViewModel::updateEditShipmentDraftDeliveryDate,
                    onConfirmEditShipment = shipmentViewModel::confirmEditShipment,
                    onQrScannerFinished = shipmentViewModel::onQrScannerFinished,
                    onConfirmPendingQr = shipmentViewModel::confirmPendingQrAndStartEpc,
                    onDismissPendingQrRescan = shipmentViewModel::dismissPendingQrAndRescan,
                    onShowSizeTotalsBreakdown = shipmentViewModel::showSizeTotalsBreakdown,
                    onDismissSizeTotalsDialog = shipmentViewModel::dismissSizeTotalsDialog,
                    onDismissAddPackageWarning = shipmentViewModel::dismissAddPackageWarning,
                    onDismissFindPackageDialog = shipmentViewModel::dismissFindPackageDialog,
                    onFindPackageByTag = shipmentViewModel::startFindPackageLookup
                )
            }
            composable(NavRoutes.SETTINGS) {
                SettingsScreen(
                    language = uiState.selectedLanguage,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

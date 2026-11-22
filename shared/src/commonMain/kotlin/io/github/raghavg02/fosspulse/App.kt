package io.github.raghavg02.fosspulse

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.raghavg02.fosspulse.platform.BackHandler
import io.github.raghavg02.fosspulse.ui.addrepo.AddRepoSheet
import io.github.raghavg02.fosspulse.ui.addrepo.AddRepoViewModel
import io.github.raghavg02.fosspulse.ui.dashboard.DashboardScreen
import io.github.raghavg02.fosspulse.ui.dashboard.DashboardUiEvent
import io.github.raghavg02.fosspulse.ui.dashboard.DashboardViewModel
import io.github.raghavg02.fosspulse.ui.detail.ReleaseDetailScreen
import io.github.raghavg02.fosspulse.ui.scanner.InstalledAppsScannerScreen
import io.github.raghavg02.fosspulse.ui.scanner.InstalledAppsScannerViewModel
import io.github.raghavg02.fosspulse.ui.settings.SettingsScreen
import io.github.raghavg02.fosspulse.ui.theme.BrutalTheme
import io.github.raghavg02.fosspulse.ui.theme.ThemeManager
import io.github.raghavg02.fosspulse.ui.theme.ThemeMode
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    KoinContext {
        val themeMode by ThemeManager.themeMode.collectAsState()
        val isSystemDark = isSystemInDarkTheme()
        val isDark = when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemDark
        }

        BrutalTheme(darkTheme = isDark) {
            val dashboardViewModel = koinViewModel<DashboardViewModel>()
            val addRepoViewModel = koinViewModel<AddRepoViewModel>()
            val scannerViewModel = koinViewModel<InstalledAppsScannerViewModel>()
            val uiState by dashboardViewModel.uiState.collectAsState()

            // System Back Gesture handling: closes overlays/screens in priority order before exiting
            val isOverlayOpen = uiState.selectedRepoForDetail != null ||
                    uiState.isAddRepoSheetVisible ||
                    uiState.isSettingsVisible ||
                    uiState.isScannerVisible

            BackHandler(enabled = isOverlayOpen) {
                when {
                    uiState.selectedRepoForDetail != null -> dashboardViewModel.onEvent(DashboardUiEvent.DismissDetail)
                    uiState.isAddRepoSheetVisible -> dashboardViewModel.onEvent(DashboardUiEvent.DismissAddRepo)
                    uiState.isSettingsVisible -> dashboardViewModel.onEvent(DashboardUiEvent.DismissSettings)
                    uiState.isScannerVisible -> dashboardViewModel.onEvent(DashboardUiEvent.DismissScanner)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BrutalTheme.colors.canvas)
            ) {
                // Main Dashboard
                DashboardScreen(viewModel = dashboardViewModel)

                // Installed Apps Scanner Full Screen
                if (uiState.isScannerVisible) {
                    InstalledAppsScannerScreen(
                        viewModel = scannerViewModel,
                        onBackClick = { dashboardViewModel.onEvent(DashboardUiEvent.DismissScanner) },
                        onLinkApp = { app ->
                            addRepoViewModel.prefillFromApp(
                                displayName = app.appName,
                                packageName = app.packageName,
                                installedVersion = app.installedVersionName
                            )
                            dashboardViewModel.onEvent(DashboardUiEvent.DismissScanner)
                            dashboardViewModel.onEvent(DashboardUiEvent.ShowAddRepo)
                        }
                    )
                }

                // Settings Screen Full Screen
                if (uiState.isSettingsVisible) {
                    SettingsScreen(
                        onNavigateBack = { dashboardViewModel.onEvent(DashboardUiEvent.DismissSettings) }
                    )
                }

                // Add Repository Modal Overlay
                if (uiState.isAddRepoSheetVisible) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AddRepoSheet(
                            viewModel = addRepoViewModel,
                            onDismiss = { dashboardViewModel.onEvent(DashboardUiEvent.DismissAddRepo) }
                        )
                    }
                }

                // Full-Page Release Detail Screen
                if (uiState.selectedRepoForDetail != null) {
                    ReleaseDetailScreen(
                        item = uiState.selectedRepoForDetail!!,
                        onNavigateBack = { dashboardViewModel.onEvent(DashboardUiEvent.DismissDetail) }
                    )
                }
            }
        }
    }
}

package io.github.raghavg02.fosspulse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.raghavg02.fosspulse.ui.addrepo.AddRepoSheet
import io.github.raghavg02.fosspulse.ui.addrepo.AddRepoViewModel
import io.github.raghavg02.fosspulse.ui.dashboard.DashboardScreen
import io.github.raghavg02.fosspulse.ui.dashboard.DashboardUiEvent
import io.github.raghavg02.fosspulse.ui.dashboard.DashboardViewModel
import io.github.raghavg02.fosspulse.ui.detail.ReleaseDetailSheet
import io.github.raghavg02.fosspulse.ui.scanner.InstalledAppsScannerScreen
import io.github.raghavg02.fosspulse.ui.scanner.InstalledAppsScannerViewModel
import io.github.raghavg02.fosspulse.ui.theme.BrutalTheme
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    KoinContext {
        BrutalTheme {
            val dashboardViewModel = koinViewModel<DashboardViewModel>()
            val addRepoViewModel = koinViewModel<AddRepoViewModel>()
            val scannerViewModel = koinViewModel<InstalledAppsScannerViewModel>()
            val uiState by dashboardViewModel.uiState.collectAsState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BrutalTheme.colors.canvas)
            ) {
                // Main Dashboard
                DashboardScreen(viewModel = dashboardViewModel)

                // Installed Apps Scanner Overlay Screen
                if (uiState.isScannerVisible) {
                    InstalledAppsScannerScreen(
                        viewModel = scannerViewModel,
                        onBackClick = { dashboardViewModel.onEvent(DashboardUiEvent.DismissScanner) }
                    )
                }

                // Settings Screen Overlay Screen
                if (uiState.isSettingsVisible) {
                    io.github.raghavg02.fosspulse.ui.settings.SettingsScreen(
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

                // Release Detail Sheet Modal Overlay
                if (uiState.selectedRepoForDetail != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        ReleaseDetailSheet(
                            item = uiState.selectedRepoForDetail!!,
                            onDismiss = { dashboardViewModel.onEvent(DashboardUiEvent.DismissDetail) }
                        )
                    }
                }
            }
        }
    }
}

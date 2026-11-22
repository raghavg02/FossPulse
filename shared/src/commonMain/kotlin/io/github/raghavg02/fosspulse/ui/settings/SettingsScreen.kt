package io.github.raghavg02.fosspulse.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.raghavg02.fosspulse.domain.ai.LlmModelStatus
import io.github.raghavg02.fosspulse.ui.components.BrutalBadge
import io.github.raghavg02.fosspulse.ui.components.BrutalBadgeStyle
import io.github.raghavg02.fosspulse.ui.components.BrutalButton
import io.github.raghavg02.fosspulse.ui.components.BrutalButtonType
import io.github.raghavg02.fosspulse.ui.components.BrutalCard
import io.github.raghavg02.fosspulse.ui.components.BrutalTextField
import io.github.raghavg02.fosspulse.ui.components.BrutalTopBar
import io.github.raghavg02.fosspulse.ui.theme.BrutalTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            BrutalTopBar(
                title = "SETTINGS",
                actions = {
                    BrutalButton(
                        text = "BACK",
                        onClick = onNavigateBack,
                        type = BrutalButtonType.SECONDARY
                    )
                }
            )
        },
        containerColor = BrutalTheme.colors.canvas,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Card 1: GitHub Personal Access Token (PAT)
            BrutalCard(
                backgroundColor = BrutalTheme.colors.surface,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 16.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GITHUB PAT",
                            style = BrutalTheme.typography.H2,
                            color = BrutalTheme.colors.ink
                        )
                        val limit = state.rateLimit ?: 60
                        val badgeText = if (limit > 60) "$limit REQ/HR" else "60 REQ/HR (DEFAULT)"
                        BrutalBadge(
                            text = badgeText,
                            style = if (limit > 60) BrutalBadgeStyle.UP_TO_DATE else BrutalBadgeStyle.PLATFORM
                        )
                    }

                    Text(
                        text = "Optional. Add a GitHub PAT to increase API limits from 60 to 5,000 requests per hour. Stored in encrypted OS keychain.",
                        style = BrutalTheme.typography.BodySmall,
                        color = BrutalTheme.colors.muted
                    )

                    BrutalTextField(
                        value = state.githubPat,
                        onValueChange = { viewModel.onPatChanged(it) },
                        placeholder = "ghp_xxxxxxxxxxxxxxxxxxxx",
                        label = "PERSONAL ACCESS TOKEN",
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!state.patMessage.isNullOrBlank()) {
                        Text(
                            text = state.patMessage!!,
                            style = BrutalTheme.typography.MonoSmall,
                            color = BrutalTheme.colors.ink
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BrutalButton(
                            text = if (state.isTestingPat) "TESTING..." else "SAVE & TEST",
                            onClick = { viewModel.saveAndTestPat() },
                            type = BrutalButtonType.PRIMARY,
                            enabled = !state.isTestingPat,
                            modifier = Modifier.weight(1f)
                        )
                        if (state.githubPat.isNotBlank()) {
                            BrutalButton(
                                text = "CLEAR",
                                onClick = { viewModel.clearPat() },
                                type = BrutalButtonType.SECONDARY,
                                modifier = Modifier.weight(0.5f)
                            )
                        }
                    }
                }
            }

            // Card 2: Google Drive Cloud Sync
            BrutalCard(
                backgroundColor = BrutalTheme.colors.surface,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 16.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GOOGLE DRIVE SYNC",
                            style = BrutalTheme.typography.H2,
                            color = BrutalTheme.colors.ink
                        )
                        BrutalBadge(
                            text = if (state.isDriveConnected) "CONNECTED" else "DISCONNECTED",
                            style = if (state.isDriveConnected) BrutalBadgeStyle.UP_TO_DATE else BrutalBadgeStyle.PLATFORM
                        )
                    }

                    Text(
                        text = "Syncs your watchlist and regex patterns across your devices via Google Drive's hidden appDataFolder. Installed version records always stay local.",
                        style = BrutalTheme.typography.BodySmall,
                        color = BrutalTheme.colors.muted
                    )

                    if (!state.lastSyncedTime.isNullOrBlank()) {
                        Text(
                            text = "Last sync: ${state.lastSyncedTime}",
                            style = BrutalTheme.typography.MonoSmall,
                            color = BrutalTheme.colors.muted
                        )
                    }

                    if (!state.driveSyncMessage.isNullOrBlank()) {
                        BrutalCard(
                            backgroundColor = BrutalTheme.colors.canvas,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = 10.dp
                        ) {
                            val isError = "not configured" in state.driveSyncMessage!!.lowercase() ||
                                    "error" in state.driveSyncMessage!!.lowercase() ||
                                    "failed" in state.driveSyncMessage!!.lowercase()
                            Text(
                                text = state.driveSyncMessage!!,
                                style = BrutalTheme.typography.MonoSmall,
                                color = if (isError) BrutalTheme.colors.error else BrutalTheme.colors.ink
                            )
                        }
                    }

                    if (!state.isDriveConnected) {
                        BrutalTextField(
                            value = state.googleClientId,
                            onValueChange = { viewModel.onGoogleClientIdChanged(it) },
                            placeholder = "your-app.apps.googleusercontent.com",
                            label = "GOOGLE OAUTH CLIENT ID",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BrutalButton(
                                text = "SAVE CLIENT ID",
                                onClick = { viewModel.saveGoogleClientId() },
                                type = BrutalButtonType.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )
                            BrutalButton(
                                text = if (state.isDriveSyncing) "CONNECTING..." else "CONNECT GOOGLE DRIVE",
                                onClick = { viewModel.connectDrive() },
                                type = BrutalButtonType.PRIMARY,
                                enabled = !state.isDriveSyncing,
                                modifier = Modifier.weight(1.5f)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BrutalButton(
                                text = if (state.isDriveSyncing) "SYNCING..." else "SYNC NOW",
                                onClick = { viewModel.syncDriveNow() },
                                type = BrutalButtonType.PRIMARY,
                                enabled = !state.isDriveSyncing,
                                modifier = Modifier.weight(1f)
                            )
                            BrutalButton(
                                text = "DISCONNECT",
                                onClick = { viewModel.disconnectDrive() },
                                type = BrutalButtonType.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Card 3: Local AI Model Manager
            BrutalCard(
                backgroundColor = BrutalTheme.colors.surface,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 16.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LOCAL AI MODEL",
                            style = BrutalTheme.typography.H2,
                            color = BrutalTheme.colors.ink
                        )
                        val isDownloaded = state.modelStatus is LlmModelStatus.Downloaded
                        BrutalBadge(
                            text = if (isDownloaded) "READY" else "NOT INSTALLED",
                            style = if (isDownloaded) BrutalBadgeStyle.UP_TO_DATE else BrutalBadgeStyle.PLATFORM
                        )
                    }

                    Text(
                        text = "Qwen2.5 0.5B Instruct GGUF offline model (~${state.formattedModelSize}). Runs entirely on-device with zero telemetry or subscription costs.",
                        style = BrutalTheme.typography.BodySmall,
                        color = BrutalTheme.colors.muted
                    )

                    when (val status = state.modelStatus) {
                        is LlmModelStatus.NotDownloaded -> {
                            BrutalButton(
                                text = "DOWNLOAD MODEL (${state.formattedModelSize})",
                                onClick = { viewModel.downloadModel() },
                                type = BrutalButtonType.PRIMARY,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        is LlmModelStatus.Downloading -> {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                val percent = (status.progress * 100).toInt()
                                Text(
                                    text = "DOWNLOADING MODEL... $percent%",
                                    style = BrutalTheme.typography.MonoSmall,
                                    color = BrutalTheme.colors.ink
                                )
                                LinearProgressIndicator(
                                    progress = { status.progress },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = BrutalTheme.colors.primary,
                                    trackColor = BrutalTheme.colors.canvas
                                )
                            }
                        }
                        is LlmModelStatus.Downloaded -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Model Size on Disk: ${state.formattedModelSize}",
                                    style = BrutalTheme.typography.MonoSmall,
                                    color = BrutalTheme.colors.ink
                                )
                                BrutalButton(
                                    text = "DELETE MODEL",
                                    onClick = { viewModel.deleteModel() },
                                    type = BrutalButtonType.SECONDARY
                                )
                            }
                        }
                        is LlmModelStatus.Error -> {
                            Text(
                                text = "Download Error: ${status.message}",
                                style = BrutalTheme.typography.BodySmall,
                                color = BrutalTheme.colors.error
                            )
                            BrutalButton(
                                text = "RETRY DOWNLOAD",
                                onClick = { viewModel.downloadModel() },
                                type = BrutalButtonType.PRIMARY,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

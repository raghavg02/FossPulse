package io.github.raghavg02.fosspulse.ui.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.raghavg02.fosspulse.domain.scanner.ScannedApp
import io.github.raghavg02.fosspulse.ui.components.BrutalBadge
import io.github.raghavg02.fosspulse.ui.components.BrutalBadgeStyle
import io.github.raghavg02.fosspulse.ui.components.BrutalButton
import io.github.raghavg02.fosspulse.ui.components.BrutalButtonType
import io.github.raghavg02.fosspulse.ui.components.BrutalCard
import io.github.raghavg02.fosspulse.ui.components.BrutalTextField
import io.github.raghavg02.fosspulse.ui.components.BrutalTopBar
import io.github.raghavg02.fosspulse.ui.theme.BrutalTheme

@Composable
fun InstalledAppsScannerScreen(
    viewModel: InstalledAppsScannerViewModel,
    onBackClick: () -> Unit,
    onLinkApp: ((ScannedApp) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BrutalTheme.colors.canvas,
        topBar = {
            BrutalTopBar(
                title = "Scan Installed Apps",
                actions = {
                    BrutalButton(
                        text = "BACK",
                        onClick = onBackClick,
                        type = BrutalButtonType.SECONDARY
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Device ABI & Info Header Card
                BrutalCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = BrutalTheme.colors.surface,
                    contentPadding = 12.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "AUTO-DETECTED DEVICE ABI",
                                style = BrutalTheme.typography.Button,
                                color = BrutalTheme.colors.muted
                            )
                            Text(
                                text = state.deviceAbi.uppercase(),
                                style = BrutalTheme.typography.H2,
                                color = BrutalTheme.colors.ink
                            )
                        }
                        BrutalBadge(
                            text = "ANDROID",
                            style = BrutalBadgeStyle.PLATFORM
                        )
                    }
                }

                // Filter Tabs: MATCHED FOSS vs ALL INSTALLED
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BrutalButton(
                        text = "MATCHED FOSS (${state.matchedFossCount})",
                        onClick = { viewModel.onFilterTabChanged(ScannerFilterTab.MATCHED_FOSS) },
                        type = if (state.filterTab == ScannerFilterTab.MATCHED_FOSS) BrutalButtonType.PRIMARY else BrutalButtonType.SECONDARY,
                        modifier = Modifier.weight(1f)
                    )
                    BrutalButton(
                        text = "ALL INSTALLED (${state.totalInstalledCount})",
                        onClick = { viewModel.onFilterTabChanged(ScannerFilterTab.ALL_INSTALLED) },
                        type = if (state.filterTab == ScannerFilterTab.ALL_INSTALLED) BrutalButtonType.PRIMARY else BrutalButtonType.SECONDARY,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Search field
                BrutalTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = "Search applications...",
                    modifier = Modifier.fillMaxWidth()
                )

                // Select / Deselect actions (visible for matched FOSS or bulk selection)
                if (state.filterTab == ScannerFilterTab.MATCHED_FOSS) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${state.selectedCount} apps selected",
                            style = BrutalTheme.typography.MonoBadge,
                            color = BrutalTheme.colors.ink
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "[SELECT ALL]",
                                style = BrutalTheme.typography.Button,
                                color = BrutalTheme.colors.ink,
                                modifier = Modifier
                                    .clickable { viewModel.selectAll() }
                                    .padding(4.dp)
                            )
                            Text(
                                text = "[DESELECT ALL]",
                                style = BrutalTheme.typography.Button,
                                color = BrutalTheme.colors.muted,
                                modifier = Modifier
                                    .clickable { viewModel.deselectAll() }
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                // List of Scanned Apps
                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrutalTheme.colors.primary)
                    }
                } else if (state.filteredApps.isEmpty()) {
                    BrutalCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 24.dp
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (state.filterTab == ScannerFilterTab.MATCHED_FOSS) "NO MATCHED FOSS APPS FOUND" else "NO APPS FOUND",
                                style = BrutalTheme.typography.H2
                            )
                            Text(
                                text = if (state.filterTab == ScannerFilterTab.MATCHED_FOSS)
                                    "No pre-mapped open-source apps found among your installed packages. Switch to 'ALL INSTALLED' to link any app to GitHub manually."
                                else
                                    "No installed applications match your search query.",
                                style = BrutalTheme.typography.Body,
                                color = BrutalTheme.colors.muted
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.filteredApps, key = { it.packageName }) { app ->
                            ScannedAppCard(
                                app = app,
                                onToggle = {
                                    if (app.isKnownFoss) {
                                        viewModel.toggleSelection(app.packageName)
                                    } else {
                                        onLinkApp?.invoke(app)
                                    }
                                },
                                onLinkClick = { onLinkApp?.invoke(app) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }

            // Pinned Bottom Import Button
            if (state.selectedCount > 0 && state.filterTab == ScannerFilterTab.MATCHED_FOSS) {
                BrutalButton(
                    text = if (state.isImporting) "IMPORTING..." else "IMPORT ${state.selectedCount} APPS",
                    onClick = { viewModel.importSelectedApps(onSuccess = onBackClick) },
                    type = BrutalButtonType.PRIMARY,
                    enabled = !state.isImporting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ScannedAppCard(
    app: ScannedApp,
    onToggle: () -> Unit,
    onLinkClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(4.dp)
    val checkBg = when {
        app.isAlreadyTracked -> BrutalTheme.colors.canvas
        app.isSelected -> BrutalTheme.colors.primary
        else -> BrutalTheme.colors.surface
    }

    BrutalCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        backgroundColor = BrutalTheme.colors.surface,
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = app.appName,
                        style = BrutalTheme.typography.Button,
                        color = BrutalTheme.colors.ink
                    )
                    if (app.isAlreadyTracked) {
                        BrutalBadge(
                            text = "TRACKED",
                            style = BrutalBadgeStyle.UP_TO_DATE
                        )
                    } else if (!app.isKnownFoss) {
                        BrutalBadge(
                            text = "UNMAPPED",
                            style = BrutalBadgeStyle.NEUTRAL
                        )
                    }
                }

                Text(
                    text = app.packageName,
                    style = BrutalTheme.typography.MonoSmall,
                    color = BrutalTheme.colors.muted
                )

                if (app.isKnownFoss) {
                    Text(
                        text = "GitHub: ${app.githubRepoId}",
                        style = BrutalTheme.typography.MonoBadge,
                        color = BrutalTheme.colors.ink
                    )
                }

                Text(
                    text = "Installed: v${app.installedVersionName}",
                    style = BrutalTheme.typography.BodySmall,
                    color = BrutalTheme.colors.muted
                )
            }

            if (app.isKnownFoss) {
                // Checkbox for batch import
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(shape)
                        .background(checkBg, shape)
                        .border(2.dp, BrutalTheme.colors.border, shape)
                        .clickable(enabled = !app.isAlreadyTracked) { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    if (app.isSelected && !app.isAlreadyTracked) {
                        Text(
                            text = "✓",
                            style = BrutalTheme.typography.H3,
                            color = io.github.raghavg02.fosspulse.ui.theme.BrutalColors.Ink
                        )
                    } else if (app.isAlreadyTracked) {
                        Text(
                            text = "—",
                            style = BrutalTheme.typography.Button,
                            color = BrutalTheme.colors.muted
                        )
                    }
                }
            } else {
                // Button to link to owner/repo
                BrutalButton(
                    text = "LINK",
                    onClick = { onLinkClick?.invoke() },
                    type = BrutalButtonType.SECONDARY
                )
            }
        }
    }
}

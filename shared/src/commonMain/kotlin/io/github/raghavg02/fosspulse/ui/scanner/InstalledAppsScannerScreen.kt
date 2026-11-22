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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import io.github.raghavg02.fosspulse.ui.theme.brutalShadow

@Composable
fun InstalledAppsScannerScreen(
    viewModel: InstalledAppsScannerViewModel,
    onBackClick: () -> Unit,
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
                    Text(
                        text = "[BACK]",
                        style = BrutalTheme.typography.Button,
                        color = BrutalTheme.colors.ink,
                        modifier = Modifier
                            .clickable { onBackClick() }
                            .padding(8.dp)
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

                // Search field
                BrutalTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = "Search scanned applications...",
                    modifier = Modifier.fillMaxWidth()
                )

                // Select / Deselect actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MATCHED OPEN-SOURCE APPS (${state.filteredApps.size})",
                        style = BrutalTheme.typography.Button,
                        color = BrutalTheme.colors.ink
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "SELECT ALL",
                            style = BrutalTheme.typography.MonoSmall,
                            color = BrutalTheme.colors.ink,
                            modifier = Modifier.clickable { viewModel.selectAll() }
                        )
                        Text(
                            text = "|",
                            style = BrutalTheme.typography.MonoSmall,
                            color = BrutalTheme.colors.muted
                        )
                        Text(
                            text = "DESELECT ALL",
                            style = BrutalTheme.typography.MonoSmall,
                            color = BrutalTheme.colors.ink,
                            modifier = Modifier.clickable { viewModel.deselectAll() }
                        )
                    }
                }

                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        BrutalCard {
                            Text(
                                text = "SCANNING INSTALLED PACKAGES...",
                                style = BrutalTheme.typography.Button
                            )
                        }
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
                                text = "NO MATCHED FOSS APPS FOUND",
                                style = BrutalTheme.typography.H2
                            )
                            Text(
                                text = "Could not find known open-source apps among your installed packages. You can still track any repository manually from the dashboard.",
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
                                onToggle = { viewModel.toggleSelection(app.packageName) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }

            // Pinned Bottom Import Button
            if (state.selectedCount > 0) {
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
            .clickable(enabled = !app.isAlreadyTracked) { onToggle() },
        backgroundColor = if (app.isAlreadyTracked) BrutalTheme.colors.canvas else BrutalTheme.colors.surface,
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(checkBg, shape)
                    .border(2.dp, BrutalTheme.colors.border, shape)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        app.isAlreadyTracked -> "TRACKED"
                        app.isSelected -> "✓"
                        else -> " "
                    },
                    style = BrutalTheme.typography.MonoSmall,
                    color = BrutalTheme.colors.ink
                )
            }

            // Info column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = app.appName,
                        style = BrutalTheme.typography.H3,
                        color = BrutalTheme.colors.ink
                    )
                    BrutalBadge(
                        text = "v${app.installedVersionName}",
                        style = BrutalBadgeStyle.NEUTRAL
                    )
                }

                Text(
                    text = "GitHub: ${app.githubRepoId}",
                    style = BrutalTheme.typography.MonoSmall,
                    color = BrutalTheme.colors.ink
                )

                Text(
                    text = app.packageName,
                    style = BrutalTheme.typography.BodySmall,
                    color = BrutalTheme.colors.muted
                )
            }
        }
    }
}

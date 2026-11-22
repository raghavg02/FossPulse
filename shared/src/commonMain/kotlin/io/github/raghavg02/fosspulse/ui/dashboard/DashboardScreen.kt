package io.github.raghavg02.fosspulse.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.raghavg02.fosspulse.domain.model.TargetPlatform
import io.github.raghavg02.fosspulse.ui.components.BrutalBadge
import io.github.raghavg02.fosspulse.ui.components.BrutalBadgeStyle
import io.github.raghavg02.fosspulse.ui.components.BrutalButton
import io.github.raghavg02.fosspulse.ui.components.BrutalButtonType
import io.github.raghavg02.fosspulse.ui.components.BrutalCard
import io.github.raghavg02.fosspulse.ui.components.BrutalTopBar
import io.github.raghavg02.fosspulse.ui.theme.BrutalTheme
import io.github.raghavg02.fosspulse.ui.theme.brutalShadow

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    DashboardContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BrutalTheme.colors.canvas,
        topBar = {
            BrutalTopBar(
                title = "FossPulse",
                actions = {
                    if (uiState.rateLimitRemaining != null) {
                        BrutalBadge(
                            text = "API: ${uiState.rateLimitRemaining}/60",
                            style = BrutalBadgeStyle.NEUTRAL
                        )
                    }
                    BrutalButton(
                        text = if (uiState.isRefreshing) "..." else "SYNC",
                        onClick = { onEvent(DashboardUiEvent.RefreshAll) },
                        type = BrutalButtonType.SECONDARY,
                        enabled = !uiState.isRefreshing
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
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Platform Filter Bar (ALL, ANDROID, IOS, WINDOWS, MACOS)
                PlatformFilterRow(
                    selectedPlatform = uiState.selectedPlatformFilter,
                    onSelectPlatform = { onEvent(DashboardUiEvent.FilterByPlatform(it)) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Error message banner if any
                if (!uiState.errorMessage.isNullOrBlank()) {
                    BrutalCard(
                        backgroundColor = BrutalTheme.colors.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            style = BrutalTheme.typography.BodySmall,
                            color = BrutalTheme.colors.surface
                        )
                    }
                }

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        BrutalCard {
                            Text(
                                text = "LOADING WATCHLIST...",
                                style = BrutalTheme.typography.Button
                            )
                        }
                    }
                } else if (uiState.filteredRepos.isEmpty()) {
                    EmptyWatchlistState(
                        onAddClick = { onEvent(DashboardUiEvent.ShowAddRepo) }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(uiState.filteredRepos, key = { it.repo.id }) { item ->
                            RepoWatchlistCard(
                                item = item,
                                onClick = { onEvent(DashboardUiEvent.SelectRepo(item)) }
                            )
                        }
                        // Extra bottom spacing for FAB
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }

            // Fixed bottom action button
            BrutalButton(
                text = "+ ADD REPO",
                onClick = { onEvent(DashboardUiEvent.ShowAddRepo) },
                type = BrutalButtonType.PRIMARY,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            )
        }
    }
}

@Composable
fun PlatformFilterRow(
    selectedPlatform: TargetPlatform?,
    onSelectPlatform: (TargetPlatform?) -> Unit,
    modifier: Modifier = Modifier
) {
    val platforms = listOf(
        null to "ALL",
        TargetPlatform.ANDROID to "ANDROID",
        TargetPlatform.IOS to "IOS",
        TargetPlatform.WINDOWS to "WINDOWS",
        TargetPlatform.MACOS to "MACOS"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        platforms.forEach { (platform, label) ->
            val isSelected = selectedPlatform == platform
            val bg = if (isSelected) BrutalTheme.colors.primary else BrutalTheme.colors.surface
            val borderWidth = if (isSelected) 3.dp else 2.dp
            val shape = RoundedCornerShape(4.dp)

            Box(
                modifier = Modifier
                    .brutalShadow(
                        offsetX = if (isSelected) 3.dp else 2.dp,
                        offsetY = if (isSelected) 3.dp else 2.dp,
                        color = BrutalTheme.colors.border,
                        cornerRadius = 4.dp
                    )
                    .clip(shape)
                    .background(bg, shape)
                    .border(borderWidth, BrutalTheme.colors.border, shape)
                    .clickable { onSelectPlatform(platform) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    style = BrutalTheme.typography.Button,
                    color = BrutalTheme.colors.ink
                )
            }
        }
    }
}

@Composable
fun EmptyWatchlistState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BrutalCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 24.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "NO REPOS TRACKED YET",
                    style = BrutalTheme.typography.H1
                )
                Text(
                    text = "Start monitoring GitHub releases with exact OS binary matching and zero server dependencies.",
                    style = BrutalTheme.typography.Body,
                    color = BrutalTheme.colors.muted
                )
                Spacer(modifier = Modifier.height(8.dp))
                BrutalButton(
                    text = "ADD GITHUB REPOSITORY",
                    onClick = onAddClick,
                    type = BrutalButtonType.PRIMARY
                )
            }
        }
    }
}

@Composable
fun RepoWatchlistCard(
    item: TrackedRepoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repo = item.repo
    val release = item.release

    BrutalCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header: Display Name + Platform Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = repo.displayName,
                    style = BrutalTheme.typography.H2,
                    color = BrutalTheme.colors.ink,
                    modifier = Modifier.weight(1f)
                )
                BrutalBadge(
                    text = repo.targetPlatform.name,
                    style = BrutalBadgeStyle.PLATFORM
                )
            }

            // Subtitle: owner/repo
            Text(
                text = repo.id,
                style = BrutalTheme.typography.MonoBadge,
                color = BrutalTheme.colors.muted
            )

            // Version Comparison Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "INSTALLED: ${repo.installedVersion ?: "—"}",
                        style = BrutalTheme.typography.MonoSmall,
                        color = BrutalTheme.colors.ink
                    )
                    Text(
                        text = "→",
                        style = BrutalTheme.typography.MonoSmall,
                        color = BrutalTheme.colors.muted
                    )
                    Text(
                        text = "LATEST: ${repo.latestRemoteVersion ?: "—"}",
                        style = BrutalTheme.typography.MonoSmall,
                        color = BrutalTheme.colors.ink
                    )
                }

                // Status Badge
                when {
                    item.isUpdateAvailable -> {
                        BrutalBadge(
                            text = "UPDATE READY",
                            style = BrutalBadgeStyle.UPDATE_READY
                        )
                    }
                    item.isUpToDate -> {
                        BrutalBadge(
                            text = "UP TO DATE",
                            style = BrutalBadgeStyle.UP_TO_DATE
                        )
                    }
                    else -> {
                        BrutalBadge(
                            text = "CHECKING",
                            style = BrutalBadgeStyle.NEUTRAL
                        )
                    }
                }
            }

            // Binary Asset details if matched
            if (release?.matchedAsset != null) {
                val sizeMb = release.matchedAsset.sizeBytes / (1024.0 * 1024.0)
                val sizeStr = if (sizeMb > 0.0) " (${sizeMb.toInt()} MB)" else ""
                Text(
                    text = "BINARY: ${release.matchedAsset.name}$sizeStr",
                    style = BrutalTheme.typography.MonoSmall,
                    color = BrutalTheme.colors.muted
                )
            }
        }
    }
}

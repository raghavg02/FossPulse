package io.github.raghavg02.fosspulse.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.raghavg02.fosspulse.platform.downloadFile
import io.github.raghavg02.fosspulse.platform.openUrl
import io.github.raghavg02.fosspulse.ui.components.BrutalBadge
import io.github.raghavg02.fosspulse.ui.components.BrutalBadgeStyle
import io.github.raghavg02.fosspulse.ui.components.BrutalButton
import io.github.raghavg02.fosspulse.ui.components.BrutalButtonType
import io.github.raghavg02.fosspulse.ui.components.BrutalCard
import io.github.raghavg02.fosspulse.ui.dashboard.TrackedRepoItem
import io.github.raghavg02.fosspulse.ui.theme.BrutalTheme
import io.github.raghavg02.fosspulse.ui.theme.brutalShadow

@Composable
fun ReleaseDetailSheet(
    item: TrackedRepoItem,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repo = item.repo
    val release = item.release

    BrutalCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        backgroundColor = BrutalTheme.colors.surface,
        contentPadding = 20.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar with Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = repo.displayName,
                        style = BrutalTheme.typography.H1,
                        color = BrutalTheme.colors.ink
                    )
                    Text(
                        text = repo.id,
                        style = BrutalTheme.typography.MonoBadge,
                        color = BrutalTheme.colors.muted
                    )
                }
                Text(
                    text = "[CLOSE]",
                    style = BrutalTheme.typography.Button,
                    color = BrutalTheme.colors.muted,
                    modifier = Modifier.clickable { onDismiss() }
                )
            }

            // Version Diff Card
            BrutalCard(
                backgroundColor = BrutalTheme.colors.canvas,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 12.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "INSTALLED VERSION",
                            style = BrutalTheme.typography.MonoSmall,
                            color = BrutalTheme.colors.muted
                        )
                        Text(
                            text = repo.installedVersion ?: "Not Installed",
                            style = BrutalTheme.typography.H3,
                            color = BrutalTheme.colors.ink
                        )
                    }

                    Text(
                        text = "→",
                        style = BrutalTheme.typography.H2,
                        color = BrutalTheme.colors.ink
                    )

                    Column {
                        Text(
                            text = "LATEST RELEASE",
                            style = BrutalTheme.typography.MonoSmall,
                            color = BrutalTheme.colors.muted
                        )
                        Text(
                            text = release?.tagName ?: repo.latestRemoteVersion ?: "Unknown",
                            style = BrutalTheme.typography.H3,
                            color = BrutalTheme.colors.ink
                        )
                    }
                }
            }

            // Matched Binary Card
            if (release?.matchedAsset != null) {
                val asset = release.matchedAsset
                val sizeMb = asset.sizeBytes / (1024.0 * 1024.0)

                BrutalCard(
                    backgroundColor = BrutalTheme.colors.surface,
                    borderColor = BrutalTheme.colors.border,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 14.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MATCHED OS BINARY",
                                style = BrutalTheme.typography.Button,
                                color = BrutalTheme.colors.ink
                            )
                            BrutalBadge(
                                text = repo.targetPlatform.name,
                                style = BrutalBadgeStyle.PLATFORM
                            )
                        }

                        Text(
                            text = asset.name,
                            style = BrutalTheme.typography.MonoBadge,
                            color = BrutalTheme.colors.ink
                        )

                        Text(
                            text = "Size: ${if (sizeMb > 0) "%.1f MB".format(sizeMb) else "Unknown"}",
                            style = BrutalTheme.typography.BodySmall,
                            color = BrutalTheme.colors.muted
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Primary Download CTA
                        BrutalButton(
                            text = "DOWNLOAD ${asset.name}",
                            onClick = { downloadFile(asset.name, asset.downloadUrl) },
                            type = BrutalButtonType.PRIMARY,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // No binary matched yet
                BrutalCard(
                    backgroundColor = BrutalTheme.colors.canvas,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 12.dp
                ) {
                    Text(
                        text = "NO MATCHING OS BINARY ATTACHED YET FOR ${repo.targetPlatform.name}",
                        style = BrutalTheme.typography.BodySmall,
                        color = BrutalTheme.colors.muted
                    )
                }
            }

            // On-Device AI Changelog Summary Section Container
            BrutalCard(
                backgroundColor = BrutalTheme.colors.surface,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 14.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI CHANGELOG SUMMARY",
                            style = BrutalTheme.typography.Button,
                            color = BrutalTheme.colors.ink
                        )
                        BrutalBadge(
                            text = "ON-DEVICE LLM",
                            style = BrutalBadgeStyle.PLATFORM
                        )
                    }

                    Text(
                        text = "Generate concise 3–4 bullet points of features, bug fixes, and breaking changes locally on your hardware without cloud latency or subscription fees.",
                        style = BrutalTheme.typography.BodySmall,
                        color = BrutalTheme.colors.muted
                    )

                    BrutalButton(
                        text = "DOWNLOAD LOCAL AI MODEL (900 MB)",
                        onClick = { /* Phase 3: On-demand GGUF loader */ },
                        type = BrutalButtonType.SECONDARY,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Mandatory Pinned Bottom Button: READ FULL CHANGELOG ON GITHUB
            val changelogUrl = release?.htmlUrl ?: "https://github.com/${repo.id}/releases"
            BrutalButton(
                text = "READ FULL CHANGELOG ON GITHUB",
                onClick = { openUrl(changelogUrl) },
                type = BrutalButtonType.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

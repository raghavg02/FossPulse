package io.github.raghavg02.fosspulse.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.raghavg02.fosspulse.domain.ai.LlmModelStatus
import io.github.raghavg02.fosspulse.platform.downloadFile
import io.github.raghavg02.fosspulse.platform.openUrl
import io.github.raghavg02.fosspulse.ui.components.BrutalBadge
import io.github.raghavg02.fosspulse.ui.components.BrutalBadgeStyle
import io.github.raghavg02.fosspulse.ui.components.BrutalButton
import io.github.raghavg02.fosspulse.ui.components.BrutalButtonType
import io.github.raghavg02.fosspulse.ui.components.BrutalCard
import io.github.raghavg02.fosspulse.ui.dashboard.TrackedRepoItem
import io.github.raghavg02.fosspulse.ui.theme.BrutalTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ReleaseDetailSheet(
    item: TrackedRepoItem,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReleaseDetailViewModel = koinViewModel()
) {
    val repo = item.repo
    val release = item.release
    val tagName = release?.tagName ?: repo.latestRemoteVersion ?: "latest"
    val rawChangelog = release?.rawChangelogMd ?: "Release notes for ${repo.displayName} $tagName"

    val aiState by viewModel.aiState.collectAsState()

    LaunchedEffect(repo.id, tagName) {
        viewModel.loadSummaryFor(repo.id, tagName)
    }

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
                            text = tagName,
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

                        val formattedBinarySize = kotlin.math.round(sizeMb * 10) / 10.0
                        Text(
                            text = "Size: ${if (sizeMb > 0) "$formattedBinarySize MB" else "Unknown"}",
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

                    if (aiState.summaryBullets != null) {
                        // Display generated bullet points with clear styling
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            aiState.summaryBullets!!.lines().forEach { line ->
                                if (line.isNotBlank()) {
                                    Text(
                                        text = line,
                                        style = BrutalTheme.typography.BodySmall,
                                        color = BrutalTheme.colors.ink
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Generate concise 3–4 bullet points of features, bug fixes, and breaking changes locally on your hardware without cloud latency or subscription fees.",
                            style = BrutalTheme.typography.BodySmall,
                            color = BrutalTheme.colors.muted
                        )

                        when (val status = aiState.modelStatus) {
                            is LlmModelStatus.NotDownloaded -> {
                                val sizeText = if (aiState.formattedModelSize.isNotBlank()) " (${aiState.formattedModelSize})" else ""
                                BrutalButton(
                                    text = "DOWNLOAD LOCAL AI MODEL$sizeText",
                                    onClick = { viewModel.downloadModel() },
                                    type = BrutalButtonType.SECONDARY,
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
                                BrutalButton(
                                    text = if (aiState.isGenerating) "GENERATING SUMMARY..." else "SUMMARIZE WITH LOCAL AI (${aiState.formattedModelSize})",
                                    onClick = { viewModel.generateSummary(repo.id, tagName, rawChangelog) },
                                    type = BrutalButtonType.SECONDARY,
                                    enabled = !aiState.isGenerating,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            is LlmModelStatus.Error -> {
                                Text(
                                    text = "Error: ${status.message}",
                                    style = BrutalTheme.typography.BodySmall,
                                    color = BrutalTheme.colors.error
                                )
                                BrutalButton(
                                    text = "RETRY MODEL DOWNLOAD",
                                    onClick = { viewModel.downloadModel() },
                                    type = BrutalButtonType.SECONDARY,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
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

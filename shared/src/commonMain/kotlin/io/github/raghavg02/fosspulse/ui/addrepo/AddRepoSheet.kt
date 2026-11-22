package io.github.raghavg02.fosspulse.ui.addrepo

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.raghavg02.fosspulse.domain.model.TargetPlatform
import io.github.raghavg02.fosspulse.ui.components.BrutalButton
import io.github.raghavg02.fosspulse.ui.components.BrutalButtonType
import io.github.raghavg02.fosspulse.ui.components.BrutalCard
import io.github.raghavg02.fosspulse.ui.components.BrutalTextField
import io.github.raghavg02.fosspulse.ui.theme.BrutalTheme
import io.github.raghavg02.fosspulse.ui.theme.brutalShadow

@Composable
fun AddRepoSheet(
    viewModel: AddRepoViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRACK REPOSITORY",
                    style = BrutalTheme.typography.H2,
                    color = BrutalTheme.colors.ink
                )
                Text(
                    text = "[ESC/CLOSE]",
                    style = BrutalTheme.typography.MonoSmall,
                    color = BrutalTheme.colors.muted,
                    modifier = Modifier.clickable { onDismiss() }
                )
            }

            // GitHub URL / repo input
            BrutalTextField(
                value = state.repoInput,
                onValueChange = viewModel::onRepoInputChanged,
                label = "GitHub Repository",
                placeholder = "owner/repo or github.com/owner/repo",
                isMonospace = true,
                errorMessage = state.errorMessage,
                modifier = Modifier.fillMaxWidth()
            )

            // Display Name
            BrutalTextField(
                value = state.displayName,
                onValueChange = viewModel::onDisplayNameChanged,
                label = "Display Name",
                placeholder = "App Name",
                modifier = Modifier.fillMaxWidth()
            )

            // Target OS Selector
            Text(
                text = "TARGET PLATFORM",
                style = BrutalTheme.typography.Button,
                color = BrutalTheme.colors.ink
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TargetPlatform.entries.forEach { platform ->
                    val isSelected = state.targetPlatform == platform
                    val bg = if (isSelected) BrutalTheme.colors.primary else BrutalTheme.colors.surface
                    val shape = RoundedCornerShape(4.dp)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .brutalShadow(
                                offsetX = if (isSelected) 3.dp else 2.dp,
                                offsetY = if (isSelected) 3.dp else 2.dp,
                                color = BrutalTheme.colors.border,
                                cornerRadius = 4.dp
                            )
                            .clip(shape)
                            .background(bg, shape)
                            .border(if (isSelected) 3.dp else 2.dp, BrutalTheme.colors.border, shape)
                            .clickable { viewModel.onPlatformChanged(platform) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = platform.name,
                            style = BrutalTheme.typography.MonoSmall,
                            color = BrutalTheme.colors.ink
                        )
                    }
                }
            }

            // Asset Regex Matcher input
            BrutalTextField(
                value = state.assetRegex,
                onValueChange = viewModel::onRegexChanged,
                label = "Binary Asset Regex Filter",
                placeholder = ".*(arm64|universal).*\\.apk$",
                isMonospace = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Pre-release toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onIncludePrereleasesChanged(!state.includePrereleases) }
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INCLUDE PRE-RELEASES",
                    style = BrutalTheme.typography.Button,
                    color = BrutalTheme.colors.ink
                )
                val checkBg = if (state.includePrereleases) BrutalTheme.colors.primary else BrutalTheme.colors.surface
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(checkBg)
                        .border(2.dp, BrutalTheme.colors.border, RoundedCornerShape(2.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (state.includePrereleases) "YES" else "NO",
                        style = BrutalTheme.typography.MonoSmall,
                        color = BrutalTheme.colors.ink
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BrutalButton(
                    text = "CANCEL",
                    onClick = onDismiss,
                    type = BrutalButtonType.SECONDARY,
                    modifier = Modifier.weight(1f)
                )
                BrutalButton(
                    text = if (state.isSaving) "SAVING..." else "TRACK REPO",
                    onClick = {
                        viewModel.saveRepository(onSuccess = {
                            viewModel.resetState()
                            onDismiss()
                        })
                    },
                    type = BrutalButtonType.PRIMARY,
                    enabled = !state.isSaving && state.repoInput.isNotBlank(),
                    modifier = Modifier.weight(2f)
                )
            }
        }
    }
}

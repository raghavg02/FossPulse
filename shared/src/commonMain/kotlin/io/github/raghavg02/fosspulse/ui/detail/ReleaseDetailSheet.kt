package io.github.raghavg02.fosspulse.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.raghavg02.fosspulse.ui.dashboard.TrackedRepoItem
import org.koin.compose.viewmodel.koinViewModel

/**
 * Retained for backward-compatibility; delegates to full-page [ReleaseDetailScreen].
 */
@Composable
fun ReleaseDetailSheet(
    item: TrackedRepoItem,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReleaseDetailViewModel = koinViewModel()
) {
    ReleaseDetailScreen(
        item = item,
        onNavigateBack = onDismiss,
        modifier = modifier,
        viewModel = viewModel
    )
}

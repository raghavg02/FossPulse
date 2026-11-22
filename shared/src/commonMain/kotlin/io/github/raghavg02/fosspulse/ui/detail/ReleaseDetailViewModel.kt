package io.github.raghavg02.fosspulse.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.raghavg02.fosspulse.domain.ai.LlmModelManager
import io.github.raghavg02.fosspulse.domain.ai.LlmModelStatus
import io.github.raghavg02.fosspulse.domain.repository.ChangelogAiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ReleaseDetailAiState(
    val summaryBullets: String? = null,
    val isGenerating: Boolean = false,
    val modelStatus: LlmModelStatus = LlmModelStatus.NotDownloaded,
    val formattedModelSize: String = "",
    val error: String? = null
)

class ReleaseDetailViewModel(
    private val changelogAiRepository: ChangelogAiRepository,
    private val llmModelManager: LlmModelManager
) : ViewModel() {

    private val _aiState = MutableStateFlow(ReleaseDetailAiState())
    val aiState: StateFlow<ReleaseDetailAiState> = _aiState.asStateFlow()

    init {
        viewModelScope.launch {
            llmModelManager.modelStatus.collect { status ->
                _aiState.value = _aiState.value.copy(
                    modelStatus = status,
                    formattedModelSize = llmModelManager.getFormattedModelSize()
                )
            }
        }
    }

    fun loadSummaryFor(repoId: String, tagName: String) {
        viewModelScope.launch {
            llmModelManager.refreshStatus()
            val existing = changelogAiRepository.getSummary(repoId, tagName)
            _aiState.value = _aiState.value.copy(
                summaryBullets = existing?.summaryBullets,
                formattedModelSize = llmModelManager.getFormattedModelSize()
            )
        }
    }

    fun downloadModel() {
        viewModelScope.launch {
            val result = llmModelManager.downloadModel()
            if (result.isFailure) {
                _aiState.value = _aiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Model download failed"
                )
            }
        }
    }

    fun generateSummary(repoId: String, tagName: String, rawChangelog: String) {
        viewModelScope.launch {
            _aiState.value = _aiState.value.copy(isGenerating = true, error = null)
            val result = changelogAiRepository.generateAndCacheSummary(repoId, tagName, rawChangelog)
            result.onSuccess { entity ->
                _aiState.value = _aiState.value.copy(
                    summaryBullets = entity.summaryBullets,
                    isGenerating = false
                )
            }.onFailure { err ->
                _aiState.value = _aiState.value.copy(
                    isGenerating = false,
                    error = err.message ?: "Failed to generate summary"
                )
            }
        }
    }
}

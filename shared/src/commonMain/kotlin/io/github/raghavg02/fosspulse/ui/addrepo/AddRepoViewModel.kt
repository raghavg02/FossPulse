package io.github.raghavg02.fosspulse.ui.addrepo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.raghavg02.fosspulse.data.remote.GitHubApiService
import io.github.raghavg02.fosspulse.domain.engine.RegexAssetMatcher
import io.github.raghavg02.fosspulse.domain.model.NetworkResult
import io.github.raghavg02.fosspulse.domain.model.TargetPlatform
import io.github.raghavg02.fosspulse.domain.model.TrackedRepo
import io.github.raghavg02.fosspulse.domain.repository.RepoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddRepoUiState(
    val repoInput: String = "",
    val displayName: String = "",
    val targetPlatform: TargetPlatform = TargetPlatform.ANDROID,
    val assetRegex: String = defaultRegexForPlatform(TargetPlatform.ANDROID),
    val includePrereleases: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
) {
    companion object {
        fun defaultRegexForPlatform(platform: TargetPlatform): String = when (platform) {
            TargetPlatform.ANDROID -> ".*(arm64-v8a|universal).*\\.apk$"
            TargetPlatform.WINDOWS -> ".*(setup|x64).*\\.(exe|msi)$"
            TargetPlatform.MACOS -> ".*(universal|x64|arm64).*\\.dmg$"
            TargetPlatform.IOS -> ".*\\.ipa$"
        }
    }
}

class AddRepoViewModel(
    private val repoRepository: RepoRepository,
    private val gitHubApiService: GitHubApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddRepoUiState())
    val uiState: StateFlow<AddRepoUiState> = _uiState.asStateFlow()

    fun onRepoInputChanged(input: String) {
        val cleaned = cleanRepoInput(input)
        val defaultName = if (cleaned.contains('/')) {
            cleaned.substringAfter('/').replace('-', ' ').replace('_', ' ')
                .split(' ')
                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        } else {
            _uiState.value.displayName
        }

        _uiState.update {
            it.copy(
                repoInput = input,
                displayName = if (it.displayName.isBlank() || it.displayName == defaultName) defaultName else it.displayName,
                errorMessage = null
            )
        }
    }

    fun onDisplayNameChanged(name: String) {
        _uiState.update { it.copy(displayName = name) }
    }

    fun onPlatformChanged(platform: TargetPlatform) {
        _uiState.update {
            it.copy(
                targetPlatform = platform,
                assetRegex = AddRepoUiState.defaultRegexForPlatform(platform)
            )
        }
    }

    fun onRegexChanged(regex: String) {
        _uiState.update { it.copy(assetRegex = regex, errorMessage = null) }
    }

    fun onIncludePrereleasesChanged(include: Boolean) {
        _uiState.update { it.copy(includePrereleases = include) }
    }

    fun resetState() {
        _uiState.value = AddRepoUiState()
    }

    fun saveRepository(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val canonical = cleanRepoInput(currentState.repoInput)

        // 1. Validate owner/repo format: ^[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+$
        val repoRegex = Regex("^[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+$")
        if (!repoRegex.matches(canonical)) {
            _uiState.update {
                it.copy(errorMessage = "Invalid repository format. Use 'owner/repo' or a GitHub URL.")
            }
            return
        }

        // 2. Validate Regex
        val regexValidation = RegexAssetMatcher.validatePattern(currentState.assetRegex)
        if (regexValidation.isFailure) {
            _uiState.update {
                it.copy(errorMessage = regexValidation.exceptionOrNull()?.message ?: "Invalid regex pattern")
            }
            return
        }

        val parts = canonical.split('/')
        val owner = parts[0]
        val repoName = parts[1]
        val displayName = if (currentState.displayName.isNotBlank()) currentState.displayName else repoName

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val trackedRepo = TrackedRepo(
                id = canonical.lowercase(),
                owner = owner,
                repoName = repoName,
                displayName = displayName,
                targetPlatform = currentState.targetPlatform,
                assetRegex = currentState.assetRegex,
                includePrereleases = currentState.includePrereleases
            )

            // Save to local Room database
            repoRepository.saveRepo(trackedRepo)

            // Trigger immediate initial fetch
            when (val result = gitHubApiService.getLatestRelease(
                owner = owner,
                repo = repoName,
                assetRegex = currentState.assetRegex,
                includePrereleases = currentState.includePrereleases
            )) {
                is NetworkResult.Success -> {
                    repoRepository.updateLatestReleaseCheck(
                        repoId = trackedRepo.id,
                        latestTag = result.data.tagName,
                        etag = result.etag,
                        release = result.data
                    )
                }
                else -> {
                    // Saved repo remains; next sync will retry
                }
            }

            _uiState.update { it.copy(isSaving = false, isSuccess = true) }
            onSuccess()
        }
    }

    private fun cleanRepoInput(raw: String): String {
        return raw.trim()
            .removePrefix("https://github.com/")
            .removePrefix("http://github.com/")
            .removePrefix("github.com/")
            .removeSuffix(".git")
            .removeSuffix("/")
    }
}

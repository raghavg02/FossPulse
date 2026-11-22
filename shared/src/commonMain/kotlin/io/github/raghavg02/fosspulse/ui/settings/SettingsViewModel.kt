package io.github.raghavg02.fosspulse.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.raghavg02.fosspulse.data.remote.GitHubApiService
import io.github.raghavg02.fosspulse.domain.ai.LlmModelManager
import io.github.raghavg02.fosspulse.domain.ai.LlmModelStatus
import io.github.raghavg02.fosspulse.domain.model.NetworkResult
import io.github.raghavg02.fosspulse.domain.usecase.DriveSyncUseCase
import io.github.raghavg02.fosspulse.platform.auth.GoogleAuthProvider
import io.github.raghavg02.fosspulse.platform.storage.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val githubPat: String = "",
    val rateLimit: Int? = null,
    val isTestingPat: Boolean = false,
    val patMessage: String? = null,
    val isDriveConnected: Boolean = false,
    val isDriveSyncing: Boolean = false,
    val driveSyncMessage: String? = null,
    val lastSyncedTime: String? = null,
    val modelStatus: LlmModelStatus = LlmModelStatus.NotDownloaded,
    val formattedModelSize: String = ""
)

class SettingsViewModel(
    private val secureStorage: SecureStorage,
    private val gitHubApiService: GitHubApiService,
    private val googleAuthProvider: GoogleAuthProvider,
    private val driveSyncUseCase: DriveSyncUseCase,
    private val llmModelManager: LlmModelManager
) : ViewModel() {

    companion object {
        const val GITHUB_PAT_KEY = "github_personal_access_token"
        const val LAST_SYNC_KEY = "google_drive_last_synced_at"
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadInitialState()
        observeModelStatus()
    }

    private fun loadInitialState() {
        val savedPat = secureStorage.get(GITHUB_PAT_KEY) ?: ""
        val token = googleAuthProvider.getSavedAccessToken()
        val lastSync = secureStorage.get(LAST_SYNC_KEY)

        _uiState.value = _uiState.value.copy(
            githubPat = savedPat,
            isDriveConnected = !token.isNullOrBlank(),
            lastSyncedTime = lastSync,
            formattedModelSize = llmModelManager.getFormattedModelSize()
        )

        if (savedPat.isNotBlank()) {
            testPat(savedPat)
        } else {
            testPat(null)
        }
    }

    private fun observeModelStatus() {
        viewModelScope.launch {
            llmModelManager.modelStatus.collect { status ->
                _uiState.value = _uiState.value.copy(
                    modelStatus = status,
                    formattedModelSize = llmModelManager.getFormattedModelSize()
                )
            }
        }
    }

    fun onPatChanged(pat: String) {
        _uiState.value = _uiState.value.copy(githubPat = pat, patMessage = null)
    }

    fun saveAndTestPat() {
        val pat = _uiState.value.githubPat.trim()
        if (pat.isNotBlank()) {
            secureStorage.save(GITHUB_PAT_KEY, pat)
        } else {
            secureStorage.remove(GITHUB_PAT_KEY)
        }
        testPat(if (pat.isNotBlank()) pat else null)
    }

    fun clearPat() {
        secureStorage.remove(GITHUB_PAT_KEY)
        _uiState.value = _uiState.value.copy(githubPat = "", patMessage = "Token cleared")
        testPat(null)
    }

    private fun testPat(pat: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingPat = true)
            when (val result = gitHubApiService.getRateLimit(pat)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isTestingPat = false,
                        rateLimit = result.data,
                        patMessage = if (pat != null) "Valid PAT: ${result.data} requests/hr" else "Default rate limit: ${result.data} requests/hr"
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isTestingPat = false,
                        rateLimit = 60,
                        patMessage = "Error: ${result.message}"
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isTestingPat = false)
                }
            }
        }
    }

    fun connectDrive() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDriveSyncing = true, driveSyncMessage = null)
            val authResult = googleAuthProvider.signIn()
            authResult.onSuccess { res ->
                _uiState.value = _uiState.value.copy(
                    isDriveConnected = true,
                    isDriveSyncing = false,
                    driveSyncMessage = "Connected to Google Drive"
                )
                syncDriveNow()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isDriveSyncing = false,
                    driveSyncMessage = err.message ?: "Authentication failed"
                )
            }
        }
    }

    fun disconnectDrive() {
        viewModelScope.launch {
            googleAuthProvider.signOut()
            secureStorage.remove(LAST_SYNC_KEY)
            _uiState.value = _uiState.value.copy(
                isDriveConnected = false,
                driveSyncMessage = "Disconnected from Google Drive",
                lastSyncedTime = null
            )
        }
    }

    fun syncDriveNow() {
        val token = googleAuthProvider.getSavedAccessToken()
        if (token.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(driveSyncMessage = "Google Drive is not connected")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDriveSyncing = true, driveSyncMessage = "Syncing with Google Drive...")
            val result = driveSyncUseCase.execute(token)
            result.onSuccess { payload ->
                val timeStr = "${payload.repos.size} repos synced at ${payload.syncedAt}"
                secureStorage.save(LAST_SYNC_KEY, timeStr)
                _uiState.value = _uiState.value.copy(
                    isDriveSyncing = false,
                    driveSyncMessage = "Sync successful (${payload.repos.size} repos synced)",
                    lastSyncedTime = timeStr
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isDriveSyncing = false,
                    driveSyncMessage = "Sync failed: ${err.message}"
                )
            }
        }
    }

    fun downloadModel() {
        viewModelScope.launch {
            llmModelManager.downloadModel()
        }
    }

    fun deleteModel() {
        llmModelManager.deleteModel()
    }
}

package io.github.raghavg02.fosspulse.domain.scanner

data class ScannedApp(
    val packageName: String,
    val appName: String,
    val installedVersionName: String,
    val githubRepoId: String,
    val owner: String,
    val repoName: String,
    val suggestedRegex: String,
    val isAlreadyTracked: Boolean = false,
    val isSelected: Boolean = !isAlreadyTracked
)

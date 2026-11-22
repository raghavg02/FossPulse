package io.github.raghavg02.fosspulse.platform.scanner

import android.content.pm.PackageManager
import android.os.Build
import io.github.raghavg02.fosspulse.data.local.appContext
import io.github.raghavg02.fosspulse.domain.scanner.FossPackageCatalog
import io.github.raghavg02.fosspulse.domain.scanner.ScannedApp

actual fun detectDeviceAbi(): String {
    return Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
}

actual fun scanInstalledApps(): List<ScannedApp> {
    val context = appContext ?: return emptyList()
    val pm = context.packageManager
    val abi = detectDeviceAbi()
    val suggestedRegex = FossPackageCatalog.defaultRegexForAbi(abi)

    return try {
        val installed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(0)
        }

        installed.mapNotNull { packageInfo ->
            val pkgName = packageInfo.packageName ?: return@mapNotNull null
            val mapping = FossPackageCatalog.findMapping(pkgName) ?: return@mapNotNull null

            val appLabel = try {
                packageInfo.applicationInfo?.loadLabel(pm)?.toString() ?: mapping.displayName
            } catch (_: Exception) {
                mapping.displayName
            }
            val versionName = packageInfo.versionName ?: "Unknown"

            ScannedApp(
                packageName = pkgName,
                appName = appLabel,
                installedVersionName = versionName,
                githubRepoId = mapping.repoId,
                owner = mapping.owner,
                repoName = mapping.repoName,
                suggestedRegex = suggestedRegex,
                isAlreadyTracked = false,
                isSelected = true
            )
        }.sortedBy { it.appName.lowercase() }
    } catch (_: Exception) {
        emptyList()
    }
}

package io.github.raghavg02.fosspulse.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import io.github.raghavg02.fosspulse.domain.engine.SemVerComparator
import io.github.raghavg02.fosspulse.domain.repository.RepoRepository
import io.github.raghavg02.fosspulse.platform.notifier.PlatformNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PackageReplacedReceiver : BroadcastReceiver(), KoinComponent {

    private val repoRepository: RepoRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_PACKAGE_REPLACED && action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }

        val packageName = intent.data?.schemeSpecificPart ?: return

        val installedVersionName = try {
            val pm = context.packageManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0)).versionName
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0).versionName
            }
        } catch (_: Exception) {
            null
        } ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val activeRepos = repoRepository.getAllActiveRepos()
                val matchedRepo = activeRepos.firstOrNull { it.androidPackageName == packageName }

                if (matchedRepo != null) {
                    // Update installed version
                    repoRepository.updateInstalledVersion(matchedRepo.id, installedVersionName)

                    val remote = matchedRepo.latestRemoteVersion
                    // If newly installed version now matches or exceeds latest release:
                    if (remote != null && SemVerComparator.compare(installedVersionName, remote) >= 0) {
                        // Only trigger "What's new" alert if we haven't already notified for this release
                        if (matchedRepo.lastNotifiedTag != remote) {
                            PlatformNotifier().notifyAlreadyUpdated(
                                appName = matchedRepo.displayName,
                                latestTag = remote,
                                repoId = matchedRepo.id
                            )
                            repoRepository.updateLastNotifiedTag(matchedRepo.id, remote)
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

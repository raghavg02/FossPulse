package io.github.raghavg02.fosspulse.domain.scanner

data class FossMapping(
    val packageName: String,
    val displayName: String,
    val owner: String,
    val repoName: String
) {
    val repoId: String = "${owner.lowercase()}/${repoName.lowercase()}"
}

object FossPackageCatalog {

    val KNOWN_MAPPINGS = listOf(
        FossMapping("dev.imranr.obtainium", "Obtainium", "ImranR98", "Obtainium"),
        FossMapping("org.videolan.vlc", "VLC for Android", "videolan", "vlc-android"),
        FossMapping("org.schabi.newpipe", "NewPipe", "TeamNewPipe", "NewPipe"),
        FossMapping("me.breezes.neostore", "Neo-Store", "NeoApplications", "Neo-Store"),
        FossMapping("com.aurora.store", "Aurora Store", "whyorean", "AuroraStore"),
        FossMapping("org.thunderdog.challegram", "Telegram FOSS", "TGAndroid", "Telegram-FOSS"),
        FossMapping("org.mozilla.fenix", "Firefox Nightly (Fenix)", "mozilla-mobile", "fenix"),
        FossMapping("org.mozilla.firefox", "Firefox for Android", "mozilla-mobile", "fenix"),
        FossMapping("com.duckduckgo.mobile.android", "DuckDuckGo", "duckduckgo", "Android"),
        FossMapping("ch.protonmail.android", "Proton Mail", "ProtonMail", "proton-mail-android"),
        FossMapping("keepass2android.keepass2android", "KeePass2Android", "bpellin", "keepass2android"),
        FossMapping("org.thoughtcrime.securesms", "Signal", "signalapp", "Signal-Android"),
        FossMapping("org.briarproject.briar.android", "Briar", "briar", "briar"),
        FossMapping("com.fsck.k9", "Thunderbird / K-9 Mail", "thunderbird", "thunderbird-android"),
        FossMapping("app.organicmaps", "Organic Maps", "organicmaps", "organicmaps"),
        FossMapping("com.ichi2.anki", "AnkiDroid", "ankidroid", "Anki-Android"),
        FossMapping("com.beemdevelopment.aegis", "Aegis Authenticator", "beemdevelopment", "Aegis"),
        FossMapping("io.github.muntashirakon.AppManager", "App Manager", "MuntashirAkon", "AppManager"),
        FossMapping("com.termux", "Termux", "termux", "termux-app")
    )

    fun findMapping(packageName: String): FossMapping? {
        val trimmed = packageName.trim()
        return KNOWN_MAPPINGS.firstOrNull { it.packageName.equals(trimmed, ignoreCase = true) }
    }

    fun defaultRegexForAbi(abi: String): String {
        return when {
            abi.contains("arm64", ignoreCase = true) || abi.contains("v8a", ignoreCase = true) -> {
                ".*(arm64-v8a|arm64|universal).*\\.apk$"
            }
            abi.contains("v7a", ignoreCase = true) || abi.contains("armeabi", ignoreCase = true) -> {
                ".*(armeabi-v7a|armv7|universal).*\\.apk$"
            }
            abi.contains("x86_64", ignoreCase = true) -> {
                ".*(x86_64|universal).*\\.apk$"
            }
            else -> {
                ".*\\.apk$"
            }
        }
    }
}

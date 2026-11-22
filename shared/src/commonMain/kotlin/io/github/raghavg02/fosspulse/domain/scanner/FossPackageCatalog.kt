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
        // Core Utilities & App Stores
        FossMapping("dev.imranr.obtainium", "Obtainium", "ImranR98", "Obtainium"),
        FossMapping("me.breezes.neostore", "Neo-Store", "NeoApplications", "Neo-Store"),
        FossMapping("com.aurora.store", "Aurora Store", "whyorean", "AuroraStore"),
        FossMapping("io.github.muntashirakon.AppManager", "App Manager", "MuntashirAkon", "AppManager"),
        FossMapping("com.termux", "Termux", "termux", "termux-app"),
        FossMapping("app.revanced.manager.flutter", "ReVanced Manager", "ReVanced", "revanced-manager"),

        // Media, Video & Players
        FossMapping("org.videolan.vlc", "VLC for Android", "videolan", "vlc-android"),
        FossMapping("is.xyz.mpv", "mpv-android", "mpv-android", "mpv-android"),
        FossMapping("live.mehiz.mpvkt", "mpvKt", "mpvKt", "mpvKt"),
        FossMapping("dev.anilbeesetti.nextplayer", "Next Player", "anilbeesetti", "nextplayer"),
        FossMapping("org.schabi.newpipe", "NewPipe", "TeamNewPipe", "NewPipe"),
        FossMapping("com.junkfood.seal", "Seal (Video Downloader)", "JunkFood02", "Seal"),
        FossMapping("com.zionhuang.music", "InnerTune", "z-huang", "InnerTune"),
        FossMapping("it.fast4x.rimusic", "RiMusic", "fast4x", "RiMusic"),
        FossMapping("com.bitchute.mobile", "BitChute", "bitchute", "bitchute-android"),

        // Readers, Notes & Office
        FossMapping("pro.affine.app", "AFFiNE", "toeverything", "AFFiNE"),
        FossMapping("com.tcmedia.storypad", "StoryPad", "marchellodev", "StoryPad"),
        FossMapping("eu.kanade.tachiyomi", "Mihon", "mihonapp", "mihon"),
        FossMapping("com.ichi2.anki", "AnkiDroid", "ankidroid", "Anki-Android"),

        // Fossify Suite
        FossMapping("org.fossify.gallery", "Fossify Gallery", "FossifyOrg", "Gallery"),
        FossMapping("org.fossify.filemanager", "Fossify File Manager", "FossifyOrg", "File-Manager"),
        FossMapping("org.fossify.calendar", "Fossify Calendar", "FossifyOrg", "Calendar"),
        FossMapping("org.fossify.contacts", "Fossify Contacts", "FossifyOrg", "Contacts"),
        FossMapping("org.fossify.messages", "Fossify Messages", "FossifyOrg", "Messages"),
        FossMapping("org.fossify.phone", "Fossify Phone", "FossifyOrg", "Phone"),

        // Networking, Sharing & Tools
        FossMapping("org.localsend.localsend_app", "LocalSend", "localsend", "localsend"),
        FossMapping("org.breezyweather", "Breezy Weather", "breezy-weather", "breezy-weather"),
        FossMapping("app.organicmaps", "Organic Maps", "organicmaps", "organicmaps"),

        // Browsers & Communication
        FossMapping("org.mozilla.fenix", "Firefox Nightly (Fenix)", "mozilla-mobile", "fenix"),
        FossMapping("org.mozilla.firefox", "Firefox for Android", "mozilla-mobile", "fenix"),
        FossMapping("com.duckduckgo.mobile.android", "DuckDuckGo", "duckduckgo", "Android"),
        FossMapping("org.thoughtcrime.securesms", "Signal", "signalapp", "Signal-Android"),
        FossMapping("org.thunderdog.challegram", "Telegram FOSS", "TGAndroid", "Telegram-FOSS"),
        FossMapping("org.briarproject.briar.android", "Briar", "briar", "briar"),
        FossMapping("im.vector.app", "Element (Matrix)", "element-hq", "element-android"),
        FossMapping("chat.fluffy.fluffychat", "FluffyChat", "fluffychat", "fluffychat"),
        FossMapping("com.aliucord", "Aliucord", "Aliucord", "Aliucord"),
        FossMapping("com.fsck.k9", "Thunderbird / K-9 Mail", "thunderbird", "thunderbird-android"),
        FossMapping("ch.protonmail.android", "Proton Mail", "ProtonMail", "proton-mail-android"),

        // Security & Passwords
        FossMapping("com.beemdevelopment.aegis", "Aegis Authenticator", "beemdevelopment", "Aegis"),
        FossMapping("keepass2android.keepass2android", "KeePass2Android", "bpellin", "keepass2android")
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

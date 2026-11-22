package io.github.raghavg02.fosspulse.domain.model

enum class TargetPlatform {
    ANDROID,
    IOS,
    WINDOWS,
    MACOS;

    companion object {
        fun fromString(value: String): TargetPlatform {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ANDROID
        }
    }
}

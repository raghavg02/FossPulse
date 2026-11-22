package io.github.raghavg02.fosspulse

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

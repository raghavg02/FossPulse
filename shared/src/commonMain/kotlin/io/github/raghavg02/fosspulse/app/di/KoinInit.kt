package io.github.raghavg02.fosspulse.app.di

import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration? = null) {
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            appDeclaration?.invoke(this)
            modules(appModule)
        }
    }
}

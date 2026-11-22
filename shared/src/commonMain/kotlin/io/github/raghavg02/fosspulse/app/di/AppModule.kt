package io.github.raghavg02.fosspulse.app.di

import io.github.raghavg02.fosspulse.data.local.FossPulseDatabase
import io.github.raghavg02.fosspulse.data.local.createRoomDatabase
import io.github.raghavg02.fosspulse.data.local.getDatabaseBuilder
import io.github.raghavg02.fosspulse.data.remote.GitHubApiService
import io.github.raghavg02.fosspulse.data.remote.GitHubApiServiceImpl
import io.github.raghavg02.fosspulse.data.repository.RepoRepositoryImpl
import io.github.raghavg02.fosspulse.domain.repository.RepoRepository
import io.github.raghavg02.fosspulse.ui.addrepo.AddRepoViewModel
import io.github.raghavg02.fosspulse.ui.dashboard.DashboardViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<FossPulseDatabase> {
        createRoomDatabase(getDatabaseBuilder())
    }
    single { get<FossPulseDatabase>().trackedRepoDao() }
    single { get<FossPulseDatabase>().cachedReleaseDao() }
    single { get<FossPulseDatabase>().aiSummaryDao() }

    single<RepoRepository> {
        RepoRepositoryImpl(
            trackedRepoDao = get(),
            cachedReleaseDao = get()
        )
    }

    single<GitHubApiService> {
        GitHubApiServiceImpl()
    }

    single {
        io.github.raghavg02.fosspulse.domain.usecase.CheckHourlyReleaseUpdatesUseCase(
            repoRepository = get(),
            gitHubApiService = get()
        )
    }

    single {
        io.github.raghavg02.fosspulse.domain.usecase.RunDailyVersionAuditUseCase(
            repoRepository = get()
        )
    }

    viewModel {
        DashboardViewModel(
            repoRepository = get(),
            gitHubApiService = get()
        )
    }

    viewModel {
        AddRepoViewModel(
            repoRepository = get(),
            gitHubApiService = get()
        )
    }

    viewModel {
        io.github.raghavg02.fosspulse.ui.scanner.InstalledAppsScannerViewModel(
            repoRepository = get(),
            gitHubApiService = get()
        )
    }
}

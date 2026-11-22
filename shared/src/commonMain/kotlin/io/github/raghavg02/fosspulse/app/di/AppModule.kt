package io.github.raghavg02.fosspulse.app.di

import io.github.raghavg02.fosspulse.data.local.FossPulseDatabase
import io.github.raghavg02.fosspulse.data.local.createRoomDatabase
import io.github.raghavg02.fosspulse.data.local.getDatabaseBuilder
import io.github.raghavg02.fosspulse.data.remote.GitHubApiService
import io.github.raghavg02.fosspulse.data.remote.GitHubApiServiceImpl
import io.github.raghavg02.fosspulse.data.remote.GoogleDriveApiService
import io.github.raghavg02.fosspulse.data.remote.GoogleDriveApiServiceImpl
import io.github.raghavg02.fosspulse.data.repository.RepoRepositoryImpl
import io.github.raghavg02.fosspulse.domain.ai.LlmModelManager
import io.github.raghavg02.fosspulse.domain.repository.ChangelogAiRepository
import io.github.raghavg02.fosspulse.domain.repository.ChangelogAiRepositoryImpl
import io.github.raghavg02.fosspulse.domain.repository.RepoRepository
import io.github.raghavg02.fosspulse.domain.usecase.CheckHourlyReleaseUpdatesUseCase
import io.github.raghavg02.fosspulse.domain.usecase.DriveSyncUseCase
import io.github.raghavg02.fosspulse.domain.usecase.RunDailyVersionAuditUseCase
import io.github.raghavg02.fosspulse.platform.ai.LocalLlmEngine
import io.github.raghavg02.fosspulse.platform.ai.PlatformModelStorage
import io.github.raghavg02.fosspulse.platform.auth.GoogleAuthProvider
import io.github.raghavg02.fosspulse.platform.storage.SecureStorage
import io.github.raghavg02.fosspulse.ui.addrepo.AddRepoViewModel
import io.github.raghavg02.fosspulse.ui.dashboard.DashboardViewModel
import io.github.raghavg02.fosspulse.ui.detail.ReleaseDetailViewModel
import io.github.raghavg02.fosspulse.ui.scanner.InstalledAppsScannerViewModel
import io.github.raghavg02.fosspulse.ui.settings.SettingsViewModel
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

    single<SecureStorage> {
        SecureStorage()
    }

    single<GitHubApiService> {
        GitHubApiServiceImpl()
    }

    single<GoogleAuthProvider> {
        GoogleAuthProvider(secureStorage = get<SecureStorage>())
    }

    single<GoogleDriveApiService> {
        GoogleDriveApiServiceImpl(httpClient = GitHubApiServiceImpl.createDefaultHttpClient())
    }

    single {
        DriveSyncUseCase(
            repoRepository = get(),
            googleDriveApiService = get()
        )
    }

    single {
        PlatformModelStorage()
    }

    single {
        LlmModelManager(
            httpClient = GitHubApiServiceImpl.createDefaultHttpClient(),
            storage = get()
        )
    }

    single {
        LocalLlmEngine(modelStorage = get())
    }

    single<ChangelogAiRepository> {
        ChangelogAiRepositoryImpl(
            aiSummaryDao = get(),
            llmModelManager = get(),
            localLlmEngine = get()
        )
    }

    single {
        CheckHourlyReleaseUpdatesUseCase(
            repoRepository = get(),
            gitHubApiService = get()
        )
    }

    single {
        RunDailyVersionAuditUseCase(
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
        InstalledAppsScannerViewModel(
            repoRepository = get(),
            gitHubApiService = get()
        )
    }

    viewModel {
        ReleaseDetailViewModel(
            changelogAiRepository = get(),
            llmModelManager = get()
        )
    }

    viewModel {
        SettingsViewModel(
            secureStorage = get(),
            gitHubApiService = get(),
            googleAuthProvider = get(),
            driveSyncUseCase = get(),
            llmModelManager = get()
        )
    }
}

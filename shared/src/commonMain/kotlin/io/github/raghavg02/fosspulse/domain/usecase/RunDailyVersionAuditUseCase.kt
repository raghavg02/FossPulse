package io.github.raghavg02.fosspulse.domain.usecase

import io.github.raghavg02.fosspulse.domain.engine.NotificationDecisionEngine
import io.github.raghavg02.fosspulse.domain.model.NotificationDecision
import io.github.raghavg02.fosspulse.domain.repository.RepoRepository
import io.github.raghavg02.fosspulse.platform.notifier.PlatformNotifier

class RunDailyVersionAuditUseCase(
    private val repoRepository: RepoRepository,
    private val notifier: PlatformNotifier = PlatformNotifier()
) {
    suspend operator fun invoke(): NotificationDecision.DailyAuditDecision {
        val repos = repoRepository.getAllActiveRepos()
        val decision = NotificationDecisionEngine.evaluateDailyAudit(repos)

        when (decision) {
            is NotificationDecision.DailyAuditDecision.DailyAuditSingleApp -> {
                notifier.notifyDailyAudit(
                    title = decision.title,
                    body = decision.body
                )
            }
            is NotificationDecision.DailyAuditDecision.DailyAuditGrouped -> {
                notifier.notifyDailyAudit(
                    title = decision.title,
                    body = decision.body
                )
            }
            is NotificationDecision.DailyAuditDecision.DailyAuditSilent -> {
                // 0 outdated apps -> completely silent
            }
        }

        return decision
    }
}

package io.qalipsis.plugins.slack.notification

import io.qalipsis.api.report.ExecutionStatus

enum class ReportExecutionStatus(val supportedCampaignStatus: Set<ExecutionStatus>) {
    ALL(setOf(ExecutionStatus.FAILED, ExecutionStatus.WARNING, ExecutionStatus.SUCCESSFUL, ExecutionStatus.ABORTED)),
    FAILED(setOf(ExecutionStatus.FAILED)),
    SUCCESSFUL(setOf(ExecutionStatus.SUCCESSFUL)),
    WARNING(setOf(ExecutionStatus.WARNING)),
    ABORTED(setOf(ExecutionStatus.ABORTED))
}
package io.qalipsis.plugins.slack

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import com.slack.api.methods.AsyncMethodsClient
import com.slack.api.model.Attachment
import com.slack.api.model.Message
import com.slack.api.model.block.HeaderBlock
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.MarkdownTextObject
import com.slack.api.model.block.composition.PlainTextObject
import io.micronaut.context.annotation.Value
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.qalipsis.api.report.CampaignReport
import io.qalipsis.api.report.ExecutionStatus
import io.qalipsis.api.sync.asSuspended
import io.qalipsis.plugins.slack.notification.SlackNotificationConfiguration
import io.qalipsis.plugins.slack.notification.SlackNotificationPublisher
import io.qalipsis.plugins.slack.notification.catadioptre.asyncSlackMethodsClient
import io.qalipsis.plugins.slack.notification.catadioptre.postChatMessageRequest
import io.qalipsis.test.coroutines.TestDispatcherProvider
import io.qalipsis.test.mockk.WithMockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.Duration
import java.time.Instant

@WithMockk
@MicronautTest(startApplication = false, propertySources = ["classpath:application-slack-test.yml"])
internal class SlackNotificationPublisherIntegrationTest {

    @JvmField
    @RegisterExtension
    val testDispatcherProvider = TestDispatcherProvider()

    @Value("\${report.export.slack.token}")
    private lateinit var botToken: String

    private lateinit var mockSlackClient: AsyncMethodsClient

    private lateinit var slackNotificationPublisher: SlackNotificationPublisher

    private lateinit var campaignReportPrototype: CampaignReport

    private lateinit var slackNotificationConfiguration: SlackNotificationConfiguration

    @BeforeEach
    internal fun setupAll() {
        campaignReportPrototype = CampaignReport(
            campaignKey = "Campaign-1",
            startedMinions = 1000,
            completedMinions = 990,
            successfulExecutions = 990,
            failedExecutions = 10,
            status = ExecutionStatus.SUCCESSFUL,
            scheduledMinions = 4,
            start = Instant.parse("2022-10-29T00:00:00.00Z"),
            end = Instant.parse("2022-11-05T00:00:00.00Z")
        )

        slackNotificationConfiguration = mockk {
            every { enabled } returns true
            every { channel } returns "slack-test"
            every { status } returns "ALL"
            every { url } returns "https://slack.com/api/chat.postMessage"
            every { token } returns botToken
        }
        slackNotificationPublisher =
            SlackNotificationPublisher(slackNotificationConfiguration)
        slackNotificationPublisher.init()
        mockSlackClient = spyk(slackNotificationPublisher.asyncSlackMethodsClient())
        slackNotificationPublisher.asyncSlackMethodsClient(mockSlackClient)
    }


    @Test
    fun `should send notification for successful campaign`() = testDispatcherProvider.run {
        // given
        val campaignReport = campaignReportPrototype.copy(failedExecutions = 0)
        val message = composeMessage(campaignReport)
        val colorScheme = getColorScheme(campaignReport.status)

        // when
        val response = slackNotificationPublisher.postChatMessageRequest(
            campaignReport.campaignKey,
            campaignReport,
            message,
            colorScheme
        ).asSuspended().get()

        // then
        val retrievedMessage: Message = retrieveMessage(response.channel, response.ts)
        val headerBlock = retrievedMessage.blocks[0] as HeaderBlock
        val attachmentBlock = retrievedMessage.attachments[0]
        assertThat(headerBlock.text).isEqualTo(
            PlainTextObject(
                "${campaignReport.campaignKey} ${campaignReport.status} ${colorScheme.second}",
                true
            )
        )
        assertThat(attachmentBlock.blocks[0] as SectionBlock).all {
            prop(SectionBlock::getType).isEqualTo("section")
            prop(SectionBlock::getText).isEqualTo(MarkdownTextObject(message, false))
        }
        assertThat(attachmentBlock).all {
            prop(Attachment::getFallback).isEqualTo("${campaignReport.campaignKey} ${campaignReport.status}")
            prop(Attachment::getColor).isEqualTo(colorScheme.first)
        }
    }

    @Test
    fun `should send notification for a failed campaign`() = testDispatcherProvider.run {
        // given
        val campaignReport = campaignReportPrototype.copy(
            status = ExecutionStatus.FAILED,
            failedExecutions = 900,
            successfulExecutions = 100,
            completedMinions = 100,
            campaignKey = "Campaign-2"
        )
        val message = composeMessage(campaignReport)
        val colorScheme = getColorScheme(campaignReport.status)

        // when
        val response = slackNotificationPublisher.postChatMessageRequest(
            campaignReport.campaignKey,
            campaignReport,
            message,
            colorScheme
        ).asSuspended().get()

        // then
        val retrievedMessage: Message = retrieveMessage(response.channel, response.ts)
        val headerBlock = retrievedMessage.blocks[0] as HeaderBlock
        val attachmentBlock = retrievedMessage.attachments[0]
        assertThat(headerBlock.text).isEqualTo(
            PlainTextObject(
                "${campaignReport.campaignKey} ${campaignReport.status} ${colorScheme.second}",
                true
            )
        )
        assertThat(attachmentBlock.blocks[0] as SectionBlock).all {
            prop(SectionBlock::getType).isEqualTo("section")
            prop(SectionBlock::getText).isEqualTo(MarkdownTextObject(message, false))
        }
        assertThat(attachmentBlock).all {
            prop(Attachment::getFallback).isEqualTo("${campaignReport.campaignKey} ${campaignReport.status}")
            prop(Attachment::getColor).isEqualTo(colorScheme.first)
        }
    }

    @Test
    fun `should send notification for a campaign with warning or other status`() = testDispatcherProvider.run {
        // when
        val campaignReport = campaignReportPrototype.copy(
            status = ExecutionStatus.WARNING,
            failedExecutions = 500,
            successfulExecutions = 200,
            completedMinions = 200,
            startedMinions = 700,
            campaignKey = "Campaign-3"
        )
        val message = composeMessage(campaignReport)
        val colorScheme = getColorScheme(campaignReport.status)

        // when
        val response = slackNotificationPublisher.postChatMessageRequest(
            campaignReport.campaignKey,
            campaignReport,
            message,
            colorScheme
        ).asSuspended().get()

        // then
        val retrievedMessage: Message = retrieveMessage(response.channel, response.ts)
        val headerBlock = retrievedMessage.blocks[0] as HeaderBlock
        val attachmentBlock = retrievedMessage.attachments[0]
        assertThat(headerBlock.text).isEqualTo(
            PlainTextObject(
                "${campaignReport.campaignKey} ${campaignReport.status} ${colorScheme.second}",
                true
            )
        )
        assertThat(attachmentBlock.blocks[0] as SectionBlock).all {
            prop(SectionBlock::getType).isEqualTo("section")
            prop(SectionBlock::getText).isEqualTo(MarkdownTextObject(message, false))
        }
        assertThat(attachmentBlock).all {
            prop(Attachment::getFallback).isEqualTo("${campaignReport.campaignKey} ${campaignReport.status}")
            prop(Attachment::getColor).isEqualTo(colorScheme.first)
        }
    }

    private fun composeMessage(report: CampaignReport): String {
        val duration = report.end?.let { Duration.between(report.start, it).toSeconds() }
        return "*Campaign*.......................................${report.campaignKey}\n" +
                "*Start*.................................................${report.start}\n" +
                "*End*...................................................${report.end ?: "<Running>"}\n" +
                "*Duration*.........................................${duration?.let { "$it seconds" } ?: "<Running>"}\n" +
                "*Started minions*............................${report.startedMinions}\n" +
                "*Completed minions*.....................${report.completedMinions}\n" +
                "*Successful steps executions*.....${report.successfulExecutions}\n" +
                "*Failed steps executions*..............${report.failedExecutions}\n" +
                "*Status*..............................................${report.status}"
    }

    /**
     * Fetch message using the channelId and the message id
     */
    private fun retrieveMessage(channelId: String, messageId: String): Message {
        val result = mockSlackClient.conversationsHistory { r ->
            r
                .token(slackNotificationConfiguration.token)
                .channel(channelId)
                .latest(messageId)
                .inclusive(true)
                .limit(1)
        }
        return result.get().messages[0]
    }

    private fun getColorScheme(status: ExecutionStatus): Pair<String, String> {
        return when (status) {
            ExecutionStatus.SUCCESSFUL -> Pair("#36a64f", ":large_green_circle:")
            ExecutionStatus.WARNING -> Pair("#e69d0b", ":large_orange_circle:")
            else -> Pair("#bf0606", ":red_circle:")
        }
    }
}
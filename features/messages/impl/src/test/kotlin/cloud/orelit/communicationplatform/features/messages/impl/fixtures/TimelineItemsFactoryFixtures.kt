/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.fixtures

import cloud.orelit.communicationplatform.features.messages.impl.messagesummary.FakeMessageSummaryFormatter
import cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.TimelineItemsFactory
import cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.TimelineItemsFactoryConfig
import cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.event.TimelineItemContentFactory
import cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.event.TimelineItemContentFailedToParseMessageFactory
import cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.event.TimelineItemContentFailedToParseStateFactory
import cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.event.TimelineItemContentMessageFactory
import cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.event.TimelineItemContentPollFactory
import cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.event.TimelineItemContentProfileChangeFactory
import cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.event.TimelineItemContentRedactedFactory
import cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.event.TimelineItemContentRoomMembershipFactory
import cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.event.TimelineItemContentStateFactory
import cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.event.TimelineItemContentStickerFactory
import cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.event.TimelineItemContentUTDFactory
import cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.event.TimelineItemEventFactory
import cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.virtual.TimelineItemDaySeparatorFactory
import cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.virtual.TimelineItemVirtualFactory
import cloud.orelit.communicationplatform.features.messages.impl.timeline.groups.TimelineItemGrouper
import cloud.orelit.communicationplatform.features.messages.impl.utils.FakeTextPillificationHelper
import cloud.orelit.communicationplatform.features.messages.test.timeline.FakeHtmlConverterProvider
import cloud.orelit.communicationplatform.features.poll.test.pollcontent.FakePollContentStateFactory
import cloud.orelit.communicationplatform.libraries.androidutils.filesize.FakeFileSizeFormatter
import cloud.orelit.communicationplatform.libraries.dateformatter.test.FakeDateFormatter
import cloud.orelit.communicationplatform.libraries.eventformatter.api.TimelineEventFormatter
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.EventContent
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.test.permalink.FakePermalinkParser
import cloud.orelit.communicationplatform.libraries.mediaviewer.test.util.FileExtensionExtractorWithoutValidation
import cloud.orelit.communicationplatform.services.toolbox.test.strings.FakeStringProvider
import cloud.orelit.communicationplatform.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope

internal fun TestScope.aTimelineItemsFactoryCreator(): TimelineItemsFactory.Creator {
    return object : TimelineItemsFactory.Creator {
        override fun create(config: TimelineItemsFactoryConfig): TimelineItemsFactory {
            return aTimelineItemsFactory(config)
        }
    }
}

internal fun aTimelineItemContentFactory(
    timelineEventFormatter: TimelineEventFormatter = aTimelineEventFormatter(),
    matrixClient: FakeMatrixClient = FakeMatrixClient(),
): TimelineItemContentFactory = TimelineItemContentFactory(
    messageFactory = TimelineItemContentMessageFactory(
        fileSizeFormatter = FakeFileSizeFormatter(),
        fileExtensionExtractor = FileExtensionExtractorWithoutValidation(),
        htmlConverterProvider = FakeHtmlConverterProvider(),
        permalinkParser = FakePermalinkParser(),
        textPillificationHelper = FakeTextPillificationHelper(),
    ),
    redactedMessageFactory = TimelineItemContentRedactedFactory(),
    stickerFactory = TimelineItemContentStickerFactory(
        fileSizeFormatter = FakeFileSizeFormatter(),
        fileExtensionExtractor = FileExtensionExtractorWithoutValidation()
    ),
    pollFactory = TimelineItemContentPollFactory(FakePollContentStateFactory()),
    utdFactory = TimelineItemContentUTDFactory(),
    roomMembershipFactory = TimelineItemContentRoomMembershipFactory(timelineEventFormatter),
    profileChangeFactory = TimelineItemContentProfileChangeFactory(timelineEventFormatter),
    stateFactory = TimelineItemContentStateFactory(timelineEventFormatter),
    failedToParseMessageFactory = TimelineItemContentFailedToParseMessageFactory(),
    failedToParseStateFactory = TimelineItemContentFailedToParseStateFactory(),
    sessionId = matrixClient.sessionId,
    dateFormatter = FakeDateFormatter(),
    stringProvider = FakeStringProvider(),
)

internal fun TestScope.aTimelineItemsFactory(
    config: TimelineItemsFactoryConfig,
): TimelineItemsFactory {
    val matrixClient = FakeMatrixClient()
    return TimelineItemsFactory(
        dispatchers = testCoroutineDispatchers(),
        eventItemFactoryCreator = object : TimelineItemEventFactory.Creator {
            override fun create(config: TimelineItemsFactoryConfig): TimelineItemEventFactory {
                return TimelineItemEventFactory(
                    contentFactory = aTimelineItemContentFactory(matrixClient = matrixClient),
                    matrixClient = matrixClient,
                    dateFormatter = FakeDateFormatter(),
                    permalinkParser = FakePermalinkParser(),
                    config = config,
                    summaryFormatter = FakeMessageSummaryFormatter(),
                )
            }
        },
        virtualItemFactory = TimelineItemVirtualFactory(
            daySeparatorFactory = TimelineItemDaySeparatorFactory(
                FakeDateFormatter()
            ),
        ),
        timelineItemGrouper = TimelineItemGrouper(),
        config = config
    )
}

internal fun aTimelineEventFormatter(): TimelineEventFormatter {
    return object : TimelineEventFormatter {
        override fun format(content: EventContent, isOutgoing: Boolean, sender: UserId, senderDisambiguatedDisplayName: String): CharSequence? {
            return ""
        }
    }
}

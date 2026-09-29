/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.search

import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import cloud.orelit.communicationplatform.libraries.androidutils.filesize.FileSizeFormatter
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.core.coroutine.CoroutineDispatchers
import cloud.orelit.communicationplatform.libraries.dateformatter.api.DateFormatter
import cloud.orelit.communicationplatform.libraries.dateformatter.api.DateFormatterMode
import cloud.orelit.communicationplatform.libraries.eventformatter.api.RoomLatestEventFormatter
import cloud.orelit.communicationplatform.libraries.featureflag.api.FeatureFlagService
import cloud.orelit.communicationplatform.libraries.featureflag.api.FeatureFlags
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkParser
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomInfo
import cloud.orelit.communicationplatform.libraries.matrix.api.roomlist.LatestEventValue
import cloud.orelit.communicationplatform.libraries.matrix.api.search.MessageSearch
import cloud.orelit.communicationplatform.libraries.matrix.api.search.MessageSearchPaginationState
import cloud.orelit.communicationplatform.libraries.matrix.api.search.MessageSearchResult
import cloud.orelit.communicationplatform.libraries.matrix.api.search.MessageSearchService
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.AudioMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FileMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.ImageMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.MessageContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.MessageTypeWithAttachment
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.StickerMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.VideoMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.VoiceMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.isMediaContent
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.AttachmentThumbnailType
import cloud.orelit.communicationplatform.libraries.matrix.ui.messages.toPlainText
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.jvm.optionals.getOrElse
import kotlin.time.Duration.Companion.milliseconds

@Inject
class GlobalSearchPresenter(
    private val roomListSearchDataSourceFactory: RoomListSearchDataSource.Factory,
    private val messageSearchService: MessageSearchService,
    private val featureFlagService: FeatureFlagService,
    private val latestEventFormatter: RoomLatestEventFormatter,
    private val dateFormatter: DateFormatter,
    private val fileSizeFormatter: FileSizeFormatter,
    private val permalinkParser: PermalinkParser,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val matrixClient: MatrixClient,
) : Presenter<GlobalSearchState> {
    @Composable
    override fun present(): GlobalSearchState {
        val coroutineScope = rememberCoroutineScope()
        val isEnabled by produceState(false) {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.MessageSearch).collectLatest { value = it }
        }
        val roomListSearchDataSource = remember { roomListSearchDataSourceFactory.create(coroutineScope = coroutineScope) }
        val queryState = rememberTextFieldState()
        var isSearchActive by remember { mutableStateOf(false) }
        var searchResults: AsyncData<GlobalSearchResults> by remember { mutableStateOf(AsyncData.Uninitialized) }
        var currentTarget: GlobalSearchTarget by remember { mutableStateOf(GlobalSearchTarget.ROOMS) }
        val currentMessageSearch: MessageSearch = remember { messageSearchService.createMessageSearch(scope = coroutineScope) }

        LaunchedEffect(queryState.text) {
            // Add a delay to avoid performing too many searches in a short period of time, which can be expensive
            delay(200.milliseconds)

            searchResults = if (queryState.text.isNotEmpty()) {
                AsyncData.Loading(prevData = searchResults.dataOrNull())
            } else {
                AsyncData.Uninitialized
            }

            launch { roomListSearchDataSource.setSearchQuery(queryState.text.toString()) }
            launch {
                currentMessageSearch.setQuery(queryState.text.toString())
                    .onFailure { Timber.e(it, "Could not set query for message search") }
            }
        }

        LaunchedEffect(currentTarget) {
            if (queryState.text.isNotEmpty()) {
                searchResults = AsyncData.Loading()
            }

            when (currentTarget) {
                GlobalSearchTarget.ROOMS -> {
                    roomListSearchDataSource.roomSummaries.collectLatest { results ->
                        Timber.d("Room list search found ${results.size} items")
                        // The room list data source always returns some values, even if the query is empty,
                        // so we only want to update the search results if the query is not empty
                        searchResults = if (queryState.text.isNotEmpty()) {
                            AsyncData.Success(GlobalSearchResults.RoomListResults(results = results))
                        } else {
                            AsyncData.Uninitialized
                        }
                    }
                }
                GlobalSearchTarget.MESSAGES -> {
                    Timber.d("Message search found ${currentMessageSearch.results.value.size} items")
                    combine(
                        currentMessageSearch.results,
                        currentMessageSearch.paginationState,
                    ) { results, paginationState ->
                        Pair(results, paginationState)
                    }.collectLatest { (results, paginationState) ->
                        Timber.d("Message search found ${results.size} items, pagination state: $paginationState")

                        // Try processing the results in parallel to speed up the processing time, since we have to wait until we get the room info for each
                        // result, which can be slow if we have a lot of results. The original order is kept by using `awaitAll()`.
                        val mappedResults = results.map { result ->
                            async(coroutineDispatchers.computation) {
                                val roomInfo = matrixClient.getRoomInfoFlow(result.roomId).first().getOrElse { return@async null }
                                val formattedTimestamp = dateFormatter.format(
                                    timestamp = result.timestamp,
                                    mode = DateFormatterMode.TimeOrDate,
                                    useRelative = true
                                )

                                if (!result.content.isMediaContent()) {
                                    mapMessageContent(result, roomInfo, formattedTimestamp)
                                } else {
                                    mapMediaContent(result, roomInfo, formattedTimestamp)
                                }
                            }
                        }
                            .awaitAll()
                            .mapNotNull { it }

                        searchResults = when {
                            queryState.text.isEmpty() -> AsyncData.Uninitialized
                            mappedResults.isNotEmpty() -> {
                                AsyncData.Success(GlobalSearchResults.MessageSearchResults(results = mappedResults.toImmutableList()))
                            }
                            paginationState is MessageSearchPaginationState.Idle && paginationState.endReached -> {
                                AsyncData.Success(GlobalSearchResults.MessageSearchResults(results = persistentListOf()))
                            }
                            paginationState is MessageSearchPaginationState.Loading -> {
                                AsyncData.Loading(prevData = searchResults.dataOrNull())
                            }
                            else -> {
                                AsyncData.Uninitialized
                            }
                        }
                    }
                }
            }
        }

        fun handleEvent(event: GlobalSearchEvent) {
            when (event) {
                GlobalSearchEvent.ClearQuery -> {
                    queryState.clearText()
                }
                GlobalSearchEvent.ToggleSearchVisibility -> {
                    isSearchActive = !isSearchActive
                    queryState.clearText()
                }
                is GlobalSearchEvent.UpdateVisibleRange -> {
                    when (currentTarget) {
                        GlobalSearchTarget.ROOMS -> coroutineScope.launch {
                            // TODO Maybe this is not needed? We're not displaying the latest event or timestamp info so maybe we dont need to subscribe
                            //  to the room info for each room in the search results.
                            roomListSearchDataSource.updateVisibleRange(event.range)
                        }
                        GlobalSearchTarget.MESSAGES -> coroutineScope.launch {
                            val currentCount = (searchResults.dataOrNull() as? GlobalSearchResults.MessageSearchResults)?.results?.size ?: 0
                            val currentPaginationState = currentMessageSearch.paginationState.value
                            val canPaginate = currentPaginationState is MessageSearchPaginationState.Idle && !currentPaginationState.endReached
                            if (event.range.last >= currentCount - 10 && canPaginate) {
                                currentMessageSearch.paginate()
                            }
                        }
                    }
                }
                is GlobalSearchEvent.UpdateTarget -> currentTarget = event.target
            }
        }

        return GlobalSearchState(
            isEnabled = isEnabled,
            isSearchActive = isSearchActive,
            queryState = queryState,
            currentTarget = currentTarget,
            results = searchResults,
            eventSink = ::handleEvent,
        )
    }

    private fun mapMessageContent(
        result: MessageSearchResult,
        roomInfo: RoomInfo,
        formattedTimestamp: String,
    ): MessageSearchResultItem.Message? {
        val body = latestEventFormatter.format(
            latestEvent = LatestEventValue.Remote(
                timestamp = result.timestamp,
                content = result.content,
                senderId = result.senderId,
                senderProfile = result.senderProfile,
                isOwn = matrixClient.isMe(result.senderId),
            ),
            isDmRoom = false,
        )
        return MessageSearchResultItem.Message(
            messageSearchResult = result,
            body = body?.toString() ?: "",
            formattedTimestamp = formattedTimestamp,
            roomInfo = roomInfo,
        )
    }

    private fun mapMediaContent(
        result: MessageSearchResult,
        roomInfo: RoomInfo,
        formattedTimestamp: String,
    ): MessageSearchResultItem.Media? {
        val messageType = when (val content = result.content) {
            is MessageContent if content.type is MessageTypeWithAttachment -> content.type as MessageTypeWithAttachment
            else -> return null
        }

        val caption = messageType.toPlainText(permalinkParser, default = messageType.caption ?: messageType.filename)

        val thumbnailType = when (messageType) {
            is ImageMessageType -> AttachmentThumbnailType.Image
            is VideoMessageType -> AttachmentThumbnailType.Video
            is AudioMessageType -> AttachmentThumbnailType.Audio
            is VoiceMessageType -> AttachmentThumbnailType.Voice
            is FileMessageType -> AttachmentThumbnailType.File
            is StickerMessageType -> AttachmentThumbnailType.Image
        }
        val thumbnailSource = when (messageType) {
            is ImageMessageType -> messageType.info?.thumbnailSource ?: messageType.source
            is VideoMessageType -> messageType.info?.thumbnailSource
            is AudioMessageType -> null
            is VoiceMessageType -> null
            is FileMessageType -> null
            is StickerMessageType -> messageType.info?.thumbnailSource ?: messageType.source
        }
        val blurhash = when (messageType) {
            is ImageMessageType -> messageType.info?.blurhash
            is VideoMessageType -> messageType.info?.blurhash
            is AudioMessageType -> null
            is VoiceMessageType -> null
            is FileMessageType -> null
            is StickerMessageType -> messageType.info?.blurhash
        }

        val extension = messageType.filename.split(".").takeIf { it.size > 1 }?.lastOrNull()?.uppercase()
        val formattedSize = when (messageType) {
            is ImageMessageType -> messageType.info?.size
            is VideoMessageType -> messageType.info?.size
            is AudioMessageType -> messageType.info?.size
            is VoiceMessageType -> messageType.info?.size
            is FileMessageType -> messageType.info?.size
            is StickerMessageType -> messageType.info?.size
        }
            ?.let(fileSizeFormatter::format)

        val mediaContent = MediaSearchResultContent(
            filename = messageType.filename,
            extension = extension,
            caption = caption,
            formattedSize = formattedSize,
            thumbnailSource = thumbnailSource,
            thumbnailType = thumbnailType,
            blurhash = blurhash
        )

        return MessageSearchResultItem.Media(
            messageSearchResult = result,
            mediaContent = mediaContent,
            formattedTimestamp = formattedTimestamp,
            roomInfo = roomInfo,
        )
    }
}

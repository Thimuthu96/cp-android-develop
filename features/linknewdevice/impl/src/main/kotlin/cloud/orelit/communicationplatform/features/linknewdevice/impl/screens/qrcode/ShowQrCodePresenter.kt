/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.linknewdevice.impl.screens.qrcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.features.linknewdevice.impl.LinkNewMobileHandler
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.core.log.logger.LoggerTag
import cloud.orelit.communicationplatform.libraries.matrix.api.linknewdevice.LinkMobileStep
import cloud.orelit.communicationplatform.libraries.matrix.api.logs.LoggerTags
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

private val tag = LoggerTag("ShowQrCodePresenter", LoggerTags.linkNewDevice)

@AssistedInject
class ShowQrCodePresenter(
    @Assisted private val initialData: String,
    @Assisted private val maxQrCodeRotation: Int,
    private val linkNewMobileHandler: LinkNewMobileHandler,
) : Presenter<ShowQrCodeState> {
    @AssistedFactory
    interface Factory {
        fun create(
            initialData: String,
            maxQrCodeRotation: Int,
        ): ShowQrCodePresenter
    }

    private var loadingJob: Job? = null

    @Composable
    override fun present(): ShowQrCodeState {
        var qrCodeRotationCounter by remember { mutableIntStateOf(maxQrCodeRotation) }
        val state by produceState(
            initialValue = ShowQrCodeState(
                data = AsyncData.Success(initialData),
            )
        ) {
            linkNewMobileHandler.stepFlow.collect { step ->
                when (step) {
                    is LinkMobileStep.QrReady -> {
                        loadingJob?.cancel()
                        value = ShowQrCodeState(
                            data = AsyncData.Success(step.data),
                        )
                    }
                    is LinkMobileStep.QrRotating -> {
                        if (qrCodeRotationCounter-- > 0) {
                            Timber.tag(tag.value).d("Rotating QrCode")
                            linkNewMobileHandler.rotateQrCode()
                            // Ensure that outdated data is not rendered too long while rotating QR code
                            loadingJob = launch {
                                delay(1.seconds)
                                value = ShowQrCodeState(
                                    data = AsyncData.Loading(),
                                )
                            }
                        } else {
                            Timber.tag(tag.value).w("Max QR code rotation reached, not rotating anymore")
                            linkNewMobileHandler.onTooManyRotation()
                        }
                    }
                    else -> Unit
                }
            }
        }

        return state
    }
}

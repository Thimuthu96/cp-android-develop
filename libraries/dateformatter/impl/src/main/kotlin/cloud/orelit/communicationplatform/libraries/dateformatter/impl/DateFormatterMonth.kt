/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.dateformatter.impl

import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.libraries.core.extensions.safeCapitalize
import cloud.orelit.communicationplatform.services.toolbox.api.strings.StringProvider

@Inject
class DateFormatterMonth(
    private val stringProvider: StringProvider,
    private val localDateTimeProvider: LocalDateTimeProvider,
    private val dateFormatters: DateFormatters,
) {
    fun format(
        timestamp: Long,
        useRelative: Boolean,
    ): String {
        val today = localDateTimeProvider.providesNow()
        val dateToFormat = localDateTimeProvider.providesFromTimestamp(timestamp)
        return if (useRelative && dateToFormat.month == today.month && dateToFormat.year == today.year) {
            stringProvider.getString(R.string.common_date_this_month)
        } else {
            dateFormatters.formatDateWithMonthAndYear(dateToFormat)
        }
            .safeCapitalize()
    }
}

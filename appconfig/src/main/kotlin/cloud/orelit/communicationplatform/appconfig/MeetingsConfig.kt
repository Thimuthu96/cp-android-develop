/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.appconfig

/**
 * Configuration of the Matrix Meetings integration, https://github.com/nordeck/matrix-meetings.
 *
 * Events are created by sending a room message to the meetings bot, exactly like the meetings widget of the web app does.
 */
object MeetingsConfig {
    /**
     * The Matrix user id of the meetings bot deployed next to the homeserver, for instance `@meetings-bot:example.org`.
     * Creating events is not available while this is null.
     *
     * TODO(orelit): set the user id of the meetings bot of the OREL deployment.
     */
    val BOT_USER_ID: String? = null
}

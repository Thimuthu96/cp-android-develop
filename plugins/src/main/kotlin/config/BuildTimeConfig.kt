/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package config

object BuildTimeConfig {
    const val APPLICATION_ID = "cloud.orelit.communicationplatform"
    const val APPLICATION_NAME = "Communication Platform"
    val METADATA_HOST_REVERSED: String? = null
    val OAUTH_CLIENT_URL_PATH: String? = "apps/android"
    // TODO(orelit): set real legal/marketing URLs before a public release.
    val URL_WEBSITE: String? = null
    // TODO(orelit): set real legal/marketing URLs before a public release.
    val URL_LOGO: String? = null
    // TODO(orelit): set real legal/marketing URLs before a public release.
    val URL_COPYRIGHT: String? = null
    // TODO(orelit): set real legal/marketing URLs before a public release.
    val URL_ACCEPTABLE_USE: String? = null
    // TODO(orelit): set real legal/marketing URLs before a public release.
    val URL_PRIVACY: String? = null
    // TODO(orelit): set real legal/marketing URLs before a public release.
    val URL_POLICY: String? = null
    // TODO(orelit): set real Maptiler credentials before a public release.
    val SERVICES_MAPTILER_BASE_URL: String? = null
    // TODO(orelit): set real Maptiler credentials before a public release.
    val SERVICES_MAPTILER_APIKEY: String? = null
    // TODO(orelit): set real Maptiler credentials before a public release.
    val SERVICES_MAPTILER_LIGHT_MAPID: String? = null
    // TODO(orelit): set real Maptiler credentials before a public release.
    val SERVICES_MAPTILER_DARK_MAPID: String? = null
    // TODO(orelit): set real PostHog credentials before a public release.
    val SERVICES_POSTHOG_HOST: String? = null
    // TODO(orelit): set real PostHog credentials before a public release.
    val SERVICES_POSTHOG_APIKEY: String? = null
    // TODO(orelit): set real Sentry DSN before a public release.
    val SERVICES_SENTRY_DSN: String? = null
    // TODO(orelit): set real Sentry DSN before a public release.
    val SERVICES_SENTRY_DSN_RUST: String? = null
    // TODO(orelit): set real bug report endpoint before a public release.
    val BUG_REPORT_URL: String? = null
    // TODO(orelit): set real bug report app name before a public release.
    val BUG_REPORT_APP_NAME: String? = null
    const val PUSH_CONFIG_INCLUDE_FIREBASE: Boolean = true
    const val PUSH_CONFIG_INCLUDE_UNIFIED_PUSH: Boolean = true
    // TODO(orelit): set real Firebase/Pusher credentials before a public release.
    val PUSHER_APP_ID_RELEASE: String? = null
    // TODO(orelit): set real Firebase/Pusher credentials before a public release.
    val PUSHER_APP_ID_DEBUG: String? = null
    // TODO(orelit): set real Firebase/Pusher credentials before a public release.
    val PUSHER_APP_ID_NIGHTLY: String? = null
}

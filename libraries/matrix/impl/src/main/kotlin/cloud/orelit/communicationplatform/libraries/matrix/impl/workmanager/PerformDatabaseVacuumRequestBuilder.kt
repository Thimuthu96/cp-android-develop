/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.workmanager

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.PeriodicWorkRequest
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.impl.workmanager.VacuumDatabaseWorker.Companion.SESSION_ID_PARAM
import cloud.orelit.communicationplatform.libraries.workmanager.api.WorkManagerRequestBuilder
import cloud.orelit.communicationplatform.libraries.workmanager.api.WorkManagerRequestType
import cloud.orelit.communicationplatform.libraries.workmanager.api.WorkManagerRequestWrapper
import cloud.orelit.communicationplatform.libraries.workmanager.api.workManagerTag
import java.util.concurrent.TimeUnit

class PerformDatabaseVacuumRequestBuilder(
    private val sessionId: SessionId,
) : WorkManagerRequestBuilder {
    override suspend fun build(): Result<List<WorkManagerRequestWrapper>> {
        val data = Data.Builder().putString(SESSION_ID_PARAM, sessionId.value).build()
        val workRequest = PeriodicWorkRequest.Builder(
            workerClass = VacuumDatabaseWorker::class,
            // Run once a day
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS,
        )
            .addTag(workManagerTag(sessionId, WorkManagerRequestType.DB_VACUUM))
            .setInputData(data)
            // Only run when the device is idle to avoid impacting user experience
            .setConstraints(
                Constraints.Builder()
                    .setRequiresDeviceIdle(true)
                    // Vacuuming can duplicate the DB sizes in disk
                    .setRequiresStorageNotLow(true)
                    .build()
            )
            .build()

        return Result.success(listOf(WorkManagerRequestWrapper(workRequest)))
    }
}

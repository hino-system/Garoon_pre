package com.example.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.garoon_pre.feature.schedule.domain.repository.ScheduleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@HiltWorker
class SyncTodaySchedulesWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: ScheduleRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return runCatching {
            val today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
            repository.syncSchedulesByDate(today)
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }
}
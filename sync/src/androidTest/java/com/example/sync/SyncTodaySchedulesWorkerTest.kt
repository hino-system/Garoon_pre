package com.example.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.garoon_pre.feature.schedule.domain.repository.ScheduleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SyncTodaySchedulesWorkerTest {

    private val repository = mockk<ScheduleRepository>()

    @Test
    fun doWork_returns_success_when_repository_succeeds() = runTest {
        coEvery {
            repository.syncSchedulesByDate(any(), any())
        } returns Unit

        val worker = buildWorker(repository)

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)

        coVerify(exactly = 1) {
            repository.syncSchedulesByDate(any(), any())
        }
    }

    @Test
    fun doWork_returns_retry_when_repository_throws() = runTest {
        coEvery {
            repository.syncSchedulesByDate(any(), any())
        } throws IllegalStateException("boom")

        val worker = buildWorker(repository)

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)

        coVerify(exactly = 1) {
            repository.syncSchedulesByDate(any(), any())
        }
    }

    private fun buildWorker(
        repository: ScheduleRepository
    ): SyncTodaySchedulesWorker {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val factory = object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker? {
                return when (workerClassName) {
                    SyncTodaySchedulesWorker::class.java.name -> {
                        SyncTodaySchedulesWorker(
                            appContext = appContext,
                            params = workerParameters,
                            repository = repository
                        )
                    }
                    else -> null
                }
            }
        }

        return TestListenableWorkerBuilder<SyncTodaySchedulesWorker>(context)
            .setWorkerFactory(factory)
            .build()
    }
}
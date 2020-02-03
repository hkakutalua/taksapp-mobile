package com.taksapp.taksapp.data.infrastructure.services

import android.os.Handler
import android.os.Looper
import com.taksapp.taksapp.domain.interfaces.TaskScheduler
import org.joda.time.DateTime
import java.util.*
import kotlin.concurrent.schedule
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
class TimerTaskScheduler : TaskScheduler {
    private val tasks = mutableListOf<ScheduledTask>()

    override fun schedule(id: String, date: DateTime, task: () -> Unit) {
        val scheduledTask = OneTimeExecutionTask(id, date, task)
        tasks.add(scheduledTask)
    }

    override fun schedule(id: String, interval: Duration, task: () -> Unit) {
        val scheduledTask = PeriodicExecutionTask(id, interval, task)
        tasks.add(scheduledTask)
    }

    override fun pause(taskIdentifier: String) {
        val taskToPause = tasks.firstOrNull { x -> x.id == taskIdentifier }
        taskToPause?.paused = true
    }

    override fun resume(taskIdentifier: String) {
        val taskToResume = tasks.firstOrNull { x -> x.id == taskIdentifier }
        taskToResume?.paused = false
    }

    override fun cancel(taskIdentifier: String) {
        val taskToCancel = tasks.firstOrNull { x -> x.id == taskIdentifier }
        taskToCancel?.let {
            it.cancel()
            tasks.remove(taskToCancel)
        }
    }

    private abstract class ScheduledTask(val id: String) {
        private var cancelled = false

        var paused: Boolean = false
            set(value) {
                if (cancelled)
                    return

                if (value) {
                    cancelTask()
                } else {
                    scheduleTask()
                }

                field = value
            }

        fun cancel() {
            cancelled = true
            cancelTask()
        }

        protected abstract fun scheduleTask()
        protected abstract fun cancelTask()
    }

    private class OneTimeExecutionTask(
        id: String,
        val scheduledDate: DateTime,
        val task: () -> Unit): ScheduledTask(id) {

        private lateinit var _timerTask: TimerTask

        init {
            scheduleTask()
        }

        override fun scheduleTask() {
            _timerTask = Timer().schedule(scheduledDate.toDate()) {
                val mainLooper = Looper.getMainLooper()
                Handler(mainLooper).post { task() }
            }
        }

        override fun cancelTask() {
            _timerTask.cancel()
        }
    }

    private class PeriodicExecutionTask(
        id: String,
        val interval: Duration,
        val task: () -> Unit): ScheduledTask(id) {

        private lateinit var _timerTask: TimerTask

        init {
            scheduleTask()
        }

        override fun scheduleTask() {
            val periodInMilliseconds = interval.toLongMilliseconds()
            _timerTask =
                Timer().schedule(delay = periodInMilliseconds, period = periodInMilliseconds) {
                    val mainLooper = Looper.getMainLooper()
                    Handler(mainLooper).post { task() }
                }
        }

        override fun cancelTask() {
            _timerTask.cancel()
        }
    }
}
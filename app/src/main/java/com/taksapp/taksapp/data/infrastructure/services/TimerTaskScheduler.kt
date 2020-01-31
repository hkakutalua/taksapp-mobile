package com.taksapp.taksapp.data.infrastructure.services

import android.os.Handler
import android.os.Looper
import com.taksapp.taksapp.domain.interfaces.TaskScheduler
import org.joda.time.DateTime
import java.util.*
import kotlin.concurrent.schedule

class TimerTaskScheduler : TaskScheduler {
    private val tasks = mutableListOf<ScheduledTask>()

    override fun schedule(date: DateTime, task: () -> Unit): String {
        val scheduledTask = ScheduledTask(UUID.randomUUID().toString(), date, task)
        tasks.add(scheduledTask)
        return scheduledTask.id
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

    private class ScheduledTask(
        val id: String,
        val scheduledDate: DateTime,
        val task: () -> Unit) {

        private var _timerTask: TimerTask
        private var cancelled = false

        var paused: Boolean = false
            set(value) {
                if (cancelled)
                    return

                if (value) {
                    _timerTask.cancel()
                } else {
                    _timerTask = scheduleTask()
                }

                field = value
            }

        init {
            _timerTask = scheduleTask()
        }

        fun cancel() {
            _timerTask.cancel()
            cancelled = true
        }

        private fun scheduleTask(): TimerTask {
            return Timer().schedule(scheduledDate.toDate()) {
                val mainLooper = Looper.getMainLooper()
                Handler(mainLooper).post { task() }
            }
        }
    }
}
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
        val timerTask = Timer().schedule(date.toDate()) {
            val mainLooper = Looper.getMainLooper()
            Handler(mainLooper).post { task() }
        }

        val taskId = UUID.randomUUID().toString()
        tasks.add(ScheduledTask(taskId, timerTask))
        return taskId
    }

    override fun cancel(taskIdentifier: String) {
        val taskToCancel = tasks.firstOrNull { x -> x.id == taskIdentifier }
        taskToCancel?.let {
            it.timerTask.cancel()
            tasks.remove(taskToCancel)
        }
    }

    private class ScheduledTask(val id: String, val timerTask: TimerTask)
}
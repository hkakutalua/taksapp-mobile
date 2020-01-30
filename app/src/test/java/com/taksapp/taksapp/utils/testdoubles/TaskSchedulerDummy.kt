package com.taksapp.taksapp.utils.testdoubles

import com.taksapp.taksapp.domain.interfaces.TaskScheduler
import org.joda.time.DateTime
import org.junit.Assert
import java.util.*

class TaskSchedulerDummy : TaskScheduler {
    private val scheduledTasks = mutableListOf<ScheduledTask>()

    override fun schedule(date: DateTime, task: () -> Unit): String {
        val taskIdentifier = UUID.randomUUID().toString()
        scheduledTasks.add(ScheduledTask(taskIdentifier, date, task))
        return taskIdentifier
    }

    override fun cancel(taskIdentifier: String) {
        val task = scheduledTasks.firstOrNull { x -> x.identifier == taskIdentifier }
        task?.markAsCancelled()
    }

    fun executePendingTasks() {
        scheduledTasks.forEach { task -> task.action() }
        scheduledTasks.clear()
    }

    fun assertThatHasCancelledTask() {
        Assert.assertTrue(scheduledTasks.any { x -> x.cancelled })
    }

    class ScheduledTask(val identifier: String, val date: DateTime, val action: () -> Unit) {
        private var _cancelled = false
        val cancelled: Boolean
            get() = _cancelled

        fun markAsCancelled() {
            _cancelled = true
        }
    }
}
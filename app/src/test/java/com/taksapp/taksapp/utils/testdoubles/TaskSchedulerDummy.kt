package com.taksapp.taksapp.utils.testdoubles

import com.taksapp.taksapp.domain.interfaces.TaskScheduler
import org.joda.time.DateTime
import org.junit.Assert
import java.util.*

class TaskSchedulerDummy : TaskScheduler {
    private val scheduledTasks = mutableListOf<ScheduledTask>()

    override fun schedule(date: DateTime, task: () -> Unit): String {
        val taskIdentifier = UUID.randomUUID().toString()
        scheduledTasks.add(ScheduledTask(taskIdentifier, task))
        return taskIdentifier
    }

    override fun pause(taskIdentifier: String) {
        val task = scheduledTasks.firstOrNull { x -> x.identifier == taskIdentifier }
        task?.pause()
    }

    override fun resume(taskIdentifier: String) {
        val task = scheduledTasks.firstOrNull { x -> x.identifier == taskIdentifier }
        task?.resume()
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

    fun assertThatHasPausedTask() {
        Assert.assertTrue(scheduledTasks.any { x -> x.paused })
    }

    fun assertThatHasResumedTask() {
        Assert.assertTrue(scheduledTasks.any { x -> !x.paused })
    }

    class ScheduledTask(val identifier: String, val action: () -> Unit) {
        private var _cancelled = false
        private var _paused = false

        val cancelled get() = _cancelled
        val paused get() = _paused

        fun markAsCancelled() {
            _cancelled = true
        }

        fun pause() {
            _paused = true
        }

        fun resume() {
            _paused = false
        }
    }
}
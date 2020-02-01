package com.taksapp.taksapp.utils.testdoubles

import com.taksapp.taksapp.domain.interfaces.TaskScheduler
import org.joda.time.DateTime
import org.junit.Assert
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
class TaskSchedulerDummy : TaskScheduler {
    private val scheduledTasks = mutableListOf<ScheduledTask>()

    override fun schedule(id: String, date: DateTime, task: () -> Unit) {
        scheduledTasks.add(ScheduledTask(id, task))
    }

    override fun schedule(id: String, interval: Duration, task: () -> Unit) {
        scheduledTasks.add(ScheduledTask(id, task))
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

    fun executePendingTask(id: String) {
        val task = scheduledTasks.firstOrNull { task -> !task.cancelled && task.identifier == id }
        task?.action?.invoke()
    }

    fun assertThatHasCancelledTask(id: String) {
        Assert.assertTrue(scheduledTasks.any { x -> x.identifier == id && x.cancelled })
    }

    fun assertThatHasPausedTask(id: String) {
        Assert.assertTrue(scheduledTasks.any { x -> x.identifier == id && x.paused })
    }

    fun assertThatHasResumedTask(id: String) {
        Assert.assertTrue(scheduledTasks.any { x -> x.identifier == id && x.resumed })
    }

    class ScheduledTask(val identifier: String, val action: () -> Unit) {
        private var _cancelled = false
        private var _paused = false
        private var _resumed = true

        val cancelled get() = _cancelled
        val paused get() = _paused
        val resumed get() = _resumed

        fun markAsCancelled() {
            _cancelled = true
        }

        fun pause() {
            _paused = true
            _resumed = !_paused
        }

        fun resume() {
            _paused = false
            _resumed = !_paused
        }
    }
}
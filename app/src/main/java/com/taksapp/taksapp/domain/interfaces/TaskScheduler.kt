package com.taksapp.taksapp.domain.interfaces

import org.joda.time.DateTime
import org.joda.time.Period
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
interface TaskScheduler {
    /**
     * Schedule an one-time execution task.
     * The task will be executed on the main thread.
     * @param id an unique identifier for the task
     * @param date scheduled for the execution of the task
     * @param task to be executed on the scheduled [date]
     */
    fun schedule(id: String, date: DateTime, task: () -> Unit)

    /**
     * Schedule a repeated execution task.
     * The task will be executed on the main thread.
     * @param id an unique identifier for the task
     * @param interval between each execution
     * @param task to be executed
     */
    fun schedule(id: String, interval: Duration, task: () -> Unit)

    /**
     * Pauses a scheduled task execution.
     * @param taskIdentifier the unique task identifier
     */
    fun pause(taskIdentifier: String)

    /**
     * Resumes a scheduled task execution.
     * The task will be immediately executed if its scheduled date passes.
     * @param taskIdentifier the unique task identifier
     */
    fun resume(taskIdentifier: String)

    /**
     * Cancels a task identified with an unique [taskIdentifier]
     * Passing a non-registered identifier will do nothing
     * @param taskIdentifier the unique task identifier
     */
    fun cancel(taskIdentifier: String)
}
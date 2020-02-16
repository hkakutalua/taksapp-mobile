package com.taksapp.taksapp.ui.utils

import android.os.CountDownTimer

typealias onTickAlias = (remainingSeconds: Long) -> Unit

class CountdownCreator {
    private val countdowns = mutableListOf<Countdown>()

    fun createCountdown(
        id: String,
        timeUntilFinishedInSeconds: Long,
        onTick: onTickAlias) {

        countdowns.add(Countdown(id, timeUntilFinishedInSeconds, onTick))
    }

    fun pauseCountdown(id: String) {
        val countdown = countdowns.firstOrNull { x -> x.id == id }
        countdown?.pause()
    }

    fun resumeCountdown(id: String) {
        val countdown = countdowns.firstOrNull { x -> x.id == id }
        countdown?.resume()
    }

    private class Countdown(
        val id: String,
        timeUntilFinishedInSeconds: Long,
        private val onTick: onTickAlias
    ) {
        private var countdownTimer: CountDownTimer? = null
        private var countDownRemainingSeconds: Long = 0

        init {
            startTimer(timeUntilFinishedInSeconds, onTick)
        }

        fun pause() {
            countdownTimer?.cancel()
        }

        fun resume() {
            val oneSecondInMillis = 1000L
            val remainingMilliseconds = countDownRemainingSeconds * oneSecondInMillis

            startTimer(remainingMilliseconds, onTick)
        }

        private fun startTimer(
            timeUntilFinishedInSeconds: Long,
            onTick: onTickAlias
        ) {
            val oneSecondInMillis = 1000L
            val timeUntilFinishedInMillis = timeUntilFinishedInSeconds * oneSecondInMillis

            countdownTimer = object : CountDownTimer(timeUntilFinishedInMillis, oneSecondInMillis) {

                override fun onTick(millisUntilFinished: Long) {
                    countDownRemainingSeconds = millisUntilFinished / oneSecondInMillis
                    onTick(countDownRemainingSeconds)
                }

                override fun onFinish() {}
            }

            countdownTimer?.start()
        }
    }
}

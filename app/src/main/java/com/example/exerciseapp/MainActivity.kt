package com.example.exerciseapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat

class MainActivity : AppCompatActivity() {

    private lateinit var workoutDurationEditText: EditText
    private lateinit var restDurationEditText: EditText
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var workoutTimerTextView: TextView
    private lateinit var restTimerTextView: TextView
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var vibrator: Vibrator
    private lateinit var workoutNameEditText: EditText

    private var workoutTimer: CountDownTimer? = null
    private var restTimer: CountDownTimer? = null
    private var isWorkoutTimerRunning = false
    private var isRestTimerRunning = false

    private var workoutDuration: Long = 0
    private var restDuration: Long = 0

    private var workoutTimeRemaining: Long = 0
    private var restTimeRemaining: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        workoutDurationEditText = findViewById(R.id.workoutDurationEditText)
        restDurationEditText = findViewById(R.id.restDurationEditText)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        progressBar = findViewById(R.id.progressBar)
        workoutTimerTextView = findViewById(R.id.workoutTimeTextView)
        restTimerTextView = findViewById(R.id.restTimeTextView)
        workoutNameEditText = findViewById(R.id.workoutNameEditText)
        mediaPlayer = MediaPlayer.create(this, R.raw.notification_sound)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator


        startButton.setOnClickListener {
            startTimer()
        }

        stopButton.setOnClickListener {
            stopTimer()
        }
    }

    private fun startTimer() {
        if (!isWorkoutTimerRunning && !isRestTimerRunning) {
            val workoutDurationInput = workoutDurationEditText.text.toString().toLongOrNull()
            val restDurationInput = restDurationEditText.text.toString().toLongOrNull()

            if (workoutDurationInput != null && restDurationInput != null) {
                workoutDuration = workoutDurationInput * 1000 // convert to milliseconds
                restDuration = restDurationInput * 1000 // convert to milliseconds

                startButton.isEnabled = false
                workoutDurationEditText.isEnabled = false
                restDurationEditText.isEnabled = false
                stopButton.visibility = View.VISIBLE

                startWorkoutTimer()
            }
        }
    }

    private fun startWorkoutTimer() {
        workoutTimer = object : CountDownTimer(workoutDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                workoutTimeRemaining = millisUntilFinished
                updateWorkoutTimerUI()
            }

            override fun onFinish() {
                workoutTimeRemaining = 0
                updateWorkoutTimerUI()
                startRestTimer()

                // Show notification
                showNotification("Workout Timer", "Workout time is up!")

                // Play sound or vibrate the device
                playSound()
                vibrateDevice()
            }
        }.start()

        isWorkoutTimerRunning = true
    }

    private fun playSound() {
        mediaPlayer.start()
    }

    private fun vibrateDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            this.vibrator.vibrate(500)
        }
    }


    private fun startRestTimer() {
        restTimer = object : CountDownTimer(restDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                restTimeRemaining = millisUntilFinished
                updateRestTimerUI()
            }

            override fun onFinish() {
                restTimeRemaining = 0
                updateRestTimerUI()

                // Show notification
                showNotification("Rest Timer", "Rest time is up!")

                // Play sound or vibrate the device
                playSound()
                vibrateDevice()
                // Add code for any additional logic after the rest period ends
            }
        }.start()

        isRestTimerRunning = true
    }

    private fun showNotification(title: String, message: String) {
        val workoutName = workoutNameEditText.text.toString()
        val contentText = if (workoutName.isNotEmpty()) {
            "$workoutName: $message"
        } else {
            message
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "timer_channel"
        val channelName = "Timer Channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
        notificationManager.notify(0, notificationBuilder.build())
    }


    private fun stopTimer() {
        workoutTimer?.cancel()
        restTimer?.cancel()

        isWorkoutTimerRunning = false
        isRestTimerRunning = false

        workoutDurationEditText.isEnabled = true
        restDurationEditText.isEnabled = true
        startButton.isEnabled = true
        stopButton.visibility = View.GONE

        resetTimerUI()
    }

    private fun updateWorkoutTimerUI() {
        val seconds = workoutTimeRemaining / 1000
        workoutTimerTextView.text = String.format("%02d:%02d", seconds / 60, seconds % 60)

        val progress = ((workoutDuration - workoutTimeRemaining) * 100 / workoutDuration).toInt()
        progressBar.progress = progress
    }

    private fun updateRestTimerUI() {
        val seconds = restTimeRemaining / 1000
        restTimerTextView.text = String.format("%02d:%02d", seconds / 60, seconds % 60)

        val progress = ((restDuration - restTimeRemaining) * 100 / restDuration).toInt()
        progressBar.progress = progress
    }

    private fun resetTimerUI() {
        workoutTimerTextView.text = "Workout Timer"
        restTimerTextView.text = "Rest Timer"
        progressBar.progress = 0
    }
}



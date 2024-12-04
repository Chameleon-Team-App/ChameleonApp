package com.example.mainchameleon.ui.mentalhealth

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mainchameleon.R

class TimerFragment : Fragment() {

    private lateinit var timerText: TextView
    private var countDownTimer: CountDownTimer? = null
    private var timeRemaining: Long = 60000 // 1 minute in milliseconds

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_mental_minute, container, false)

        // Get reference to the TextView
        timerText = view.findViewById(R.id.timerText)

        // Initialize the CountDownTimer
        countDownTimer = object : CountDownTimer(timeRemaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the timer display every second
                timeRemaining = millisUntilFinished
                updateTimerDisplay()
            }

            override fun onFinish() {
                // When the timer finishes
                timerText.text = "Done! Hope you feel Well Rested"
            }
        }

        // Start the timer when the fragment is created
        countDownTimer?.start()

        return view
    }

    // Method to update the timer display
    private fun updateTimerDisplay() {
        val seconds = (timeRemaining / 1000 % 60).toInt()
        val minutes = (timeRemaining / 1000 / 60).toInt()

        val formattedTime = String.format("%02d:%02d", minutes, seconds)
        timerText.text = formattedTime
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cancel the timer when the fragment is destroyed
        countDownTimer?.cancel()
    }
}
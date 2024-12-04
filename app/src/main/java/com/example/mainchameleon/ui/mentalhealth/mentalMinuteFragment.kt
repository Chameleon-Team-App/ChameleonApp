package com.example.mainchameleon.ui.mentalhealth

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mainchameleon.R

class MentalMinuteFragment : Fragment() {

    private lateinit var timerTextView: TextView
    private var countDownTimer: CountDownTimer? = null
    private var timeRemaining: Long = 60000 // 1 minute in milliseconds

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_mental_minute, container, false)

        timerTextView = rootView.findViewById(R.id.timerText)

        countDownTimer = object : CountDownTimer(timeRemaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                updateTimerDisplay()
            }

            override fun onFinish() {
                timerTextView.text = "Done! Hope you feel Well Rested"
            }
        }
        countDownTimer?.start()

        return rootView
    }

    private fun updateTimerDisplay() {
        val seconds = (timeRemaining / 1000 % 60).toInt()
        val minutes = (timeRemaining / 1000 / 60).toInt()
        timerTextView.text = String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}
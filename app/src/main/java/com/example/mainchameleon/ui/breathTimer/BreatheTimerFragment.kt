package com.example.mainchameleon.ui.breathtimer

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mainchameleon.R

class BreathTimerFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var breathTextView: TextView
    private lateinit var doneButton: Button
    private lateinit var button2: Button
    private var currentState = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.activity_breath_timer, container, false)
        currentState = 0;
        progressBar = rootView.findViewById(R.id.progressBar)
        breathTextView = rootView.findViewById(R.id.breathTextView)
        doneButton = rootView.findViewById(R.id.Donebutton)
        button2 = rootView.findViewById(R.id.button2)

        startBreathingActivity()

        button2.setOnClickListener {
            requireActivity().onBackPressed() // Go back to the previous screen
        }
        doneButton.setOnClickListener{
            startBreathingActivity()
        }

        return rootView
    }

    private fun startBreathingActivity() {
        object : CountDownTimer(5000, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                progressBar.progress = (millisUntilFinished / 50).toInt()
            }

            override fun onFinish() {
                currentState = (currentState + 1) % 4
                breathTextView.text = when (currentState) {
                    0 -> "Breathe In"
                    1, 3 -> "Hold"
                    2 -> "Exhale"
                    else -> ""
                }
                startBreathingActivity()
            }
        }.start()
    }
}
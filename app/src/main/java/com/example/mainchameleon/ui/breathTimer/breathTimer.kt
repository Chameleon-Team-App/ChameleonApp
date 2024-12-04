package com.example.mainchameleon.ui.breathTimer

import android.os.Bundle
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.example.mainchameleon.R
import com.example.mainchameleon.databinding.FragmentJournalBinding
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import android.os.Handler
import android.widget.Toast

class BreathTimer : AppCompatActivity() {
    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!
    private var breatheText1 = "Inhale"
    private var start = 0;
    private var timeleft = 0;
    private var currentstate = 0;
    private lateinit var ProgressBar: ProgressBar
    private lateinit var breatheTextView: TextView
    private lateinit var exitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProgressBar = findViewById(R.id.progressBar)
        breatheTextView = findViewById(R.id.breathTextView)
        exitButton = findViewById(R.id.button2)

        ProgressBar.visibility = View.VISIBLE
        enableEdgeToEdge()
        setContentView(R.layout.activity_breath_timer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //startBreathingActivity()
    }

    private fun startTimer() {
        val handler = Handler(mainLooper)
        val runnable = Runnable {
            // Task to be executed after delay
            currentstate %= 4
            if(currentstate == 0) {breatheText1 = "Breathe In"}
            if(currentstate == 1 || currentstate == 3) {breatheText1 = "Hold"}
            if (currentstate == 2) {breatheText1 = "Exhale"}
        }

        // Delay time (in milliseconds), e.g., 5000 ms = 5 seconds
        handler.postDelayed(runnable, 5000)
    }


    private fun startBreathingActivity(){
        object : CountDownTimer(4000, 1000) { // 10 seconds, with 1 second interval

            override fun onTick(millisUntilFinished: Long) {
                // Update the TextView every second
                //breatheTextView.text = "Time remaining: ${millisUntilFinished / 1000} seconds"
                ProgressBar.setProgress(100 - (millisUntilFinished/40).toInt())
            }

            override fun onFinish() {
                // Action to take when the timer finishes
                currentstate++
                currentstate %= 4
                if(currentstate == 0) {breatheTextView.text = "Breathe In"}
                if(currentstate == 1 || currentstate == 3) {breatheTextView.text = "Hold"}
                if (currentstate == 2) {breatheTextView.text = "Exhale"}
                startBreathingActivity()
            }
        }.start()
    }
}
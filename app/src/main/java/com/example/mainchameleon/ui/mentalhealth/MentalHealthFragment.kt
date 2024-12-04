package com.example.mainchameleon.ui.mentalhealth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mainchameleon.R
import com.google.android.material.card.MaterialCardView

class MentalHealthFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_mental_health, container, false)

        // Back Button
        val backButton = rootView.findViewById<MaterialCardView>(R.id.back_button)
        backButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_dashboard)
        }

        // Timer Button
        rootView.findViewById<Button>(R.id.timerButton).setOnClickListener {
            findNavController().navigate(R.id.action_navigation_mental_health_to_mentalMinuteFragment)
        }

        // Encouraging Phrase Button
        rootView.findViewById<Button>(R.id.encouragingPhraseButton).setOnClickListener {
            findNavController().navigate(R.id.encouraging_fragment)
        }

        // Achievements Button
        rootView.findViewById<Button>(R.id.achievementsButton).setOnClickListener {
            // Navigate to Achievements Screen (implement when ready)
        }

        // Breathing Exercise Button
        rootView.findViewById<Button>(R.id.breathingExerciseButton).setOnClickListener {
            findNavController().navigate(R.id.action_navigation_mental_health_to_breathTimerFragment)
        }

        return rootView
    }
}
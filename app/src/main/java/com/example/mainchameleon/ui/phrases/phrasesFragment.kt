package com.example.mainchameleon.ui.phrases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mainchameleon.R

class PhrasesFragment : Fragment() {

    // List of encouraging quotes
    private val quotes = listOf(
        "The best way to predict the future is to create it. – Abraham Lincoln",
        "You miss 100% of the shots you don't take. – Wayne Gretzky",
        "It always seems impossible until it's done. – Nelson Mandela",
        "What we think, we become. – Buddha",
        "Success is not how high you have climbed, but how you make a positive difference to the world. – Roy T. Bennett",
        "Don’t watch the clock; do what it does. Keep going. – Sam Levenson",
        "The only limit to our realization of tomorrow is our doubts of today. – Franklin D. Roosevelt",
        "Hardships often prepare ordinary people for an extraordinary destiny. – C.S. Lewis",
        "It’s not whether you get knocked down, it’s whether you get up. – Vince Lombardi",
        "Believe in yourself and all that you are. Know that there is something inside you that is greater than any obstacle. – Christian D. Larson",
        "Success is not in what you have, but who you are. – Bo Bennett",
        "Everything you can imagine is real. – Pablo Picasso",
        "Start where you are. Use what you have. Do what you can. – Arthur Ashe",
        "The only way to do great work is to love what you do. – Steve Jobs",
        "Don’t stop when you’re tired. Stop when you’re done. – Unknown",
        "What lies behind us and what lies before us are tiny matters compared to what lies within us. – Ralph Waldo Emerson",
        "Be the change that you wish to see in the world. – Mahatma Gandhi",
        "Act as if what you do makes a difference. It does. – William James",
        "A journey of a thousand miles begins with a single step. – Lao Tzu",
        "The only thing standing between you and your goal is the story you keep telling yourself as to why you can’t achieve it. – Jordan Belfort"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_encouraging, container, false)

        // Find views by ID
        val backButton: Button = view.findViewById(R.id.back_button)
        val centerText: TextView = view.findViewById(R.id.center_text)

        // Set a random quote when the fragment is created
        val randomQuote = quotes.random()
        centerText.text = randomQuote

        // Set a click listener for the back button
        backButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_mental_health)
        }

        return view
    }
}

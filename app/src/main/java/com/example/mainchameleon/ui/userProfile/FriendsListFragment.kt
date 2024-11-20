package com.example.mainchameleon.ui.userProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mainchameleon.R

class FriendsListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_friends_lists, container, false)

        // Set up back button to navigate to the profile
        val backButton: ImageButton = view.findViewById(R.id.back_button)
        backButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_profile)
        }

        return view
    }
}

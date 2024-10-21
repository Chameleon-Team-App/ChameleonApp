package com.example.mainchameleon.ui.journal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mainchameleon.R
import com.example.mainchameleon.databinding.FragmentHomeBinding

class JournalFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_journal, container, false)
        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
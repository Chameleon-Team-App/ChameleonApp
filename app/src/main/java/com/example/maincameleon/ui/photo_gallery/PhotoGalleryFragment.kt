package com.example.maincameleon.ui.photo_gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.maincameleon.R
import com.example.maincameleon.databinding.FragmentGalleryBinding

class PhotoGalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val photoGalleryViewModel =
            ViewModelProvider(this).get(PhotoGalleryViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textGallery
        photoGalleryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        // Find the button and set an OnClickListener
        val button: Button = binding.button2 // Assuming button2 is your button ID
        button.setOnClickListener {
            findNavController().navigate(R.id.action_photoGalleryFragment_to_cameraFragment)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.maincameleon.ui.photo_gallery

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.maincameleon.R
import com.example.maincameleon.databinding.FragmentGalleryBinding
import com.example.maincameleon.ui.camera.CameraViewModel

class PhotoGalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Get the shared ViewModel
        val photoGalleryViewModel = ViewModelProvider(requireActivity())[CameraViewModel::class.java]

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textGallery
        photoGalleryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        // Observe the photo list
        photoGalleryViewModel.photos.observe(viewLifecycleOwner) { photos ->
            binding.imageContainer.removeAllViews() // Clear old views
            for (photoPath in photos) {
                val imageView = ImageView(requireContext())
                imageView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                imageView.setImageBitmap(BitmapFactory.decodeFile(photoPath))
                binding.imageContainer.addView(imageView)
            }
        }

        // Find the button and set an OnClickListener
        val button: Button = binding.button2
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

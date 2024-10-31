package com.example.mainchameleon.ui.photo_gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController // Import this to use navigation
import com.bumptech.glide.Glide
import com.example.mainchameleon.R // Make sure you import R for navigation
import com.example.mainchameleon.databinding.FragmentPhotoGalleryBinding
import com.example.mainchameleon.ui.camera.CameraViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class PhotoGalleryFragment : Fragment() {

    private var _binding: FragmentPhotoGalleryBinding? = null
    private val binding get() = _binding!!
    private lateinit var photoGalleryViewModel: CameraViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoGalleryBinding.inflate(inflater, container, false)

        photoGalleryViewModel = ViewModelProvider(requireActivity()).get(CameraViewModel::class.java)

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            photoGalleryViewModel.fetchPhotosFromFirebase(it.uid)
        }

        // Add the observer to display images
        photoGalleryViewModel.photos.observe(viewLifecycleOwner, { photoUrls ->
            displayImages(photoUrls)
        })

        // Add a click listener for button2 to navigate to the camera
        binding.button2.setOnClickListener {
            findNavController().navigate(R.id.action_photoGalleryFragment_to_cameraFragment)
        }

        return binding.root
    }

    private fun displayImages(photoUrls: List<String>) {
        val imageContainer: LinearLayout = binding.imageContainer
        imageContainer.removeAllViews()

        for (url in photoUrls) {
            val imageView = ImageView(requireContext())
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                500 // Set the height of the image
            )
            layoutParams.setMargins(8, 8, 8, 8)
            imageView.layoutParams = layoutParams

            // Use Glide to load the image from Firebase URL
            Glide.with(this).load(url).into(imageView)
            imageContainer.addView(imageView)

            // Add delete button for each image
            val deleteButton = Button(requireContext())
            deleteButton.text = "Delete"
            deleteButton.setOnClickListener {
                deletePhoto(url) // Pass the download URL, but extract correct file path inside deletePhoto
            }
            imageContainer.addView(deleteButton)
        }
    }

    private fun deletePhoto(photoUrl: String) {
        // Firebase Storage references should be based on the path, not the download URL
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val storageRef = FirebaseStorage.getInstance().reference
            val fileName = getFileNameFromUrl(photoUrl)  // Function to extract the file name from the URL
            val userPhotoRef = storageRef.child("users/${it.uid}/photos/$fileName")

            userPhotoRef.delete()
                .addOnSuccessListener {
                    // Remove the photo from the view model
                    photoGalleryViewModel.deletePhoto(photoUrl)
                }
                .addOnFailureListener { e ->
                    // Handle failure (e.g., log the error)
                    e.printStackTrace()
                }
        }
    }

    // Utility function to extract file name from Firebase Storage URL
    private fun getFileNameFromUrl(photoUrl: String): String {
        return photoUrl.substringAfterLast("%2F").substringBefore("?alt")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

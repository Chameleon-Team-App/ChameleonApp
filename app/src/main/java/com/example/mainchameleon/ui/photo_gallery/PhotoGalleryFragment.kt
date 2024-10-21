package com.example.mainchameleon.ui.photo_gallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mainchameleon.R
import com.example.mainchameleon.databinding.FragmentGalleryBinding
import com.example.mainchameleon.ui.camera.CameraViewModel
import java.io.IOException

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

        // Observe the text
        val textView = binding.textGallery
        photoGalleryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        // Observe the photo list and load images with correct orientation
        photoGalleryViewModel.photos.observe(viewLifecycleOwner) { photos ->
            binding.imageContainer.removeAllViews() // Clear old views
            for (photoPath in photos) {
                val imageView = ImageView(requireContext())
                imageView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                // Set padding in dp
                val paddingInDp = 16 // Example: 16dp padding
                val paddingInPixels = dpToPx(paddingInDp)
                imageView.setPadding(paddingInPixels, paddingInPixels, paddingInPixels, paddingInPixels)

                // Load the bitmap and correct orientation
                val rotatedBitmap = getRotatedBitmap(photoPath)
                imageView.setImageBitmap(rotatedBitmap)
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

    // Helper function to get the rotated bitmap
    private fun getRotatedBitmap(photoPath: String): Bitmap? {
        val bitmap = BitmapFactory.decodeFile(photoPath)
        return try {
            val exif = ExifInterface(photoPath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )
            val matrix = Matrix()

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }

            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: IOException) {
            e.printStackTrace()
            bitmap
        }
    }

    // Helper function to convert dp to pixels
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
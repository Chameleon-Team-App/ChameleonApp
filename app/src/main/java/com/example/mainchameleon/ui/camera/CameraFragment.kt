package com.example.mainchameleon.ui.camera

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.example.mainchameleon.MainActivity
import com.example.mainchameleon.databinding.FragmentCameraBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService

    private val TAG = "CameraFragment"
    private var source: String? = null // Variable to hold the source

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        source = arguments?.getString("source") // Retrieve the source argument

        hideBottomNav() // Hide the bottom navigation bar

        if (allPermissionsGranted()) {
            startCamera()
        }

        binding.cameraCaptureButton.setOnClickListener {
            takePhoto()
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
        showBottomNav() // Show the bottom navigation bar when exiting the fragment
    }

    private fun hideBottomNav() {
        (activity as? MainActivity)?.navView?.visibility = View.GONE
    }

    private fun showBottomNav() {
        (activity as? MainActivity)?.navView?.visibility = View.VISIBLE
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(
            requireContext().externalMediaDirs.firstOrNull(),
            "${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    uploadCapturedImageToFirebase(savedUri)
                }
            }
        )
    }

    private fun uploadCapturedImageToFirebase(photoUri: Uri) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val storageRef = FirebaseStorage.getInstance().reference
            val userPhotoRef = storageRef.child("Users/${user.uid}/photos/${UUID.randomUUID()}.jpg")

            userPhotoRef.putFile(photoUri)
                .addOnSuccessListener {
                    userPhotoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        if (source == "register") {
                            // Navigate back to registerScreen and pass the photo URL
                            parentFragmentManager.setFragmentResult("photoResult", Bundle().apply {
                                putString("photoUrl", downloadUri.toString())
                            })
                            requireActivity().finish() // Close CameraFragment and go back to registerScreen
                        } else if (source == "journal") {
                            // Navigate back to JournalFragment and pass the photo URL
                            setFragmentResult("photoResult", Bundle().apply {
                                putString("photoUrl", downloadUri.toString())
                            })
                            findNavController().navigateUp() // Go back to JournalFragment
                        }

                        Toast.makeText(requireContext(), "Photo captured and uploaded successfully", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to upload captured image", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
}

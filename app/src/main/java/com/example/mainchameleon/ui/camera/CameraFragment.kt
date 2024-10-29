package com.example.mainchameleon.ui.camera

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mainchameleon.R
import com.example.mainchameleon.databinding.FragmentCameraBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraViewModel: CameraViewModel

    private val TAG = "CameraFragment"
    private val REQUEST_CODE_PERMISSIONS = 10
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        
        // Initialize ViewModel
        // .get was deprecated -> cameraViewModel = ViewModelProvider(requireActivity()).get(CameraViewModel::class.java)
        cameraViewModel = ViewModelProvider(requireActivity())[CameraViewModel::class.java]

        // Check if permissions are granted
        if (allPermissionsGranted()) {
            startCamera() // Start the camera only when permissions are granted
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        // Set up the capture button listener
        binding.cameraCaptureButton.setOnClickListener {
            takePhoto() // Capture photo when the button is clicked
        }

        // Set up the back button listener
        binding.backButton.setOnClickListener {
            findNavController().navigateUp() // Navigate back to the previous fragment
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        return binding.root
    }

    // Initialize and start the camera preview
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Set up the camera preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            // Select the back camera as the default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind previous use cases before rebinding
                cameraProvider.unbindAll()

                // Bind the camera to lifecycle, and also bind the image capture and preview use cases
                imageCapture = ImageCapture.Builder().build()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    // Capture and save the photo
    private fun takePhoto() {
        // Get a stable reference to the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create output file to hold the image
        val photoFile = File(
            requireContext().externalMediaDirs.firstOrNull(),
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after the photo has been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Toast.makeText(requireContext(), "Photo saved: $savedUri", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Photo capture succeeded: $savedUri")

                    // Upload photo to Firebase Storage
                    uploadPhotoToFirebaseStorage(photoFile)
                }
            }
        )
    }

    // Function to upload the photo to Firebase Storage
    private fun uploadPhotoToFirebaseStorage(photoFile: File) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // Get a reference to the Firebase storage
            val storageRef = FirebaseStorage.getInstance().reference
            val userPhotoRef = storageRef.child("users/${user.uid}/photos/${photoFile.name}")

            // Upload the file
            userPhotoRef.putFile(Uri.fromFile(photoFile))
                .addOnSuccessListener {
                    // File successfully uploaded
                    userPhotoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        Log.d(TAG, "File successfully uploaded. Download URL: $downloadUri")
                        // Save the download URL to ViewModel (or Firebase Database/Firestore)
                        cameraViewModel.addPhoto(downloadUri.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "File upload failed: ${exception.message}")
                }
        } else {
            Toast.makeText(requireContext(), "User not authenticated.", Toast.LENGTH_SHORT).show()
        }
        return mediaDir ?: requireContext().filesDir
    }

    // Check if all permissions are granted
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    // Handle the result of permission requests
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera() // Start camera if permissions are granted
            } else {
                Toast.makeText(requireContext(), "Camera permission not granted", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}
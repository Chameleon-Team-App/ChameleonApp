package com.example.mainchameleon.ui.camera

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.camera.core.*
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
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraSelector: CameraSelector // Declare cameraSelector as a field

    private val TAG = "CameraFragment"
    private var source: String? = null // Variable to hold the source

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        source = arguments?.getString("source") // Retrieve the source argument



        if (allPermissionsGranted()) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permissions not granted.", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
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
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val rotation = binding.viewFinder.display.rotation

            imageCapture = ImageCapture.Builder()
                .setTargetRotation(rotation)
                .build()

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

        // Create output options with metadata
        val metadata = ImageCapture.Metadata().apply {
            // Mirror image if using the front camera
            isReversedHorizontal = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
            .setMetadata(metadata)
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(requireContext(), "Photo capture failed", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    // Correct the image orientation
                    correctImageOrientation(photoFile.path)
                    uploadCapturedImageToFirebase(savedUri)
                }
            }
        )
    }

    private fun correctImageOrientation(imagePath: String) {
        try {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            val exif = ExifInterface(imagePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

            val rotatedBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipImage(bitmap, horizontal = true, vertical = false)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipImage(bitmap, horizontal = false, vertical = true)
                else -> bitmap
            }

            // Save the corrected bitmap back to the file
            val out = FileOutputStream(imagePath)
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error correcting image orientation", e)
        }
    }

    private fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun flipImage(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.preScale(
            if (horizontal) -1f else 1f,
            if (vertical) -1f else 1f
        )
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
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
                            val resultIntent = Intent().apply {
                                putExtra("photoUrl", downloadUri.toString())
                            }
                            requireActivity().setResult(Activity.RESULT_OK, resultIntent)
                            requireActivity().finish()
                        } else if (source == "journal") {
                            setFragmentResult("photoResult", Bundle().apply {
                                putString("photoUrl", downloadUri.toString())
                            })
                            findNavController().navigateUp()
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

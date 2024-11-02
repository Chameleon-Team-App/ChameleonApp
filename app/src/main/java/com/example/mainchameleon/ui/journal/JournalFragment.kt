package com.example.mainchameleon.ui.journal

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.example.mainchameleon.R
import com.example.mainchameleon.databinding.FragmentJournalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.InputStream
import java.util.*

class JournalFragment : Fragment() {

    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!
    private var photoUrl: String? = null
    private val TAG = "JournalFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJournalBinding.inflate(inflater, container, false)

        binding.openCameraButton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("source", "journal")
            }
            findNavController().navigate(R.id.navigation_camera, bundle)
        }


        binding.uploadFromGalleryButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }

        setFragmentResultListener("photoResult") { _, bundle ->
            photoUrl = bundle.getString("photoUrl")
            if (!photoUrl.isNullOrEmpty()) {
                binding.imageViewPlaceholder.visibility = View.VISIBLE
                Picasso.get().load(photoUrl).into(binding.imageViewPlaceholder)
            }
        }

        binding.saveJournalButton.setOnClickListener {
            saveJournalEntry()
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_REQUEST_CODE) {
            data?.data?.let { uri ->
                val rotatedBitmap = handleImageOrientation(uri)
                binding.imageViewPlaceholder.visibility = View.VISIBLE
                binding.imageViewPlaceholder.setImageBitmap(rotatedBitmap)
                uploadImageToFirebase(uri)
            }
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val storageRef = FirebaseStorage.getInstance().reference
            val userPhotoRef = storageRef.child("Users/${user.uid}/photos/${UUID.randomUUID()}.jpg")

            userPhotoRef.putFile(uri)
                .addOnSuccessListener {
                    userPhotoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        photoUrl = downloadUri.toString()
                        Log.d(TAG, "Image uploaded successfully: $photoUrl")
                        Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to upload image", exception)
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User not authenticated.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveJournalEntry() {
        val title = binding.titleEntryBox.text.toString().trim()
        val text = binding.journalEntryText.text.toString().trim()

        if (title.isEmpty() || text.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated.", Toast.LENGTH_SHORT).show()
            return
        }

        val journalEntry = JournalEntry(
            title = title,
            text = text,
            imageUrl = photoUrl,
            backgroundColor = JournalEntry.generateRandomColor(),
            userId = userId
        )

        val database = FirebaseDatabase.getInstance().reference
        val noteId = database.child("Users").child(userId).child("journals").push().key ?: UUID.randomUUID().toString()

        database.child("Users").child(userId).child("journals").child(noteId).setValue(journalEntry)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Journal entry saved successfully")
                    Toast.makeText(requireContext(), "Journal saved", Toast.LENGTH_SHORT).show()
                    clearJournalForm()
                } else {
                    Log.e(TAG, "Failed to save journal entry", task.exception)
                    Toast.makeText(requireContext(), "Failed to save journal", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun handleImageOrientation(uri: Uri): Bitmap? {
        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val exif = ExifInterface(requireContext().contentResolver.openInputStream(uri)!!)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            else -> bitmap
        }
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun clearJournalForm() {
        binding.titleEntryBox.text.clear()
        binding.journalEntryText.text.clear()
        binding.imageViewPlaceholder.visibility = View.GONE
        photoUrl = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val GALLERY_REQUEST_CODE = 1
    }
}

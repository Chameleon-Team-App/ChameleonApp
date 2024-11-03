package com.example.mainchameleon.ui.userProfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileViewModel : ViewModel() {

    private val _profileImageUri = MutableLiveData<Uri?>()
    val profileImageUri: LiveData<Uri?> = _profileImageUri

    private val _bio = MutableLiveData<String>()
    val bio: LiveData<String> = _bio

    private val _birthday = MutableLiveData<String>()
    val birthday: LiveData<String> = _birthday

    private val _uploadStatus = MutableLiveData<Boolean?>()
    val uploadStatus: LiveData<Boolean?> = _uploadStatus

    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    fun setProfileImageUri(uri: Uri?) {
        _profileImageUri.value = uri
    }

    fun setBio(bioText: String) {
        _bio.value = bioText
    }

    fun setBirthday(birthdayText: String) {
        _birthday.value = birthdayText
    }

    fun uploadProfilePicture(uri: Uri) {
        val userId = "yourUserIdHere"  // Replace with actual user ID retrieval logic
        val storageRef = storage.reference.child("Users/$userId/profilePictures/${uri.lastPathSegment}")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                _uploadStatus.value = true  // Set status to true when upload succeeds
                Log.d("ProfileViewModel", "Profile picture uploaded successfully.")
            }
            .addOnFailureListener { exception ->
                _uploadStatus.value = false  // Set status to false when upload fails
                Log.e("ProfileViewModel", "Failed to upload profile picture", exception)
            }
    }

    fun saveProfileDataToDatabase(userId: String, profileImageUrl: String, bio: String, birthday: String) {
        val userRef = database.getReference("Users").child(userId)

        val userMap = mapOf(
            "profilePictureUrl" to profileImageUrl,
            "bio" to bio,
            "birthday" to birthday
        )

        userRef.updateChildren(userMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("ProfileViewModel", "Profile data updated successfully.")
            } else {
                Log.e("ProfileViewModel", "Failed to update profile data: ${task.exception?.message}")
            }
        }
    }
}
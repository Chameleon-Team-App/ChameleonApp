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

    // LiveData properties for profile information
    private val _profileImageUri = MutableLiveData<Uri?>()
    val profileImageUri: LiveData<Uri?> = _profileImageUri

    private val _bio = MutableLiveData<String>()
    val bio: LiveData<String> = _bio

    private val _birthday = MutableLiveData<String>()
    val birthday: LiveData<String> = _birthday

    private val _uploadStatus = MutableLiveData<Boolean?>()
    val uploadStatus: LiveData<Boolean?> = _uploadStatus

    // Firebase instances
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    // Get current user ID
    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "Unknown"
    }

    // Add friend by code
    fun addFriendByCode(friendCode: String) {
        val currentUserId = getCurrentUserId()
        val databaseRef = database.reference
        databaseRef.child("users").child(currentUserId).child("friends").push().setValue(friendCode)
    }

    // Setters for profile information
    fun setProfileImageUri(uri: Uri?) {
        _profileImageUri.value = uri
    }

    fun setBio(bioText: String) {
        _bio.value = bioText
    }

    fun setBirthday(birthdayText: String) {
        _birthday.value = birthdayText
    }

    // Upload profile picture to Firebase Storage
    fun uploadProfilePicture(uri: Uri) {
        val userId = auth.currentUser?.uid ?: run {
            Log.e("ProfileViewModel", "User not authenticated")
            _uploadStatus.value = false
            return
        }

        val storageRef = storage.reference.child("Users/$userId/profilePictures/${System.currentTimeMillis()}.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    _uploadStatus.value = true
                    saveProfileDataToDatabase(profileImageUrl = downloadUri.toString(), bio = null) // Only update the picture
                    Log.d("ProfileViewModel", "Profile picture uploaded successfully.")
                }
            }
            .addOnFailureListener { exception ->
                _uploadStatus.value = false
                Log.e("ProfileViewModel", "Failed to upload profile picture", exception)
            }
    }

    // Save or update profile data to Firebase Database
    fun saveProfileDataToDatabase(profileImageUrl: String?, bio: String?) {
        val userId = getCurrentUserId()
        val userRef = database.reference.child("Users").child(userId)

        userRef.get().addOnSuccessListener { dataSnapshot ->
            val existingBio = dataSnapshot.child("bio").value as? String
            val existingProfilePictureUrl = dataSnapshot.child("profilePictureUrl").value as? String

            // Merge updates with existing data
            val updatedBio = bio ?: existingBio
            val updatedProfilePictureUrl = profileImageUrl ?: existingProfilePictureUrl

            val userMap = mutableMapOf<String, Any?>()
            updatedBio?.let { userMap["bio"] = it }
            updatedProfilePictureUrl?.let { userMap["profilePictureUrl"] = it }

            userRef.updateChildren(userMap).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ProfileViewModel", "Profile data updated successfully.")
                } else {
                    Log.e("ProfileViewModel", "Failed to update profile data: ${task.exception?.message}")
                }
            }
        }.addOnFailureListener {
            Log.e("ProfileViewModel", "Failed to fetch current profile data: ${it.message}")
        }
    }
}

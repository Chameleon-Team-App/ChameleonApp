package com.example.mainchameleon.ui.userProfile

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object UserProfileUtils {
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    fun uploadProfilePicture(
        context: Context,
        userId: String,
        profileImageUri: Uri,
        onComplete: (String) -> Unit
    ) {
        val storageRef: StorageReference = storage.reference.child("Users/$userId/profilePictures/${profileImageUri.lastPathSegment}")
        storageRef.putFile(profileImageUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    onComplete(downloadUri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to upload profile picture.", Toast.LENGTH_SHORT).show()
                onComplete("")
            }
    }
}
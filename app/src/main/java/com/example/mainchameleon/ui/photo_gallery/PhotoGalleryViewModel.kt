package com.example.mainchameleon.ui.photo_gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult

class PhotoGalleryViewModel : ViewModel() {

    private val _photos = MutableLiveData<List<String>>()
    val photos: LiveData<List<String>> = _photos

    init {
        _photos.value = emptyList() // Initialize with an empty list
    }

    fun fetchPhotosFromFirebase(userId: String) {
        val storageRef = FirebaseStorage.getInstance().reference.child("users/$userId/photos")

        storageRef.listAll()
            .addOnSuccessListener { result: ListResult ->
                val urls = mutableListOf<String>()
                for (fileRef in result.items) {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        urls.add(uri.toString())
                        _photos.value = urls
                    }.addOnFailureListener {
                        // Handle any errors
                    }
                }
            }.addOnFailureListener {
                // Handle failure to fetch files
            }
    }
}

package com.example.mainchameleon.ui.camera

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.FirebaseStorage

class CameraViewModel : ViewModel() {

    private val _photos = MutableLiveData<List<String>>()
    val photos: LiveData<List<String>> = _photos

    init {
        _photos.value = emptyList()
    }

    fun addPhoto(photoPath: String) {
        val updatedPhotos = _photos.value?.toMutableList() ?: mutableListOf()
        updatedPhotos.add(photoPath)
        _photos.value = updatedPhotos
    }

    fun fetchPhotosFromFirebase(userId: String) {
        val storageRef = FirebaseStorage.getInstance().reference.child("users/$userId/photos")

        storageRef.listAll()
            .addOnSuccessListener { result ->
                val urls = mutableListOf<String>()
                for (fileRef in result.items) {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        urls.add(uri.toString())
                        _photos.value = urls
                    }.addOnFailureListener {
                        // Handle errors
                    }
                }
            }
            .addOnFailureListener {
                // Handle failure to fetch files
            }
    }

    fun deletePhoto(photoPath: String) {
        val updatedPhotos = _photos.value?.toMutableList() ?: mutableListOf()
        updatedPhotos.remove(photoPath)
        _photos.value = updatedPhotos
    }
}
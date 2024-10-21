package com.example.mainchameleon.ui.camera

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CameraViewModel : ViewModel() {

    private val _photos = MutableLiveData<MutableList<String>>(mutableListOf())
    val photos: LiveData<MutableList<String>> = _photos

    private val _text = MutableLiveData<String>().apply {
        value = ""
    }
    val text: LiveData<String> = _text

    // Function to add a photo path
    fun addImagePath(photoPath: String) {
        _photos.value?.add(photoPath)
        _photos.value = _photos.value // Notify observers
    }
}

package com.imersa.warnu.ui.register.buyer

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class RegisterBuyerViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _registerStatus = MutableLiveData<String>()
    val registerStatus: LiveData<String> = _registerStatus

    fun register(
        name: String,
        email: String,
        phone: String,
        address: String,
        password: String,
        imageUri: Uri?
    ) {
        _registerStatus.value = "Loading..."
        viewModelScope.launch {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = authResult.user?.uid ?: throw Exception("User ID not found")

                var imageUrl: String? = null
                if (imageUri != null) {
                    val storageRef = storage.reference.child("profile_pictures/$userId/profile.jpg")
                    val uploadTask = storageRef.putFile(imageUri).await()
                    imageUrl = uploadTask.storage.downloadUrl.await().toString()
                }

                val user = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "phone" to phone,
                    "address" to address,
                    "role" to "buyer",
                    "profileImageUrl" to imageUrl
                )

                firestore.collection("users").document(userId).set(user).await()
                _registerStatus.postValue("Success")

            } catch (e: Exception) {
                _registerStatus.postValue("Error: ${e.message}")
            }
        }
    }
}
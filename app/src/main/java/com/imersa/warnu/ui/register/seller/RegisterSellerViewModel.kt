package com.imersa.warnu.ui.register.seller

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

sealed class RegistrationStatus {
    object Loading : RegistrationStatus()
    object Success : RegistrationStatus()
    data class Error(val message: String) : RegistrationStatus()
}

@HiltViewModel
class RegisterSellerViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _registrationStatus = MutableLiveData<RegistrationStatus>()
    val registrationStatus: LiveData<RegistrationStatus> = _registrationStatus

    fun registerSeller(
        name: String,
        storeName: String,
        email: String,
        phone: String,
        address: String,
        password: String,
        imageUri: Uri?
    ) {
        _registrationStatus.value = RegistrationStatus.Loading
        viewModelScope.launch {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = authResult.user?.uid ?: throw Exception("Failed to get user ID")

                var imageUrl: String? = null
                if (imageUri != null) {
                    // kalau tiap user cuma punya 1 foto profil → aman ditimpa tiap update
                    val storageRef = storage.reference.child("profile_pictures/$userId/profile.jpg")
                    val uploadTask = storageRef.putFile(imageUri).await()
                    imageUrl = uploadTask.storage.downloadUrl.await().toString()
                }

                val user = hashMapOf(
                    "name" to name,
                    "storeName" to storeName,
                    "email" to email,
                    "phone" to phone,
                    "address" to address,
                    "role" to "seller",
                    "profileImageUrl" to imageUrl
                )

                firestore.collection("users").document(userId).set(user).await()
                _registrationStatus.postValue(RegistrationStatus.Success)

            } catch (e: Exception) {
                _registrationStatus.postValue(RegistrationStatus.Error(e.message ?: "An unknown error occurred"))
            }
        }
    }
}
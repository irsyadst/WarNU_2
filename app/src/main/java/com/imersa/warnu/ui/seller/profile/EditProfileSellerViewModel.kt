package com.imersa.warnu.ui.seller.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.imersa.warnu.data.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object Loading : UpdateStatus()
    object Success : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}

@HiltViewModel
class EditProfileSellerViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    private val _updateStatus = MutableLiveData<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: LiveData<UpdateStatus> = _updateStatus

    private val userId = auth.currentUser?.uid

    fun loadUserProfile() {
        if (userId == null) return
        _updateStatus.value = UpdateStatus.Loading
        viewModelScope.launch {
            try {
                val document = firestore.collection("users").document(userId).get().await()
                val profile = document.toObject(UserProfile::class.java)
                _userProfile.postValue(profile)
            } catch (e: Exception) {
                _updateStatus.postValue(UpdateStatus.Error(e.message ?: "Failed to load data"))
            } finally {
                if (_updateStatus.value == UpdateStatus.Loading) {
                    _updateStatus.postValue(UpdateStatus.Idle)
                }
            }
        }
    }

    fun saveChanges(name: String, storeName: String, phone: String, address: String, newImageUri: Uri?) {
        if (userId == null) return
        _updateStatus.value = UpdateStatus.Loading

        viewModelScope.launch {
            try {
                val currentImageUrl = _userProfile.value?.profileImageUrl
                val imageUrl = if (newImageUri != null) {
                    val storageRef = storage.reference.child("profile_pictures/$userId/profile.jpg")

                    // Hapus foto lama jika ada sebelum upload yang baru
                    currentImageUrl?.let { if(it.isNotEmpty()) storage.getReferenceFromUrl(it).delete().await() }
                    val uploadTask = storageRef.putFile(newImageUri).await()
                    uploadTask.storage.downloadUrl.await().toString()
                } else {
                    currentImageUrl
                }
                updateFirestore(name, storeName, phone, address, imageUrl)
            } catch (e: Exception) {
                _updateStatus.postValue(UpdateStatus.Error(e.message ?: "Failed to save changes"))
            }
        }
    }

    private suspend fun updateFirestore(name: String, storeName: String, phone: String, address: String, imageUrl: String?) {
        if (userId == null) return
        val updates = mutableMapOf<String, Any?>(
            "name" to name,
            "storeName" to storeName,
            "phone" to phone,
            "address" to address
        )
        if (imageUrl != null) {
            updates["profileImageUrl"] = imageUrl
        }

        firestore.collection("users").document(userId).update(updates).await()
        _updateStatus.postValue(UpdateStatus.Success)
    }

    fun resetStatus() {
        _updateStatus.value = UpdateStatus.Idle
    }
}
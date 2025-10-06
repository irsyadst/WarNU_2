package com.imersa.warnu.ui.buyer.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.imersa.warnu.data.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// Definisikan status langsung di sini
sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object Loading : UpdateStatus()
    object Success : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}

@HiltViewModel
class EditProfileBuyerViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _user = MutableLiveData<UserProfile>()
    val user: LiveData<UserProfile> = _user

    // Gunakan UpdateStatus yang baru didefinisikan
    private val _updateStatus = MutableLiveData<UpdateStatus>()
    val updateStatus: LiveData<UpdateStatus> = _updateStatus

    fun fetchUserData() {
        val userId = auth.currentUser?.uid ?: return
        _updateStatus.value = UpdateStatus.Loading
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val userProfile = document.toObject(UserProfile::class.java)
                if (userProfile != null) {
                    _user.value = userProfile
                    _updateStatus.value = UpdateStatus.Idle // Selesai loading, kembali ke idle
                } else {
                    _updateStatus.value = UpdateStatus.Error("User data not found")
                }
            }
            .addOnFailureListener {
                _updateStatus.value = UpdateStatus.Error(it.message ?: "Failed to fetch data")
            }
    }

    fun updateUser(name: String, phone: String, address: String, newImageUri: Uri?) {
        _updateStatus.value = UpdateStatus.Loading
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _updateStatus.value = UpdateStatus.Error("User not logged in")
            return
        }

        val oldImageUrl = _user.value?.profileImageUrl

        // Jika ada gambar baru yang dipilih
        if (newImageUri != null) {
            val imageRef = storage.reference.child("profile_pictures/${currentUser.uid}/profile.jpg")

            // 1. UPLOAD GAMBAR BARU DULU
            imageRef.putFile(newImageUri)
                .addOnSuccessListener {
                    // 2. JIKA UPLOAD SUKSES, AMBIL URL DOWNLOADNYA
                    imageRef.downloadUrl.addOnSuccessListener { newDownloadUrl ->
                        val updatedData = mapOf(
                            "name" to name,
                            "phoneNumber" to phone,
                            "address" to address,
                            "profilePictureUrl" to newDownloadUrl.toString()
                        )
                        // 3. UPDATE DATA DI FIRESTORE
                        updateFirestoreData(currentUser.uid, updatedData, oldImageUrl)
                    }.addOnFailureListener { e ->
                        _updateStatus.value = UpdateStatus.Error("Failed to get new image URL: ${e.message}")
                    }
                }
                .addOnFailureListener { e ->
                    _updateStatus.value = UpdateStatus.Error("Image upload failed: ${e.message}")
                }
        } else {
            // Jika tidak ada gambar baru (hanya update data teks)
            val updatedData = mapOf(
                "name" to name,
                "phoneNumber" to phone,
                "address" to address
            )
            updateFirestoreData(currentUser.uid, updatedData, null) // Tidak ada gambar lama yang perlu dihapus
        }
    }

    private fun updateFirestoreData(userId: String, data: Map<String, Any>, oldImageUrl: String?) {
        firestore.collection("users").document(userId).update(data)
            .addOnSuccessListener {
                // 4. SETELAH SEMUA BERHASIL, BARU HAPUS GAMBAR LAMA
                if (!oldImageUrl.isNullOrEmpty()) {
                    deleteOldProfilePicture(oldImageUrl)
                }
                _updateStatus.value = UpdateStatus.Success
            }
            .addOnFailureListener { e ->
                _updateStatus.value = UpdateStatus.Error("Failed to update profile data: ${e.message}")
            }
    }

    private fun deleteOldProfilePicture(imageUrl: String) {
        // Fungsi ini aman karena hanya dipanggil setelah semua proses update berhasil
        try {
            val oldImageRef = storage.getReferenceFromUrl(imageUrl)
            oldImageRef.delete()
                .addOnSuccessListener { Log.d("EditProfileVM", "Old picture deleted successfully.") }
                .addOnFailureListener { e -> Log.e("EditProfileVM", "Failed to delete old picture: ${e.message}") }
        } catch (e: Exception) {
            // Tangani jika URL tidak valid, dll.
            Log.e("EditProfileVM", "Error deleting old picture from URL: ${e.message}")
        }
    }

    fun resetStatus() {
        _updateStatus.value = UpdateStatus.Idle
    }
}
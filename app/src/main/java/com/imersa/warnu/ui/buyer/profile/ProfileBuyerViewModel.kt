package com.imersa.warnu.ui.buyer.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileBuyerViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _name = MutableLiveData<String?>()
    val name: LiveData<String?> = _name

    private val _email = MutableLiveData<String?>()
    val email: LiveData<String?> = _email

    private val _phone = MutableLiveData<String?>()
    val phone: LiveData<String?> = _phone

    private val _address = MutableLiveData<String?>()
    val address: LiveData<String?> = _address

    private val _photoUrl = MutableLiveData<String?>()
    val photoUrl: LiveData<String?> = _photoUrl

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadUserProfile() {
        _isLoading.value = true
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "User not logged in."
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            try {
                val document = firestore.collection("users").document(userId).get().await()
                if (document.exists()) {
                    _name.postValue(document.getString("name"))
                    _email.postValue(document.getString("email"))
                    _phone.postValue(document.getString("phone"))
                    _address.postValue(document.getString("address"))
                    _photoUrl.postValue(document.getString("profileImageUrl"))
                } else {
                    _errorMessage.postValue("Profile data not found.")
                }
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
package com.imersa.warnu.ui.buyer.main

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
class MainBuyerViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    private val _userNotFound = MutableLiveData<Boolean>()
    val userNotFound: LiveData<Boolean> = _userNotFound

    fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _userNotFound.value = true
            return
        }

        viewModelScope.launch {
            try {
                val document = firestore.collection("users").document(userId).get().await()
                if (document.exists()) {
                    _name.postValue(document.getString("name"))
                } else {
                    _userNotFound.postValue(true)
                }
            } catch (e: Exception) {
                _userNotFound.postValue(true)
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}
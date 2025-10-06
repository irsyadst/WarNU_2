package com.imersa.warnu.ui.login

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

sealed class LoginState {
    object Loading : LoginState()
    data class Success(val role: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val userId = authResult.user?.uid ?: throw Exception("User not found")

                val userDoc = firestore.collection("users").document(userId).get().await()
                val role = userDoc.getString("role") ?: throw Exception("Role not found")

                _loginState.postValue(LoginState.Success(role))
            } catch (e: Exception) {
                _loginState.postValue(LoginState.Error(e.message ?: "Login failed"))
            }
        }
    }
}
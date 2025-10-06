package com.imersa.warnu.ui.buyer.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.imersa.warnu.data.model.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class DetailOrderBuyerViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _order = MutableLiveData<Order?>()
    val order: LiveData<Order?> = _order

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadOrderDetails(orderId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val document = firestore.collection("orders").document(orderId).get().await()
                if (document.exists()) {
                    val orderData = document.toObject(Order::class.java)
                    _order.postValue(orderData)
                } else {
                    _errorMessage.postValue("Order not found.")
                }
            } catch (e: Exception) {
                _errorMessage.postValue(e.message ?: "Failed to load order details.")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
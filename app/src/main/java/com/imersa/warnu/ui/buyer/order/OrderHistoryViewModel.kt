package com.imersa.warnu.ui.buyer.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.imersa.warnu.data.model.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadOrderHistory() {
        _isLoading.value = true
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _isLoading.value = false
            return
        }

        firestore.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                val orderList = snapshot?.toObjects(Order::class.java) ?: emptyList()
                _orders.value = orderList
                _isLoading.value = false
            }
    }
}
// app/src/main/java/com/imersa/warnu/ui/seller/order/DetailOrderViewModel.kt
package com.imersa.warnu.ui.seller.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.imersa.warnu.data.model.Order
import com.imersa.warnu.ui.seller.profile.UpdateStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DetailOrderViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _order = MutableLiveData<Order?>()
    val order: LiveData<Order?> = _order

    private val _updateStatus = MutableLiveData<UpdateStatus>()
    val updateStatus: LiveData<UpdateStatus> = _updateStatus

    fun loadOrderDetails(orderId: String) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("orders").document(orderId).get().await()
                val orderData = document.toObject(Order::class.java)
                _order.postValue(orderData)
            } catch (e: Exception) {
                _order.postValue(null)
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        _updateStatus.value = UpdateStatus.Loading
        viewModelScope.launch {
            try {
                firestore.collection("orders").document(orderId)
                    .update("orderStatus", newStatus.lowercase(Locale.getDefault()))
                    .await()
                _updateStatus.postValue(UpdateStatus.Success)
            } catch (e: Exception) {
                _updateStatus.postValue(UpdateStatus.Error(e.message ?: "Unknown error"))
            }
        }
    }
}
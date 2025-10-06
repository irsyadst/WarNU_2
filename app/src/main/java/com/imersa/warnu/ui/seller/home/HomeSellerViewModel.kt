package com.imersa.warnu.ui.seller.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.imersa.warnu.data.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeSellerViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadProducts() {
        _isLoading.value = true
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("products")
                    .whereEqualTo("sellerId", userId)
                    .get()
                    .await()
                val productList = snapshot.documents.mapNotNull { document ->
                    val product = document.toObject(Product::class.java)
                    // Salin objek produk dan tambahkan ID dari dokumennya
                    product?.copy(id = document.id)
                }

                _products.postValue(productList)
            } catch (e: Exception) {
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
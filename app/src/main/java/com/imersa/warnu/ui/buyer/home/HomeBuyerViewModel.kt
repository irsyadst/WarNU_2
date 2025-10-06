package com.imersa.warnu.ui.buyer.home

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
class HomeBuyerViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var allProducts: List<Product> = listOf()

    init {
        loadProducts()
    }

    fun loadProducts() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("products").get().await()
                val productList = snapshot.documents.mapNotNull { document ->
                    val product = document.toObject(Product::class.java)
                    product?.copy(id = document.id)
                }
                allProducts = productList
                _products.postValue(productList)
            } catch (e: Exception) {
                _products.postValue(emptyList())
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun searchProducts(query: String) {
        if (query.isBlank()) {
            _products.value = allProducts
            return
        }
        val filteredList = allProducts.filter { product ->
            product.name?.contains(query, ignoreCase = true) == true
        }
        _products.value = filteredList
    }
}
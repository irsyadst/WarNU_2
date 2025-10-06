package com.imersa.warnu.ui.seller.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.imersa.warnu.data.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class EditManagerViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    fun fetchProducts(sellerId: String) {
        firestore.collection("products")
            .whereEqualTo("sellerId", sellerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _toastMessage.value = "Error fetching products."
                    return@addSnapshotListener
                }

                // --- PERBAIKAN: Mengambil data beserta ID dokumen ---
                val productList = snapshot?.documents?.mapNotNull { document ->
                    val product = document.toObject(Product::class.java)
                    product?.copy(id = document.id)
                } ?: emptyList()

                _products.value = productList
            }
    }

    fun deleteProduct(productId: String, imageUrl: String?) {
        viewModelScope.launch {
            try {
                firestore.collection("products").document(productId).delete().await()

                if (!imageUrl.isNullOrEmpty()) {
                    storage.getReferenceFromUrl(imageUrl).delete().await()
                }
                _toastMessage.postValue("Product deleted successfully.")
            } catch (e: Exception) {
                _toastMessage.postValue("Error deleting product: ${e.message}")
            }
        }
    }

    fun onToastMessageShown() {
        _toastMessage.value = null
    }
}
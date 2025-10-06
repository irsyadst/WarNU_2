package com.imersa.warnu.ui.seller.product

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.imersa.warnu.data.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class EditState {
    object Idle : EditState()
    object Loading : EditState()
    object Success : EditState()
    data class Error(val message: String) : EditState()
}

@HiltViewModel
class EditProductViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _product = MutableLiveData<Product?>()
    val product: LiveData<Product?> = _product

    private val _editState = MutableLiveData<EditState>(EditState.Idle)
    val editState: LiveData<EditState> = _editState

    private var currentImageUrl: String? = null

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("products").document(productId).get().await()
                val productData = document.toObject(Product::class.java)?.copy(id = document.id)
                _product.postValue(productData)
                currentImageUrl = productData?.imageUrl
            } catch (e: Exception) {
                _editState.postValue(EditState.Error("Failed to load product: ${e.message}"))
            }
        }
    }

    fun saveChanges(
        productId: String,
        name: String,
        priceStr: String,
        stockStr: String,
        category: String,
        description: String,
        newImageUri: Uri?
    ) {
        if (name.isBlank() || priceStr.isBlank() || stockStr.isBlank() || category.isBlank() || description.isBlank()) {
            _editState.value = EditState.Error("All fields must be filled.")
            return
        }

        _editState.value = EditState.Loading

        viewModelScope.launch {
            try {
                val imageUrl = if (newImageUri != null) {
                    // Hapus gambar lama jika ada
                    currentImageUrl?.let { if(it.isNotEmpty()) storage.getReferenceFromUrl(it).delete().await() }
                    // Upload gambar baru
                    uploadNewImage(productId, newImageUri)
                } else {
                    currentImageUrl
                }
                updateProductInFirestore(productId, name, priceStr.toDouble(), stockStr.toInt(), category, description, imageUrl)
                _editState.postValue(EditState.Success)
            } catch (e: Exception) {
                _editState.postValue(EditState.Error(e.message ?: "An unknown error occurred."))
            }
        }
    }

    private suspend fun uploadNewImage(productId: String, imageUri: Uri): String {
        val fileName = "${System.currentTimeMillis()}_${productId}"
        val storageRef = storage.reference.child("product_images/$fileName")
        val uploadTask = storageRef.putFile(imageUri).await()
        return uploadTask.storage.downloadUrl.await().toString()
    }

    private suspend fun updateProductInFirestore(
        productId: String, name: String, price: Double, stock: Int, category: String, description: String, imageUrl: String?
    ) {
        val productUpdates = mapOf(
            "name" to name,
            "price" to price,
            "stock" to stock,
            "category" to category,
            "description" to description,
            "imageUrl" to imageUrl
        )
        firestore.collection("products").document(productId).update(productUpdates).await()
    }

    fun resetState() {
        _editState.value = EditState.Idle
    }
}
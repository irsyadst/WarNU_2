// app/src/main/java/com/imersa/warnu/ui/seller/product/AddProductViewModel.kt
package com.imersa.warnu.ui.seller.product

import android.net.Uri
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
import java.util.*
import javax.inject.Inject

sealed class AddProductState {
    object Idle : AddProductState()
    object Loading : AddProductState()
    data class Success(val message: String) : AddProductState()
    data class Error(val message: String) : AddProductState()
}

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableLiveData<AddProductState>(AddProductState.Idle)
    val state: LiveData<AddProductState> = _state

    fun addProduct(
        name: String,
        priceStr: String,
        stockStr: String,
        category: String,
        description: String,
        imageUri: Uri?
    ) {
        if (name.isBlank() || priceStr.isBlank() || stockStr.isBlank() || category.isBlank() || description.isBlank() || imageUri == null) {
            _state.value = AddProductState.Error("All fields and image must be filled.")
            return
        }
        val price = priceStr.toDoubleOrNull()
        val stock = stockStr.toIntOrNull()
        if (price == null || stock == null) {
            _state.value = AddProductState.Error("Price and stock must be valid numbers.")
            return
        }

        _state.value = AddProductState.Loading
        viewModelScope.launch {
            try {
                val sellerId = auth.currentUser?.uid ?: throw Exception("User not logged in.")

                val userDocument = firestore.collection("users").document(sellerId).get().await()
                val storeName = userDocument.getString("storeName") ?: throw Exception("Store name not found.")

                val imageFileName = "product_images/${UUID.randomUUID()}"
                val storageRef = storage.reference.child(imageFileName)
                val uploadTask = storageRef.putFile(imageUri).await()
                val imageUrl = uploadTask.storage.downloadUrl.await().toString()

                val newProductRef = firestore.collection("products").document()
                val productId = newProductRef.id

                val product = Product(
                    id = productId,
                    name = name,
                    price = price,
                    description = description,
                    stock = stock,
                    category = category,
                    imageUrl = imageUrl,
                    sellerId = sellerId,
                    storeName = storeName
                )

                newProductRef.set(product).await()
                _state.postValue(AddProductState.Success("Product added successfully!"))

            } catch (e: Exception) {
                _state.postValue(AddProductState.Error(e.message ?: "Failed to add product."))
            }
        }
    }

    fun resetState() {
        _state.value = AddProductState.Idle
    }
}
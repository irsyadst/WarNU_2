package com.imersa.warnu.ui.seller.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.imersa.warnu.data.model.CartItem
import com.imersa.warnu.data.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class DetailProductViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _product = MutableLiveData<Product?>()
    val product: LiveData<Product?> = _product

    private val _addToCartStatus = MutableLiveData<String?>()
    val addToCartStatus: LiveData<String?> = _addToCartStatus

    private val _userRole = MutableLiveData<String?>()
    val userRole: LiveData<String?> = _userRole

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("products").document(productId).get().await()
                val productData = document.toObject(Product::class.java)?.copy(id = document.id)
                _product.postValue(productData)
            } catch (e: Exception) {
                _product.postValue(null)
            }
        }
    }

    fun loadUserRole() {
        val userId = auth.currentUser?.uid ?: run {
            _userRole.value = null
            return
        }
        viewModelScope.launch {
            try {
                val document = firestore.collection("users").document(userId).get().await()
                _userRole.postValue(document.getString("role"))
            } catch (e: Exception) {
                _userRole.postValue(null)
            }
        }
    }

    fun addToCart(product: Product, quantityToAdd: Int) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _addToCartStatus.value = "You must be logged in to add items."
            return
        }

        val productId = product.id
        if (productId == null) {
            _addToCartStatus.value = "Cannot add item: Product ID is missing."
            return
        }

        viewModelScope.launch {
            try {
                // Lokasi item di keranjang pengguna
                val cartItemRef = firestore.collection("carts").document(userId)
                    .collection("items").document(productId)

                val productRef = firestore.collection("products").document(productId)
                val productSnapshot = productRef.get().await()
                val currentStock = productSnapshot.toObject(Product::class.java)?.stock ?: 0


                val existingCartItemSnapshot = cartItemRef.get().await()
                val quantityInCart = if (existingCartItemSnapshot.exists()) {
                    existingCartItemSnapshot.toObject(CartItem::class.java)?.quantity ?: 0
                } else {
                    0
                }

                // Validasi stok
                if (quantityInCart + quantityToAdd > currentStock) {
                    _addToCartStatus.postValue("Not enough stock. You have $quantityInCart item(s) in your cart.")
                    return@launch
                }
                val cartItem = CartItem(
                    productId = product.id,
                    name = product.name,
                    price = product.price,
                    quantity = quantityInCart + quantityToAdd,
                    imageUrl = product.imageUrl,
                    sellerId = product.sellerId,
                    storeName = product.storeName
                )

                cartItemRef.set(cartItem).await()
                _addToCartStatus.postValue("Successfully added to cart.")

            } catch (e: Exception) {
                _addToCartStatus.postValue("Failed to add to cart: ${e.message}")
            }
        }
    }


    fun onStatusMessageShown() {
        _addToCartStatus.value = null
    }
}
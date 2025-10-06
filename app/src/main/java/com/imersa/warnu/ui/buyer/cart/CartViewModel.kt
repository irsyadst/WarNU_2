package com.imersa.warnu.ui.buyer.cart

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
class CartViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : ViewModel() {
    private val _originalCartItems = MutableLiveData<List<CartItem>>()
    private val _cartViewItems = MutableLiveData<List<CartViewItem>>()
    val cartViewItems: LiveData<List<CartViewItem>> = _cartViewItems

    private val _totalPrice = MutableLiveData<Double>()
    val totalPrice: LiveData<Double> = _totalPrice

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    init {
        loadCartItems()
    }

    fun getOriginalCartItems(): List<CartItem> {
        return _originalCartItems.value ?: emptyList()
    }

    private fun loadCartItems() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("carts").document(userId)
                    .collection("items").get().await()
                val cartItems = snapshot.toObjects(CartItem::class.java)

                _originalCartItems.postValue(cartItems)
                processCartItemsForView(cartItems)

            } catch (e: Exception) {
                _toastMessage.postValue("Failed to load cart items.")
            }
        }
    }

    private fun processCartItemsForView(cartItems: List<CartItem>) {
        val groupedByStore = cartItems.groupBy { it.storeName ?: "Unknown Store" }
        val viewItems = mutableListOf<CartViewItem>()
        groupedByStore.forEach { (storeName, items) ->
            viewItems.add(CartViewItem.Header(storeName))
            items.forEach { cartItem ->
                viewItems.add(CartViewItem.Product(cartItem))
            }
        }
        _cartViewItems.postValue(viewItems)
        _totalPrice.postValue(cartItems.sumOf { (it.price ?: 0.0) * it.quantity })
    }

    fun increaseCartItemQuantity(cartItem: CartItem) {
        val productId = cartItem.productId ?: return
        viewModelScope.launch {
            try {
                val productDoc = firestore.collection("products").document(productId).get().await()
                val product = productDoc.toObject(Product::class.java)
                val availableStock = product?.stock ?: 0

                if (cartItem.quantity < availableStock) {
                    updateCartItemQuantity(productId, cartItem.quantity + 1)
                } else {
                    _toastMessage.postValue("Not enough stock, only $availableStock items left.")
                }
            } catch (e: Exception) {
                _toastMessage.postValue("Failed to verify product stock.")
            }
        }
    }

    fun updateCartItemQuantity(productId: String, newQuantity: Int) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("carts").document(userId)
            .collection("items").document(productId)
            .update("quantity", newQuantity).addOnSuccessListener { loadCartItems() }
    }

    fun removeCartItem(productId: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("carts").document(userId)
            .collection("items").document(productId).delete()
            .addOnSuccessListener { loadCartItems() }
    }

    fun onToastMessageShown() {
        _toastMessage.value = null
    }

    fun clearCart() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val cartItemsRef = firestore.collection("carts").document(userId).collection("items")
            val snapshot = cartItemsRef.get().await()
            val batch = firestore.batch()
            snapshot.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
            loadCartItems()
        }
    }
}
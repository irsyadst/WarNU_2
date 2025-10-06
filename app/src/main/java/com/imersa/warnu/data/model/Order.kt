package com.imersa.warnu.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Order(
    var orderId: String? = null,
    var userId: String? = null,
    var totalAmount: Double? = null,
    var paymentStatus: String? = null,
    val customerPhone: String? = null,
    val address: String? = null,
    var createdAt: Timestamp? = null,
    var customerName: String? = null,
    var sellerId: String? = null,
    var orderStatus: String? = null,

    @get:PropertyName("items")
    @set:PropertyName("items")
    var items: ArrayList<CartItem>? = null
) {
    constructor() : this(null, null, null, null, null, null, null, null, null)
}



package com.imersa.warnu.data.model

import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    val productId: String? = null,
    val name: String? = null,
    val price: Double? = null,
    var quantity: Int = 0,
    val imageUrl: String? = null,
    @get:PropertyName("sellerId")
    @set:PropertyName("sellerId")
    var sellerId: String? = null,
    val storeName: String? = null
) : Parcelable {
    constructor() : this(null, null, null, 0, null, null, null)
}
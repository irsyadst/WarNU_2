package com.imersa.warnu.ui.buyer.cart

import com.imersa.warnu.data.model.CartItem

sealed class CartViewItem {
    data class Header(val storeName: String) : CartViewItem()
    data class Product(val cartItem: CartItem) : CartViewItem()
}
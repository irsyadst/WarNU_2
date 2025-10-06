package com.imersa.warnu.ui.buyer.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.imersa.warnu.R
import com.imersa.warnu.data.model.CartItem
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private var cartViewItems: List<CartViewItem>,
    private val onIncrease: (CartItem) -> Unit,
    private val onDecrease: (CartItem) -> Unit,
    private val onRemove: (CartItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_PRODUCT = 1
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val storeName: TextView = view.findViewById(R.id.tv_store_name)
        fun bind(header: CartViewItem.Header) {
            storeName.text = header.storeName
        }
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.iv_product_image)
        val tvProductName: TextView = itemView.findViewById(R.id.tv_product_name)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tv_product_price)
        val tvQuantity: TextView = itemView.findViewById(R.id.tv_quantity)
        val btnIncrease: ImageButton = itemView.findViewById(R.id.btn_increase_quantity)
        val btnDecrease: ImageButton = itemView.findViewById(R.id.btn_decrease_quantity)
        val btnRemove: ImageButton = itemView.findViewById(R.id.btn_remove_item)
    }

    override fun getItemViewType(position: Int): Int {
        return when (cartViewItems[position]) {
            is CartViewItem.Header -> VIEW_TYPE_HEADER
            is CartViewItem.Product -> VIEW_TYPE_PRODUCT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cart_seller_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cart, parent, false)
            ProductViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = cartViewItems[position]) {
            is CartViewItem.Header -> (holder as HeaderViewHolder).bind(item)
            is CartViewItem.Product -> {
                val productHolder = holder as ProductViewHolder
                val cartItem = item.cartItem

                productHolder.tvProductName.text = cartItem.name
                productHolder.tvQuantity.text = cartItem.quantity.toString()
                val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")) as DecimalFormat
                formatter.maximumFractionDigits = 0
                formatter.minimumFractionDigits = 0
                productHolder.tvProductPrice.text = formatter.format(cartItem.price ?: 0.0)

                Glide.with(holder.itemView.context)
                    .load(cartItem.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(productHolder.ivProductImage)

                productHolder.btnIncrease.setOnClickListener { onIncrease(cartItem) }
                productHolder.btnDecrease.setOnClickListener { onDecrease(cartItem) }
                productHolder.btnRemove.setOnClickListener { onRemove(cartItem) }
            }
        }
    }

    override fun getItemCount(): Int = cartViewItems.size

    fun updateData(newItems: List<CartViewItem>) {
        cartViewItems = newItems
        notifyDataSetChanged()
    }
}
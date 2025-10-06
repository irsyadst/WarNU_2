package com.imersa.warnu.ui.seller.order

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.imersa.warnu.R
import com.imersa.warnu.data.model.CartItem
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class DetailOrderAdapter : ListAdapter<CartItem, DetailOrderAdapter.ProductViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProduct: ImageView = itemView.findViewById(R.id.iv_product_image)
        private val tvName: TextView = itemView.findViewById(R.id.tv_product_name)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tv_product_quantity)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_product_price)

        @SuppressLint("SetTextI18n")
        fun bind(item: CartItem) {
            tvName.text = item.name ?: "Produk"
            tvQuantity.text = "Jumlah: ${item.quantity}"

            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")) as DecimalFormat
            formatter.maximumFractionDigits = 0
            formatter.minimumFractionDigits = 0
            tvPrice.text = formatter.format(item.price ?: 0.0)

            Glide.with(itemView.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image) // biar aman kalau gagal load
                .into(ivProduct)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}

package com.imersa.warnu.ui.buyer.product

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.imersa.warnu.R
import com.imersa.warnu.data.model.Product

class ProductAdapter(
    private val onItemClick: (Product) -> Unit
) : androidx.recyclerview.widget.ListAdapter<Product, ProductAdapter.ViewHolder>(ProdukDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_product_buyer, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View, val onItemClick: (Product) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val imgProduk: ImageView = itemView.findViewById(R.id.iv_product_buyer)
        private val tvNama: TextView = itemView.findViewById(R.id.tv_product_name_buyer)
        private val tvHarga: TextView = itemView.findViewById(R.id.tv_product_price_buyer)

        @SuppressLint("DefaultLocale")
        fun bind(produk: Product) {
            tvNama.text = produk.name ?: "-"
            val priceText = if (produk.price != null) {
                "Rp ${String.format("%,.0f", produk.price)}"
            } else {
                "Harga belum tersedia"
            }
            tvHarga.text = priceText

            Glide.with(itemView.context).load(produk.imageUrl)
                .placeholder(R.drawable.placeholder_image).into(imgProduk)

            itemView.setOnClickListener {
                onItemClick(produk)
            }
        }
    }

    class ProdukDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}

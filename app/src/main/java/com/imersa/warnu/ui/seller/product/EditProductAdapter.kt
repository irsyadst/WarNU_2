package com.imersa.warnu.ui.seller.product

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.imersa.warnu.R
import com.imersa.warnu.data.model.Product
import java.text.NumberFormat
import java.util.Locale

class EditProductAdapter(
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : ListAdapter<Product, EditProductAdapter.EditProductViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_edit_product, parent, false)
        return EditProductViewHolder(view, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: EditProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EditProductViewHolder(
        itemView: View,
        private val onEditClick: (Product) -> Unit,
        private val onDeleteClick: (Product) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivProduct: ImageView = itemView.findViewById(R.id.iv_product)
        private val tvProductName: TextView = itemView.findViewById(R.id.tv_product_name)
        private val tvPriceStock: TextView = itemView.findViewById(R.id.tv_price_stock)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)

        @SuppressLint("SetTextI18n")
        fun bind(product: Product) {
            tvProductName.text = product.name
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            val price = formatter.format(product.price)
            tvPriceStock.text = "Price: $price | Stock: ${product.stock}"

            Glide.with(itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(ivProduct)

            itemView.setOnClickListener {
                onEditClick(product)
            }
            btnDelete.setOnClickListener {
                onDeleteClick(product)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}
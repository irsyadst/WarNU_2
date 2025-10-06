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
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class ProductSellerAdapter(
    private val layoutResId: Int,
    private val onItemClick: (Product) -> Unit,
    private val onEditClick: ((Product) -> Unit)? = null,
    private val onDeleteClick: ((Product) -> Unit)? = null
) : ListAdapter<Product, ProductSellerAdapter.ProductViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return ProductViewHolder(view, layoutResId, onItemClick, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProductViewHolder(
        itemView: View,
        private val layoutResId: Int,
        private val onItemClick: (Product) -> Unit,
        private val onEditClick: ((Product) -> Unit)?,
        private val onDeleteClick: ((Product) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {

        private var ivProduct: ImageView? = null
        private var tvProductName: TextView? = null
        private var tvPrice: TextView? = null
        private var tvStock: TextView? = null
        private var btnEdit: ImageButton? = null
        private var btnDelete: ImageButton? = null

        init {
            if (layoutResId == R.layout.item_product_seller) {
                ivProduct = itemView.findViewById(R.id.iv_product_seller)
                tvProductName = itemView.findViewById(R.id.tv_product_name_seller)
                tvPrice = itemView.findViewById(R.id.tv_product_price_seller)
                tvStock = itemView.findViewById(R.id.tv_product_stock_seller)
            } else { // R.layout.item_edit_product
                ivProduct = itemView.findViewById(R.id.iv_product)
                tvProductName = itemView.findViewById(R.id.tv_product_name)
                tvPrice = itemView.findViewById(R.id.tv_price_stock)
                btnEdit = itemView.findViewById(R.id.btn_edit)
                btnDelete = itemView.findViewById(R.id.btn_delete)
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(product: Product) {
            tvProductName?.text = product.name

            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")) as DecimalFormat
            formatter.maximumFractionDigits = 0
            formatter.minimumFractionDigits = 0
            val priceFormatted = formatter.format(product.price ?: 0.0)

            if (layoutResId == R.layout.item_edit_product) {
                tvPrice?.text = "Price: $priceFormatted | Stock: ${product.stock ?: 0}"
            } else {
                tvPrice?.text = priceFormatted
                tvStock?.text = "Stock: ${product.stock ?: 0}"
            }

            ivProduct?.let {
                Glide.with(itemView.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(it)
            }

            itemView.setOnClickListener { onItemClick(product) }
            btnEdit?.setOnClickListener { onEditClick?.invoke(product) }
            btnDelete?.setOnClickListener { onDeleteClick?.invoke(product) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean = oldItem == newItem
    }
}
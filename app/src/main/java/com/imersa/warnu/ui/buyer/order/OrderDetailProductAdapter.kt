package com.imersa.warnu.ui.buyer.order

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.imersa.warnu.R
import com.imersa.warnu.data.model.CartItem
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class OrderDetailProductAdapter(private val items: List<CartItem>) :
    RecyclerView.Adapter<OrderDetailProductAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProduct: ImageView = itemView.findViewById(R.id.iv_product_image)
        private val tvName: TextView = itemView.findViewById(R.id.tv_product_name)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tv_product_quantity)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_product_price)

        @SuppressLint("SetTextI18n")
        fun bind(item: CartItem) {
            tvName.text = item.name
            tvQuantity.text = "Jumlah: ${item.quantity}"

            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")) as DecimalFormat
            formatter.maximumFractionDigits = 0
            formatter.minimumFractionDigits = 0
            tvPrice.text = formatter.format(item.price ?: 0.0)

            Glide.with(itemView.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(ivProduct)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
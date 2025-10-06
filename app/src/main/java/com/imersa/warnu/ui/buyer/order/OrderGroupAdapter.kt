package com.imersa.warnu.ui.buyer.order

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.imersa.warnu.R
import com.imersa.warnu.data.model.CartItem

class OrderGroupAdapter(private val itemsBySeller: Map<String?, List<CartItem>>) :
    RecyclerView.Adapter<OrderGroupAdapter.ViewHolder>() {

    // Mengambil daftar nama penjual dari keys Map untuk menentukan jumlah item
    private val sellers = itemsBySeller.keys.toList()

    // ViewHolder untuk setiap grup penjual
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStoreName: TextView = itemView.findViewById(R.id.tv_store_name)
        private val rvProducts: RecyclerView = itemView.findViewById(R.id.rv_products_in_group)

        fun bind(storeName: String?, items: List<CartItem>?) {
            tvStoreName.text = storeName ?: "Unknown Store"
            items?.let {
                // Setup RecyclerView internal untuk menampilkan produk
                rvProducts.layoutManager = LinearLayoutManager(itemView.context)
                rvProducts.adapter = OrderDetailProductAdapter(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_seller_group, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Mendapatkan nama penjual dan daftar produk untuk posisi saat ini
        val sellerName = sellers[position]
        val items = itemsBySeller[sellerName]
        holder.bind(sellerName, items)
    }

    override fun getItemCount(): Int = sellers.size
}
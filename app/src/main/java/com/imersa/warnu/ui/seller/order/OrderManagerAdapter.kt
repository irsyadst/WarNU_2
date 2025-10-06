package com.imersa.warnu.ui.seller.order

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.imersa.warnu.R
import com.imersa.warnu.data.model.Order
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class OrderManagerAdapter(
    private val onItemClick: (Order) -> Unit
) : ListAdapter<Order, OrderManagerAdapter.OrderViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_orders, parent, false)
        return OrderViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OrderViewHolder(itemView: View, private val onItemClick: (Order) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val tvCustomerName: TextView = itemView.findViewById(R.id.tv_customer_name)
        private val tvOrderId: TextView = itemView.findViewById(R.id.tv_order_id)
        private val tvTotalAmount: TextView = itemView.findViewById(R.id.tv_total_amount)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)

        @SuppressLint("SetTextI18n")
        fun bind(order: Order) {
            // Set customer name & order ID
            tvCustomerName.text = order.customerName ?: "Pembeli"
            tvOrderId.text = order.orderId ?: "-"

            // Format total amount
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")) as DecimalFormat
            formatter.maximumFractionDigits = 0
            formatter.minimumFractionDigits = 0
            tvTotalAmount.text = formatter.format(order.totalAmount ?: 0.0)

            // Status
            val status = order.orderStatus ?: "pending"
            tvStatus.text = status.replaceFirstChar { it.uppercase() }

            val statusBackground = when (status.lowercase()) {
                "pending" -> R.drawable.status_pending_background
                "processing" -> R.drawable.status_processing_background
                "shipped" -> R.drawable.status_shipped_background
                "completed" -> R.drawable.status_completed_background
                "cancelled" -> R.drawable.status_cancelled_background
                else -> R.drawable.status_pending_background
            }
            tvStatus.background = ContextCompat.getDrawable(itemView.context, statusBackground)

            itemView.setOnClickListener { onItemClick(order) }
        }
    }
        class DiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}

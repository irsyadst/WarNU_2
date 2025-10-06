package com.imersa.warnu.ui.buyer.order

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
import java.text.SimpleDateFormat
import java.util.Locale

class OrderHistoryAdapter(private val onItemClick: (Order) -> Unit) :
    ListAdapter<Order, OrderHistoryAdapter.OrderViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_history, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClick(order)
        }
        holder.bind(order)
    }

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView = itemView.findViewById(R.id.tv_order_id)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val tvTotalAmount: TextView = itemView.findViewById(R.id.tv_total_amount)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)

        @SuppressLint("SetTextI18n")
        fun bind(order: Order) {
            tvOrderId.text = "Order ID: ${order.orderId}"

            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            tvDate.text = order.createdAt?.toDate()?.let { sdf.format(it) } ?: "No date"

            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")) as DecimalFormat
            formatter.maximumFractionDigits = 0
            formatter.minimumFractionDigits = 0
            tvTotalAmount.text = formatter.format(order.totalAmount ?: 0.0)

            val status = order.orderStatus ?: "pending"
            tvStatus.text = status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            val statusBackground = when (status.lowercase(Locale.getDefault())) {
                "pending" -> R.drawable.status_pending_background
                "processing" -> R.drawable.status_processing_background
                "shipped" -> R.drawable.status_shipped_background
                "completed" -> R.drawable.status_completed_background
                "cancelled" -> R.drawable.status_cancelled_background
                "settlement" -> R.drawable.status_settlement_background
                else -> R.drawable.status_pending_background
            }
            tvStatus.background = ContextCompat.getDrawable(itemView.context, statusBackground)
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
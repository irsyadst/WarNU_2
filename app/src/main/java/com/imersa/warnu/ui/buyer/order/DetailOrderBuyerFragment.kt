package com.imersa.warnu.ui.buyer.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.imersa.warnu.R
import com.imersa.warnu.data.model.Order
import com.imersa.warnu.databinding.FragmentDetailOrderBuyerBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class DetailOrderBuyerFragment : Fragment() {

    private var _binding: FragmentDetailOrderBuyerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailOrderBuyerViewModel by viewModels()
    private var orderId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailOrderBuyerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderId = arguments?.getString("orderId")
        if (orderId == null) {
            Toast.makeText(context, "Order ID not found", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        observeViewModel()
        orderId?.let { viewModel.loadOrderDetails(it) }
    }

    private fun observeViewModel() {
        viewModel.order.observe(viewLifecycleOwner) { order ->
            order?.let { populateUi(it) }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->

        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun populateUi(order: Order) {
        binding.tvOrderIdHeader.text = "Order Details #${order.orderId}"

        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        binding.tvOrderDate.text = order.createdAt?.toDate()?.let { sdf.format(it) } ?: "No date"

        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")) as DecimalFormat
        formatter.maximumFractionDigits = 0
        formatter.minimumFractionDigits = 0
        binding.tvTotalPrice.text = formatter.format(order.totalAmount ?: 0.0)

        binding.tvPaymentMethod.text = "BCA VA"
        binding.tvOrderStatus.text = order.orderStatus

        val statusBackground = when (order.orderStatus) {
            "pending" -> R.drawable.status_pending_background
            "processing" -> R.drawable.status_processing_background
            "shipped" -> R.drawable.status_shipped_background
            "completed" -> R.drawable.status_completed_background
            "cancelled" -> R.drawable.status_cancelled_background
            else -> R.drawable.status_pending_background
        }
        binding.tvOrderStatus.setBackgroundResource(statusBackground)

        // Group items by seller
        val itemsBySeller = order.items?.groupBy { it.storeName }

        if (itemsBySeller != null) {
            val adapter = OrderGroupAdapter(itemsBySeller)
            binding.rvOrderItemsBySeller.layoutManager = LinearLayoutManager(context)
            binding.rvOrderItemsBySeller.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
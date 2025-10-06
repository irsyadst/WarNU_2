// app/src/main/java/com/imersa/warnu/ui/seller/order/DetailOrderFragment.kt
package com.imersa.warnu.ui.seller.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.imersa.warnu.data.model.Order
import com.imersa.warnu.databinding.FragmentDetailOrderBinding
import com.imersa.warnu.ui.seller.profile.UpdateStatus
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class DetailOrderFragment : Fragment() {

    private var _binding: FragmentDetailOrderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailOrderViewModel by viewModels()
    private lateinit var productAdapter: DetailOrderAdapter
    private var orderId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderId = arguments?.getString("orderId")
        if (orderId == null) {
            Toast.makeText(context, "Order ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        setupRecyclerView()
        setupStatusDropdown()
        observeViewModel()
        setupListeners()

        orderId?.let { viewModel.loadOrderDetails(it) }
    }

    private fun setupRecyclerView() {
        productAdapter = DetailOrderAdapter()
        binding.rvOrderProducts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productAdapter
        }
    }

    private fun setupStatusDropdown() {
        val statuses = listOf("pending", "processing", "shipped", "completed", "cancelled")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses)
        binding.dropdownOrderStatus.setAdapter(adapter)

        // Default value "pending"
        binding.dropdownOrderStatus.setText("pending", false)
    }

    private fun observeViewModel() {
        viewModel.order.observe(viewLifecycleOwner) { order ->
            order?.let { populateUi(it) }
        }

        viewModel.updateStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is UpdateStatus.Success -> {
                    Toast.makeText(context, "Order status updated successfully", Toast.LENGTH_SHORT).show()
                    binding.btnUpdateStatus.isEnabled = true
                    findNavController().popBackStack()
                }
                is UpdateStatus.Error -> {
                    Toast.makeText(context, "Failed: ${status.message}", Toast.LENGTH_SHORT).show()
                    binding.btnUpdateStatus.isEnabled = true
                }
                is UpdateStatus.Loading -> {
                    binding.btnUpdateStatus.isEnabled = false
                }
                else -> {
                    binding.btnUpdateStatus.isEnabled = true
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnUpdateStatus.setOnClickListener {
            val selectedStatus = binding.dropdownOrderStatus.text.toString()
            if (selectedStatus.isNotBlank() && orderId != null) {
                viewModel.updateOrderStatus(orderId!!, selectedStatus)
            } else {
                Toast.makeText(context, "Pilih status terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateUi(order: Order) {
        binding.tvBuyerName.text = "Nama: ${order.customerName ?: "Tidak tersedia"}"
        binding.tvBuyerPhone.text = "Telepon: ${order.customerPhone ?: "Tidak tersedia"}"
        binding.tvBuyerAddress.text = "Alamat: ${order.address ?: "Tidak tersedia"}"

        productAdapter.submitList(order.items)

        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")) as DecimalFormat
        formatter.maximumFractionDigits = 0
        formatter.minimumFractionDigits = 0
        binding.tvTotalPrice.text = "Total: ${formatter.format(order.totalAmount ?: 0.0)}"

        binding.tvPaymentMethod.text = "Metode: BCA VA"
        binding.tvPaymentStatus.text =
            "Status: ${order.paymentStatus?.replaceFirstChar { it.uppercase() } ?: "N/A"}"

        val statusValue = order.orderStatus ?: "pending"
        binding.dropdownOrderStatus.setText(statusValue, false)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
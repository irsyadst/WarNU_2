package com.imersa.warnu.ui.buyer.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.imersa.warnu.R
import com.imersa.warnu.databinding.FragmentOrderHistoryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderHistoryFragment : Fragment() {

    private var _binding: FragmentOrderHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrderHistoryViewModel by viewModels()
    private lateinit var orderAdapter: OrderHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadOrderHistory()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderHistoryAdapter { order ->
            val bundle = bundleOf("orderId" to order.orderId)
            findNavController().navigate(R.id.nav_detail_order_buyer, bundle)
        }

        binding.rvOrderHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = orderAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.orders.observe(viewLifecycleOwner) { orders ->
            binding.tvNoOrders.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
            orderAdapter.submitList(orders)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


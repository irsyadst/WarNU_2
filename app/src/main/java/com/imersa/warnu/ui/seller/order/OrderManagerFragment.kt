package com.imersa.warnu.ui.seller.order

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
import com.imersa.warnu.databinding.FragmentSellerOrdersBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderManagerFragment : Fragment() {

    private var _binding: FragmentSellerOrdersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrderManagerViewModel by viewModels()
    private lateinit var orderAdapter: OrderManagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadOrders()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderManagerAdapter { order ->
            val bundle = Bundle().apply { putString("orderId", order.orderId) }
            findNavController().navigate(R.id.nav_detail_order, bundle)
        }

        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = orderAdapter
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


package com.imersa.warnu.ui.seller.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.imersa.warnu.R
import com.imersa.warnu.databinding.FragmentHomeSellerBinding
import com.imersa.warnu.ui.seller.product.ProductSellerAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeSellerFragment : Fragment() {

    private var _binding: FragmentHomeSellerBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var auth: FirebaseAuth

    private val viewModel: HomeSellerViewModel by viewModels()
    private lateinit var adapter: ProductSellerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeSellerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        binding.fabAddProduct.setOnClickListener {
            // Langsung ke ID fragment tujuan
            findNavController().navigate(R.id.nav_add_product)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadProducts()
    }

    private fun setupRecyclerView() {
        adapter = ProductSellerAdapter(
            layoutResId = R.layout.item_product_seller,
            onItemClick = { product ->
                val bundle = Bundle().apply {
                    putString("productId", product.id)
                }
                findNavController().navigate(R.id.nav_product_detail, bundle)
            }
        )
        binding.rvSellerProducts.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            binding.tvNoProducts.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
            adapter.submitList(products)
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
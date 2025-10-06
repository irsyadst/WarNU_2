package com.imersa.warnu.ui.seller.product

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.imersa.warnu.R
import com.imersa.warnu.data.model.Product
import com.imersa.warnu.databinding.FragmentEditManagerBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditManagerFragment : Fragment() {

    private var _binding: FragmentEditManagerBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var auth: FirebaseAuth

    private val viewModel: EditManagerViewModel by viewModels()
    private lateinit var adapter: ProductSellerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        val sellerId = auth.currentUser?.uid
        if (sellerId != null) {
            viewModel.fetchProducts(sellerId)
        }
    }

    private fun setupRecyclerView() {
        adapter = ProductSellerAdapter(
            layoutResId = R.layout.item_edit_product,
            onItemClick = { product -> navigateToEdit(product) },
            onEditClick = { product -> navigateToEdit(product) },
            onDeleteClick = { product -> showDeleteConfirmationDialog(product) }
        )
        binding.rvManagedProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvManagedProducts.adapter = adapter
    }

    private fun navigateToEdit(product: Product) {
        val bundle = Bundle().apply { putString("productId", product.id) }
        findNavController().navigate(R.id.nav_edit_product, bundle)
    }

    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            binding.tvNoProducts.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
            adapter.submitList(products)
        }
        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.onToastMessageShown()
            }
        }
    }

    private fun showDeleteConfirmationDialog(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete '${product.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                product.id?.let { viewModel.deleteProduct(it, product.imageUrl) }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
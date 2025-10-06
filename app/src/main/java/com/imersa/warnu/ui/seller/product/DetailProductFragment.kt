package com.imersa.warnu.ui.seller.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.bumptech.glide.Glide
import com.imersa.warnu.R
import com.imersa.warnu.data.model.Product
import com.imersa.warnu.databinding.FragmentDetailProductBinding
import com.imersa.warnu.ui.buyer.main.MainBuyerActivity
import com.imersa.warnu.ui.seller.main.MainSellerActivity
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class DetailProductFragment : Fragment() {
    private var _binding: FragmentDetailProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailProductViewModel by viewModels()
    private var currentQuantity = 1
    private var currentProduct: Product? = null
    private var defaultTitle: CharSequence? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appCompatActivity = requireActivity() as AppCompatActivity
        val actionBar = appCompatActivity.supportActionBar

        defaultTitle = actionBar?.title
        actionBar?.hide()

        appCompatActivity.setSupportActionBar(binding.toolbarDetail)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbarDetail.setNavigationOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        val productId = arguments?.getString("productId")
        if (productId == null) {
            Toast.makeText(context, "Product ID not found", Toast.LENGTH_SHORT).show()
            activity?.onBackPressedDispatcher?.onBackPressed()
            return
        }

        viewModel.loadProduct(productId)
        viewModel.loadUserRole()
        observeViewModel()
        setupClickListeners()
    }


    private fun observeViewModel() {
        viewModel.product.observe(viewLifecycleOwner) { product ->
            product?.let {
                currentProduct = it
                binding.tvProductName.text = it.name
                binding.tvProductDescription.text = it.description
                val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                binding.tvProductPrice.text = formatter.format(it.price ?: 0.0)
                binding.tvProductStock.text = "Stock: ${it.stock ?: 0}"
                Glide.with(this)
                    .load(it.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(binding.ivProductImage)
                binding.collapsingToolbar.title = it.name
            }
        }

        viewModel.addToCartStatus.observe(viewLifecycleOwner) { status ->
            status?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.onStatusMessageShown()
                // Aktifkan kembali tombol setelah proses selesai
                binding.fabAddToCart.isEnabled = true
            }
        }

        viewModel.userRole.observe(viewLifecycleOwner) { role ->
            val isBuyer = role == "buyer"
            binding.fabAddToCart.isVisible = isBuyer
            binding.btnIncreaseQuantity.isVisible = isBuyer
            binding.btnDecreaseQuantity.isVisible = isBuyer
            binding.tvQuantity.isVisible = isBuyer
        }
    }

    private fun setupClickListeners() {
        binding.btnIncreaseQuantity.setOnClickListener {
            currentProduct?.stock?.let { stock ->
                if (currentQuantity < stock) {
                    currentQuantity++
                    binding.tvQuantity.text = currentQuantity.toString()
                } else {
                    Toast.makeText(context, "You have reached the maximum stock", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnDecreaseQuantity.setOnClickListener {
            if (currentQuantity > 1) {
                currentQuantity--
                binding.tvQuantity.text = currentQuantity.toString()
            }
        }

        binding.fabAddToCart.setOnClickListener {
            currentProduct?.let { product ->
                // Nonaktifkan tombol untuk mencegah klik ganda
                binding.fabAddToCart.isEnabled = false
                viewModel.addToCart(product, currentQuantity)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as AppCompatActivity).supportActionBar?.title = defaultTitle
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val appCompatActivity = requireActivity() as AppCompatActivity

        // Logika untuk mengembalikan toolbar activity utama
        val mainToolbarId = if (viewModel.userRole.value == "seller") R.id.seller_toolbar else R.id.buyer_toolbar
        val mainNavHostId = if (viewModel.userRole.value == "seller") R.id.fragment_container_seller else R.id.fragment_container_buyer
        val mainAppBarConfig = if (viewModel.userRole.value == "seller") (requireActivity() as? MainSellerActivity)?.appBarConfiguration else (requireActivity() as? MainBuyerActivity)?.appBarConfiguration

        val mainToolbar = appCompatActivity.findViewById<Toolbar>(mainToolbarId)
        mainToolbar?.let {
            appCompatActivity.setSupportActionBar(it)
            appCompatActivity.supportActionBar?.show()
        }

        val navHostFragment = appCompatActivity.supportFragmentManager.findFragmentById(mainNavHostId) as? NavHostFragment
        if (navHostFragment != null && mainAppBarConfig != null) {
            appCompatActivity.setupActionBarWithNavController(navHostFragment.navController, mainAppBarConfig)
        }

        _binding = null
    }
}
package com.imersa.warnu.ui.seller.product

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.imersa.warnu.databinding.FragmentEditProductBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat

@AndroidEntryPoint
class EditProductFragment : Fragment() {

    private var _binding: FragmentEditProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditProductViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private var productId: String? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this)
                .load(it)
                .into(binding.ivProductImagePreview)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productId = arguments?.getString("productId")
        if (productId == null) {
            Toast.makeText(context, "Product ID not found.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        viewModel.loadProduct(productId!!)

        setupCategoryDropdown()
        setupListeners()
        observeViewModel()
    }

    private fun setupCategoryDropdown() {
        val categories = listOf("Fashion", "Electronics", "Home", "Toys", "Books", "Sports")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.btnSelectNewImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnSaveChanges.setOnClickListener {
            productId?.let {
                viewModel.saveChanges(
                    productId = it,
                    name = binding.etProductName.text.toString(),
                    priceStr = binding.etPrice.text.toString(),
                    stockStr = binding.etStock.text.toString(),
                    category = binding.actvCategory.text.toString(),
                    description = binding.etDescription.text.toString(),
                    newImageUri = selectedImageUri
                )
            }
        }
    }

    private fun observeViewModel() {
        viewModel.product.observe(viewLifecycleOwner) { product ->
            product?.let {
                binding.etProductName.setText(it.name)

                val price = it.price ?: 0.0
                val formatter = DecimalFormat("#")
                binding.etPrice.setText(formatter.format(price))

                binding.etStock.setText(it.stock?.toString())
                binding.actvCategory.setText(it.category, false)
                binding.etDescription.setText(it.description)
                Glide.with(this)
                    .load(it.imageUrl)
                    .into(binding.ivProductImagePreview)
            }
        }

        viewModel.editState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state is EditState.Loading
            binding.btnSaveChanges.isEnabled = state !is EditState.Loading

            when (state) {
                is EditState.Success -> {
                    Toast.makeText(context, "Product updated successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                    viewModel.resetState()
                }
                is EditState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    viewModel.resetState()
                }
                else -> {  }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
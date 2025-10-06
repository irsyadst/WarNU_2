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
import com.imersa.warnu.R
import com.imersa.warnu.databinding.FragmentAddProductBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddProductViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivProductImagePreview.isVisible = true
            Glide.with(this)
                .load(it)
                .into(binding.ivProductImagePreview)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        binding.btnSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnAddProduct.setOnClickListener {
            viewModel.addProduct(
                name = binding.etProductName.text.toString(),
                priceStr = binding.etPrice.text.toString(),
                stockStr = binding.etStock.text.toString(),
                category = binding.actvCategory.text.toString(),
                description = binding.etDescription.text.toString(),
                imageUri = selectedImageUri
            )
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state is AddProductState.Loading
            binding.btnAddProduct.isEnabled = state !is AddProductState.Loading

            when (state) {
                is AddProductState.Success -> {
                    Toast.makeText(context, "Product added successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                    viewModel.resetState()
                }
                is AddProductState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    viewModel.resetState()
                }
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
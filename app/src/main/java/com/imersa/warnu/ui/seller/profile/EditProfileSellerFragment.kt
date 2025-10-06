package com.imersa.warnu.ui.seller.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.imersa.warnu.R
import com.imersa.warnu.databinding.FragmentEditProfileSellerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProfileSellerFragment : Fragment() {

    private var _binding: FragmentEditProfileSellerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditProfileSellerViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivProfile.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileSellerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadUserProfile()
        observeViewModel()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnChangePhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val storeName = binding.etStoreName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            viewModel.saveChanges(name, storeName, phone, address, selectedImageUri)
        }
    }

    private fun observeViewModel() {
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.etName.setText(it.name)
                binding.etStoreName.setText(it.storeName)
                binding.etPhone.setText(it.phone)
                binding.etAddress.setText(it.address)
                Glide.with(this)
                    .load(it.profileImageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(binding.ivProfile)
            }
        }

        viewModel.updateStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is UpdateStatus.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                }
                is UpdateStatus.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                    viewModel.resetStatus()
                }
                is UpdateStatus.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(requireContext(), status.message, Toast.LENGTH_SHORT).show()
                    viewModel.resetStatus()
                }
                is UpdateStatus.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.imersa.warnu.ui.buyer.cart

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.imersa.warnu.data.model.CartItem
import com.imersa.warnu.databinding.FragmentCartBinding
import com.imersa.warnu.ui.checkout.CheckoutActivity
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CartViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()

        binding.btnCheckout.setOnClickListener {
            val allOriginalItems = viewModel.getOriginalCartItems()
            if (allOriginalItems.isNotEmpty()) {
                startCheckoutActivity(allOriginalItems)
            } else {
                Toast.makeText(requireContext(), "Your cart is empty.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            cartViewItems = emptyList(),
            onIncrease = { cartItem ->
                viewModel.increaseCartItemQuantity(cartItem)
            },
            onDecrease = { cartItem ->
                if (cartItem.quantity > 1) {
                    viewModel.updateCartItemQuantity(cartItem.productId!!, cartItem.quantity - 1)
                } else {
                    showRemoveConfirmationDialog(cartItem)
                }
            },
            onRemove = { cartItem ->
                showRemoveConfirmationDialog(cartItem)
            }
        )
        binding.rvCartItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cartAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.cartViewItems.observe(viewLifecycleOwner) { items ->
            val isEmpty = items.isNullOrEmpty()
            binding.layoutEmptyCart.isVisible = isEmpty
            binding.rvCartItems.isVisible = !isEmpty
            binding.layoutCheckout.isVisible = !isEmpty
            cartAdapter.updateData(items ?: emptyList())
        }

        viewModel.totalPrice.observe(viewLifecycleOwner) { total ->
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")) as DecimalFormat
            formatter.maximumFractionDigits = 0
            formatter.minimumFractionDigits = 0
            binding.tvTotalPrice.text = formatter.format(total)
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.onToastMessageShown()
            }
        }
    }

    private fun showRemoveConfirmationDialog(cartItem: CartItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove Item")
            .setMessage("Are you sure you want to remove '${cartItem.name}' from your cart?")
            .setPositiveButton("Remove") { dialog, _ ->
                viewModel.removeCartItem(cartItem.productId!!)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun startCheckoutActivity(cartItems: List<CartItem>) {
        val intent = Intent(requireActivity(), CheckoutActivity::class.java).apply {
            putExtra("CART_ITEMS", Gson().toJson(cartItems))
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.imersa.warnu.ui.checkout

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.imersa.warnu.data.model.*
import com.imersa.warnu.databinding.ActivityCheckoutBinding
import com.imersa.warnu.ui.buyer.cart.CartViewModel
import com.imersa.warnu.ui.buyer.main.MainBuyerActivity
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

@AndroidEntryPoint
class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private val cartViewModel: CartViewModel by viewModels()

    @Inject
    lateinit var auth: FirebaseAuth
    @Inject
    lateinit var firestore: FirebaseFirestore

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://warnu-f1434.et.r.appspot.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.progressBar.visibility = View.VISIBLE
        setupWebView()

        val cartItemsJson = intent.getStringExtra("CART_ITEMS")
        if (cartItemsJson != null) {
            val itemType = object : TypeToken<List<CartItem>>() {}.type
            val cartItems: List<CartItem> = Gson().fromJson(cartItemsJson, itemType)
            startCheckout(cartItems)
        } else {
            Toast.makeText(this, "Cart is empty.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.progressBar.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    if (url.contains("finish")) {
                        navigateToHome("Payment Successful")
                        return true
                    } else if (url.contains("unfinish")) {
                        navigateToHome("Payment Pending")
                        return true
                    } else if (url.contains("error")) {
                        navigateToHome("Payment Failed")
                        return true
                    }
                }
                return false
            }
        }
    }


    private fun startCheckout(cartItems: List<CartItem>) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val userName = document.getString("name")
                val userEmail = document.getString("email")
                val userPhone = document.getString("phone")
                val userAddress = document.getString("address")

                if (userAddress.isNullOrEmpty()) {
                    Toast.makeText(this, "Please update your address in your profile.", Toast.LENGTH_LONG).show()
                    finish()
                    return@addOnSuccessListener
                }

                val customerDetails = CustomerDetails(
                    userId = userId,
                    name = userName,
                    email = userEmail,
                    phone = userPhone,
                    address = userAddress
                )

                val allItemDetails = cartItems.map {
                    ItemDetails(
                        id = it.productId!!,
                        price = it.price!!,
                        quantity = it.quantity,
                        name = it.name!!,
                        imageUrl = it.imageUrl,
                        sellerId = it.sellerId,
                        storeName = it.storeName
                    )
                }
                val transactionRequest = MultiVendorTransactionRequest(
                    allItems = allItemDetails,
                    customerDetails = customerDetails
                )


                apiService.createMultiVendorTransaction(transactionRequest).enqueue(object : retrofit2.Callback<TransactionResponse> {
                    override fun onResponse(call: retrofit2.Call<TransactionResponse>, response: retrofit2.Response<TransactionResponse>) {
                        binding.progressBar.visibility = View.GONE
                        if (response.isSuccessful) {
                            val token = response.body()?.token
                            if (!token.isNullOrEmpty()) {
                                val midtransUrl = "https://app.sandbox.midtrans.com/snap/v2/vtweb/$token"
                                binding.webView.loadUrl(midtransUrl)
                            } else {
                                Toast.makeText(this@CheckoutActivity, "Failed to get payment token.", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            val errorMessage = try { JSONObject(errorBody!!).getString("error") } catch (e: Exception) { "Unknown server error." }
                            Toast.makeText(this@CheckoutActivity, "Checkout failed: $errorMessage", Toast.LENGTH_LONG).show()
                            finish()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<TransactionResponse>, t: Throwable) {
                        binding.progressBar.visibility = View.GONE
                        Log.e("CheckoutActivity", "API Call Failure", t)
                        Toast.makeText(this@CheckoutActivity, "Failed to connect to server: ${t.message}", Toast.LENGTH_LONG).show()
                        finish()
                    }
                })
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to get user data.", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun navigateToHome(message: String) {
        if (!isFinishing) {
            if (message == "Payment Successful") {
                cartViewModel.clearCart()
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainBuyerActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}
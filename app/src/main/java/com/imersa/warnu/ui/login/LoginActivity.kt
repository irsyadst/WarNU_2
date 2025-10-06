package com.imersa.warnu.ui.login

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.imersa.warnu.R
import com.imersa.warnu.databinding.ActivityLoginBinding
import com.imersa.warnu.ui.buyer.main.MainBuyerActivity
import com.imersa.warnu.ui.register.buyer.RegisterBuyerActivity
import com.imersa.warnu.ui.register.seller.RegisterSellerActivity
import com.imersa.warnu.ui.seller.main.MainSellerActivity
import dagger.hilt.android.AndroidEntryPoint

@Suppress("DEPRECATION")
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white_login)
        setContentView(binding.root)

        setupListeners()
        observeLoginState()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            viewModel.login(email, password)
        }

        binding.ivShowPassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility()
        }

        binding.tvSignupBuyer.setOnClickListener {
            startActivity(Intent(this, RegisterBuyerActivity::class.java))
        }

        binding.tvSignupSeller.setOnClickListener {
            startActivity(Intent(this, RegisterSellerActivity::class.java))
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            binding.etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.ivShowPassword.setImageResource(R.drawable.ic_open_eye)
        } else {
            binding.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.ivShowPassword.setImageResource(R.drawable.ic_closed_eye)
        }

        binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
    }


    private fun observeLoginState() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> {
                    binding.pbLogin.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                }
                is LoginState.Success -> {
                    binding.pbLogin.visibility = View.GONE
                    navigateToHome(state.role)
                }
                is LoginState.Error -> {
                    binding.pbLogin.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navigateToHome(role: String) {
        val intent = when (role) {
            "seller" -> Intent(this, MainSellerActivity::class.java)
            "buyer" -> Intent(this, MainBuyerActivity::class.java)
            else -> {
                Toast.makeText(this, "Invalid role: $role", Toast.LENGTH_SHORT).show()
                return
            }
        }
        startActivity(intent)
        finish()
    }
}
package com.imersa.warnu

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.imersa.warnu.ui.buyer.main.MainBuyerActivity
import com.imersa.warnu.ui.login.LoginActivity
import com.imersa.warnu.ui.seller.main.MainSellerActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@Suppress("DEPRECATION")
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var auth: FirebaseAuth
    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Make status bar transparent
        window.apply {
            statusBarColor = android.graphics.Color.TRANSPARENT
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserSession()
        }, 2000)
    }

    private fun checkUserSession() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val role = document.getString("role")
                        if (role == "buyer") {
                            navigateTo(MainBuyerActivity::class.java)
                        } else {
                            navigateTo(MainSellerActivity::class.java)
                        }
                    } else {
                        navigateTo(LoginActivity::class.java)
                    }
                }
                .addOnFailureListener {
                    navigateTo(LoginActivity::class.java)
                }
        } else {
            navigateTo(LoginActivity::class.java)
        }
    }

    private fun navigateTo(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
        finish()
    }
}
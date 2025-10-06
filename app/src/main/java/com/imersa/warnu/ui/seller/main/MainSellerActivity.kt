package com.imersa.warnu.ui.seller.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.imersa.warnu.R
import com.imersa.warnu.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainSellerActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var navController: NavController
    lateinit var appBarConfiguration: AppBarConfiguration
    private val viewModel: MainSellerViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_seller)
        window.statusBarColor = ContextCompat.getColor(this, R.color.green)

        val toolbar = findViewById<Toolbar>(R.id.seller_toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout_seller)
        navigationView = findViewById(R.id.navigation_view_seller)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container_seller) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_profile, R.id.nav_manage_product, R.id.nav_order
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)

        val headerView = navigationView.getHeaderView(0)
        val textViewWelcome = headerView.findViewById<TextView>(R.id.tv_nav_header)

        viewModel.name.observe(this) { name ->
            textViewWelcome.text = "Welcome, $name"
        }
        viewModel.userNotFound.observe(this) { notFound ->
            if (notFound) textViewWelcome.text = "Failed to load user"
        }

        viewModel.loadUserData()

        navigationView.menu.findItem(R.id.nav_order_history).isVisible = false

        navigationView.setNavigationItemSelectedListener { menuItem ->

            drawerLayout.closeDrawer(GravityCompat.START)

            if (menuItem.itemId == R.id.nav_logout) {
                viewModel.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return@setNavigationItemSelectedListener true
            }

            return@setNavigationItemSelectedListener NavigationUI.onNavDestinationSelected(menuItem, navController)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(
            navController,
            appBarConfiguration
        ) || super.onSupportNavigateUp()
    }
}
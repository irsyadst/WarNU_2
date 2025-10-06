package com.imersa.warnu.ui.buyer.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
class MainBuyerActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var navController: NavController
    lateinit var appBarConfiguration: AppBarConfiguration

    private val viewModel: MainBuyerViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_buyer)

        val toolbar = findViewById<Toolbar>(R.id.buyer_toolbar)
        setSupportActionBar(toolbar)
        window.statusBarColor = ContextCompat.getColor(this, R.color.green)

        drawerLayout = findViewById(R.id.drawer_layout_buyer)
        navigationView = findViewById(R.id.navigation_view_buyer)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container_buyer) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_profile, R.id.nav_order_history
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)

        navigationView.getHeaderView(0)?.let { headerView ->
            val textViewWelcome = headerView.findViewById<TextView>(R.id.tv_nav_header)
            viewModel.name.observe(this) { name ->
                textViewWelcome.text = "Welcome, $name"
            }
            viewModel.userNotFound.observe(this) { notFound ->
                if (notFound) textViewWelcome.text = "Failed to load user"
            }
        }

        viewModel.loadUserData()

        navigationView.menu.findItem(R.id.nav_manage_product)?.isVisible = false
        navigationView.menu.findItem(R.id.nav_order)?.isVisible = false

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

        navController.addOnDestinationChangedListener { _, destination, _ ->
            invalidateOptionsMenu()
            if (destination.id == R.id.nav_cart) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_buyer_toolbar, menu)
        val cartMenu = menu.findItem(R.id.action_cart)
        val currentDestination = navController.currentDestination?.id
        cartMenu?.isVisible = currentDestination != R.id.nav_cart
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cart -> {
                navController.navigate(R.id.nav_cart)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(
            navController,
            appBarConfiguration
        ) || super.onSupportNavigateUp()
    }
}
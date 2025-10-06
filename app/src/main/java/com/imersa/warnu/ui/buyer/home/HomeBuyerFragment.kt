package com.imersa.warnu.ui.buyer.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.imersa.warnu.R
import com.imersa.warnu.databinding.FragmentHomeBuyerBinding
import com.imersa.warnu.ui.buyer.product.BannerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class HomeBuyerFragment : Fragment() {

    private var _binding: FragmentHomeBuyerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeBuyerViewModel by viewModels()
    private lateinit var productAdapter: HomeBuyerAdapter

    private val sliderHandler = Handler(Looper.getMainLooper())
    private lateinit var sliderRunnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBuyerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupBanner()
        setupSearchView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        productAdapter = HomeBuyerAdapter(
            onItemClick = { product ->
                val bundle = Bundle().apply {
                    putString("productId", product.id)
                }
                findNavController().navigate(R.id.nav_product_detail, bundle)
            }
        )
        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productAdapter
        }
    }

    private fun setupBanner() {
        val banners = listOf(R.drawable.banner1, R.drawable.banner2, R.drawable.banner3)
        val fakeList = mutableListOf<Int>()

        fakeList.add(banners.last())
        fakeList.addAll(banners)
        fakeList.add(banners.first())

        val adapter = BannerAdapter(fakeList)
        binding.vpBanner.adapter = adapter
        binding.vpBanner.setCurrentItem(1, false) // mulai di item pertama yang asli

        val compositePageTransformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(40))
            addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.85f + r * 0.15f
            }
        }
        binding.vpBanner.setPageTransformer(compositePageTransformer)

        // Auto-slide
        sliderRunnable = Runnable {
            binding.vpBanner.currentItem = binding.vpBanner.currentItem + 1
            sliderHandler.postDelayed(sliderRunnable!!, 5000)
        }

        binding.vpBanner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                restartAutoSlide()
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    val itemCount = adapter.itemCount
                    when (binding.vpBanner.currentItem) {
                        0 -> binding.vpBanner.setCurrentItem(itemCount - 2, false)
                        itemCount - 1 -> binding.vpBanner.setCurrentItem(1, false)
                    }
                }
            }
        })

        startAutoSlide()
    }

    private fun startAutoSlide() {
        sliderRunnable?.let { sliderHandler.postDelayed(it, 5000) }
    }

    private fun stopAutoSlide() {
        sliderRunnable?.let { sliderHandler.removeCallbacks(it) }
    }

    private fun restartAutoSlide() {
        stopAutoSlide()
        startAutoSlide()
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchProducts(newText.orEmpty())
                return true
            }
        })
    }


    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            if (products.isEmpty()) {
                binding.tvNoProducts.visibility = View.VISIBLE
                binding.rvProducts.visibility = View.GONE
            } else {
                binding.tvNoProducts.visibility = View.GONE
                binding.rvProducts.visibility = View.VISIBLE
                productAdapter.submitList(products)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        stopAutoSlide()
        super.onDestroyView()
        _binding = null
    }

}
package com.imersa.warnu.ui.buyer.product

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.imersa.warnu.R

class BannerAdapter(private val bannerImages: List<Int>) :
    RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }
    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(bannerImages[position])
    }
    override fun getItemCount(): Int = bannerImages.size

    class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.iv_banner)

        fun bind(imageResId: Int) {
            imageView.setImageResource(imageResId)
        }
    }
}
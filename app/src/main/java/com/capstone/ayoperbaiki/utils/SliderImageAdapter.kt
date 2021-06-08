package com.capstone.ayoperbaiki.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.capstone.ayoperbaiki.R
import com.capstone.ayoperbaiki.databinding.ItemSliderImageBinding
import com.smarteist.autoimageslider.SliderViewAdapter

class SliderImageAdapter : SliderViewAdapter<SliderImageAdapter.ImageViewHolder>() {

    private var listData = ArrayList<String>()

    fun setData(newListData: List<String>?) {
        if (newListData == null) return
        listData.clear()
        listData.addAll(newListData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup): SliderImageAdapter.ImageViewHolder {
        return ImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_slider_image, parent, false))
    }

    override fun onBindViewHolder(viewHolder: SliderImageAdapter.ImageViewHolder, position: Int) {
        viewHolder.bind(listData[position])
    }

    override fun getCount(): Int = listData.size


    inner class ImageViewHolder(itemView: View) : SliderViewAdapter.ViewHolder(itemView) {
        private val binding = ItemSliderImageBinding.bind(itemView)

        fun bind(path: String) {
            Glide.with(itemView.context)
                .load(path)
                .into(binding.imageDisaster)
        }
    }
}
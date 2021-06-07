package com.capstone.ayoperbaiki.utils


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.capstone.ayoperbaiki.R
import com.capstone.ayoperbaiki.databinding.ItemAddPhotoBinding
import com.capstone.ayoperbaiki.databinding.ItemPhotoBinding
import com.capstone.ayoperbaiki.utils.Utils.LIMIT_PICTURE
import com.capstone.ayoperbaiki.utils.Utils.hide

class GridPhotoAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listData = ArrayList<String>()
    var onItemClick: ((Int) -> Unit)? = null
    var onButtonClick: (() -> Unit)? = null

    fun setData(newListData: List<String>?) {
        if (newListData == null) return
        listData.clear()
        listData.addAll(newListData)
        notifyDataSetChanged()
    }

    fun addData(newData: String?) {
        if (newData == null) return
        listData.add(newData)
        notifyDataSetChanged()
    }

    fun deleteDataAtIndex(position: Int) {
        listData.removeAt(position)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_add_photo -> ButtonViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_add_photo, parent, false))
            R.layout.item_photo -> ThumbnailViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false))
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            R.layout.item_photo -> (holder as ThumbnailViewHolder).bind(listData[position], position)
            R.layout.item_add_photo -> (holder as ButtonViewHolder).bind()
        }
    }

    override fun getItemCount(): Int {
        if (listData.size + 1 > LIMIT_PICTURE) {
            return LIMIT_PICTURE
        }
        return listData.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return when(position) {
            listData.size -> R.layout.item_add_photo
            else -> R.layout.item_photo
        }
    }

    inner class ThumbnailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemPhotoBinding.bind(itemView)

        fun bind(path: String, position: Int) {
            Glide.with(itemView.context)
                .load(path)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding.imgDisaster)

            binding.imgDisaster.setOnLongClickListener {
                onItemClick?.invoke(position)
                true
            }
        }
    }

    inner class ButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemAddPhotoBinding.bind(itemView)
        fun bind() {
            binding.btnAddPhoto.setOnClickListener {
                onButtonClick?.invoke()
            }
        }
    }
    
}
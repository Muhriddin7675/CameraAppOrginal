package com.example.cameraapporginal.presentantion.all_photos

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.example.cameraapporginal.R
import com.example.cameraapporginal.databinding.ItemImgBinding

class ChatImageAdapter :Adapter<ChatImageAdapter.Holder>(){
    var myCursor:Cursor?=null
    var uriImg:Uri?=null
    var currentPos=-1
    val isClickLiveData=MutableLiveData<Boolean>()
    var itemClickListener: ((String) -> Unit)? = null

    inner class Holder(private val binding: ItemImgBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item:Uri){
            Glide.with(binding.root.context)
                .load(item)
                .placeholder(R.color.black)
                .centerCrop()
                .into(binding.img)

            binding.root.setOnClickListener {
                itemClickListener?.invoke(item.toString())
                if (currentPos==-1){
                    currentPos=adapterPosition
                    uriImg=item

                    isClickLiveData.value=true
                }else if (currentPos==adapterPosition){
                    currentPos=-1
                    isClickLiveData.value=false
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder = Holder(ItemImgBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun getItemCount(): Int=myCursor?.count?:0

    override fun onBindViewHolder(holder: Holder, position: Int) {
        myCursor?.let {
            val idColumn =it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            it.moveToPosition(position)
            val id = it.getLong(idColumn)
            val contentUri: Uri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            holder.bind(contentUri)
        }
    }




}

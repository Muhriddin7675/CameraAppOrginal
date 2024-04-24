package com.example.cameraapporginal.presentantion.all_photos

import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.cameraapporginal.R
import com.example.cameraapporginal.databinding.ScreenAllPhotosBinding
import com.example.cameraapporginal.presentantion.capture.toast

class AllPhotosScreen : Fragment(R.layout.screen_all_photos) {
    private val binding by viewBinding(ScreenAllPhotosBinding::bind)
    private val adapter by lazy { ChatImageAdapter() }
    private val navController by lazy(LazyThreadSafetyMode.NONE) { findNavController() }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initAdapter()
        initActions()
    }

    private fun initActions() {
        binding.apply {
            btnBack.setOnClickListener { navController.navigateUp() }

            btnSelect.setOnClickListener {
                "This feature added a play market version".toast(requireContext())
//                appBarSelected.visibility = View.VISIBLE
            }

            btnCheck.setOnClickListener { appBarSelected.visibility = View.GONE }

            adapter.itemClickListener = {
                navController.navigate(AllPhotosScreenDirections.actionAllPhotosScreenToViewerScreen(it))
            }

        }

    }

    private fun initAdapter(){
        binding.apply{
            recyclerView.adapter=adapter
            binding.recyclerView.layoutManager= GridLayoutManager(binding.root.context,3)
        }
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN
        )
        val cursor = binding.root.context?.contentResolver?.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        )
        adapter.myCursor=cursor


    }
}
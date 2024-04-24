package com.example.cameraapporginal.presentantion.viewer

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.cameraapporginal.R
import com.example.cameraapporginal.databinding.ScreenViewerBinding
import uz.gita.camera_app_product.presentation.viewer.ViewerScreenArgs


class ViewerScreen : Fragment(R.layout.screen_viewer) {
    private val binding by viewBinding(ScreenViewerBinding::bind)
    private val navController by lazy(LazyThreadSafetyMode.NONE) { findNavController() }
    private lateinit var dialog: Dialog
    private val navArgs:ViewerScreenArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = Dialog(requireContext())
        val uri = navArgs.uri
        binding.imagePreview.setImageURI(uri.toUri())
        initActions()
    }

    private fun initActions() {
        binding.apply {
            btnBack.setOnClickListener { navController.navigateUp() }
        }
    }
}
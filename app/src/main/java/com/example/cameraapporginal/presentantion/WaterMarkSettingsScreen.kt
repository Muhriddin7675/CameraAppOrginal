package com.example.cameraapporginal.presentantion

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.cameraapporginal.R
import com.example.cameraapporginal.databinding.ScreenWatermarkSettingsBinding


class WaterMarkSettingsScreen : Fragment(R.layout.screen_watermark_settings) {
    private val binding by viewBinding(ScreenWatermarkSettingsBinding::bind)
    private val navController by lazy(LazyThreadSafetyMode.NONE) { findNavController() }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initActions()
    }
    private fun initActions() {
        binding.apply {
            btnBack.setOnClickListener { navController.navigateUp() }
        }
    }
}
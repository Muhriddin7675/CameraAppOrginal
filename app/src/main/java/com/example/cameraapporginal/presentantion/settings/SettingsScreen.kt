package com.example.cameraapporginal.presentantion.settings

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.cameraapporginal.R
import com.example.cameraapporginal.data.SharedPref
import com.example.cameraapporginal.databinding.ScreenSettingsBinding


class SettingsScreen : Fragment(R.layout.screen_settings) {
    private var isTouched: Boolean = false
    private val binding by viewBinding(ScreenSettingsBinding::bind)
    private val navController by lazy(LazyThreadSafetyMode.NONE) { findNavController() }
    private val shared = SharedPref.getInstance()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initActions()
        val window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor("#000001")
        window.navigationBarColor = Color.parseColor("#000001")

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        };
    }

    private fun initActions() {
        binding.apply {
            btnBack.setOnClickListener { navController.navigateUp() }

            eventText1.setOnClickListener {
                navController.navigate(SettingsScreenDirections.actionSettingsScreenToWaterMarkSettingsScreen())
            }
            eventText3.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.freeprivacypolicy.com/live/044fbcd0-09ab-455e-84bd-0ab1b2f3187d")
                    )
                )

            }
            eventText5.setOnClickListener {
                shared.clear()
                findNavController().navigateUp()
            }
            eventText6.setOnClickListener {

            }
            eventText7.setOnClickListener {
                val intent =
                    Intent(
                        Intent.ACTION_SENDTO,
                        Uri.fromParts("mailto", "abdulbosit9730@gmail.com", null)
                    )
                intent.putExtra(Intent.EXTRA_SUBJECT, "Subject")
                intent.putExtra(Intent.EXTRA_TEXT, "")
                startActivity(Intent.createChooser(intent, "Choose an Email client :"))
            }
            manageState1.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isTouched) {
                    isTouched = false
                    shared.watermark = isChecked
                }
            }
            manageState2.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isTouched) {
                    isTouched = false
                    shared.volumePress = isChecked
                }
            }
        }
    }
}
//private fun initActions() {
//        binding.apply {
//            tvRateApp.setOnSingleClickListener {
//                startActivity(
//                    Intent(
//                        Intent.ACTION_VIEW,
//                        Uri.parse("market://details?id=uz.gita.abdulbosit.book_store")
//                    )
//                )
//            }
//
//            tvEditProfile.setOnSingleClickListener {
//                findNavController().navigate(MainScreenDirections.actionMainScreenToEditProfileScreen())
//            }
//            tvHelpCenter.setOnSingleClickListener {
//
//                val intent =
//                    Intent(
//                        Intent.ACTION_SENDTO,
//                        Uri.fromParts("mailto", "abdulbosit9730@gmail.com", null)
//                    )
//                intent.putExtra(Intent.EXTRA_SUBJECT, "Subject")
//                intent.putExtra(Intent.EXTRA_TEXT, "")
//                startActivity(Intent.createChooser(intent, "Choose an Email client :"))
//            }
//            tvPrivacyPolicy.setOnSingleClickListener {
//                startActivity(
//                    Intent(
//                        Intent.ACTION_VIEW,
//                        Uri.parse("https://www.freeprivacypolicy.com/live/f5d8f141-d333-4e8e-819d-507e09be8170")
//                    )
//                )
//            }
//            tvShareApp.setOnSingleClickListener {
//                val intent = Intent(Intent.ACTION_SEND)
//                val shareBody =
//                    "Tavsiya etaman: ${R.string.app_name}\n\nhttps://play.google.com/store/apps/details?id=uz.gita.abdulbosit.book_store"
//                intent.setType("text/plain")
//                intent.putExtra(
//                    Intent.EXTRA_SUBJECT,
//                    getString(R.string.app_name)
//                )
//                intent.putExtra(Intent.EXTRA_TEXT, shareBody)
//                startActivity(Intent.createChooser(intent, getString(R.string.app_name)))
//            }
//
//            tvLogOut.setOnSingleClickListener {
//                openWarningDialog()
//            }
//
//        }
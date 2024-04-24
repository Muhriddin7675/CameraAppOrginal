package com.example.cameraapporginal.presentantion.preview

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.print.PrintHelper
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.cameraapporginal.R
import com.example.cameraapporginal.databinding.FragmentPhotoPreviewBinding
import com.example.cameraapporginal.util.setDialogConfigurations
import java.io.File


class PhotoPreviewScreen : Fragment(R.layout.fragment_photo_preview) {
    private val navArgs = navArgs<PhotoPreviewScreenArgs>()
    private val binding by viewBinding(FragmentPhotoPreviewBinding::bind)
    private val navController by lazy(LazyThreadSafetyMode.NONE) { findNavController() }
    private lateinit var dialog: Dialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = Dialog(requireContext())
        val uri = navArgs.value.uri
        binding.imagePreview.setImageURI(uri.toUri())
        initActions()
    }

    private fun initActions() {
        binding.apply {
            btnBack.setOnClickListener { navController.navigateUp() }

            btnAll.setOnClickListener { navController.navigate(PhotoPreviewScreenDirections.actionPhotoPreviewScreenToAllPhotosScreen()) }

            btnPrinter.setOnClickListener {
                val photoPrinter = PrintHelper(requireContext())
                photoPrinter.scaleMode = PrintHelper.SCALE_MODE_FIT
                val imageUri: Uri = navArgs.value.uri.toUri()
                val bitmap =
                    MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
                photoPrinter.printBitmap("test print", bitmap)
            }

            btnInfo.setOnClickListener {
                showInfoImageDialog()
            }


            btnShare.setOnClickListener {
                val share = Intent(Intent.ACTION_SEND)
                share.setType("image/jpeg")
                val imageUri = navArgs.value.uri.toUri()
                share.putExtra(Intent.EXTRA_STREAM, imageUri)
                startActivity(Intent.createChooser(share, "Select"))
            }
        }
    }

    private fun showInfoImageDialog() {
        dialog.setContentView(R.layout.dialog_image_path)
        dialog.findViewById<TextView>(R.id.btn_ok).setOnClickListener { cancelDialog() }
        dialog.findViewById<TextView>(R.id.path).text = navArgs.value.uri.toUri().path.toString()

        dialog.setDialogConfigurations(Gravity.CENTER, false)
    }

    private fun showDeleteDialog() {
        dialog.setContentView(R.layout.dialog_delete_image)
        dialog.findViewById<TextView>(R.id.btn_yes).setOnClickListener {
            val deletedImageFile = navArgs.value.uri.toUri().path?.let { it1 -> File(it1) }

            if (deletedImageFile!!.exists()) {
                deletedImageFile.delete()
            }
            cancelDialog()
        }
        dialog.findViewById<TextView>(R.id.btn_no).setOnClickListener { cancelDialog() }

        dialog.setDialogConfigurations(Gravity.CENTER, false)
    }

    private fun cancelDialog() = dialog.dismiss()
}
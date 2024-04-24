package com.example.cameraapporginal.util

import android.app.ActionBar
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.annotation.GravityInt

fun Dialog.setDialogConfigurations(@GravityInt center: Int, cancelable:Boolean) {
    this.show()
    this.window?.setGravity(center)
    this.setCancelable(cancelable)
    this.window?.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
    this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
}

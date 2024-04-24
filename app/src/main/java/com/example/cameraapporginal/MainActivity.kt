package com.example.cameraapporginal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import com.example.cameraapporginal.util.MyEventBus

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Toast.makeText(this, "Down", Toast.LENGTH_SHORT).show()
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            MyEventBus.connectedEvent?.invoke()
            Toast.makeText(this, "Up", Toast.LENGTH_SHORT).show()
        }
        return true
    }
}
package com.example.cameraapporginal.data

import android.content.Context
import android.content.SharedPreferences

class SharedPref private constructor(context: Context) {
    fun clear() {
        editor.clear().apply()
    }


    private val preferences: SharedPreferences =
        context.getSharedPreferences("SHARED_PREFERENCES", Context.MODE_PRIVATE)

    private val editor = preferences.edit()

    //    var bestMoves: Int
//        get() = preferences.getInt(BEST_SCORE, 0)
//        set(value) = editor.putInt(BEST_SCORE, value).apply()
//
//    var bestTime: Long
//        get() = preferences.getLong(BEST_TIME, -1)
//        set(value) = editor.putLong(BEST_TIME, value).apply()
//
    var numbers: String?
        get() = preferences.getString("NUMBERS", "1#2#3#4#5#6#7#8#9#10#11#12#13#14#15##")
        set(value) = editor.putString("NUMBERS", value).apply()
    var volumePress: Boolean
        get() = preferences.getBoolean("pressVolumeKey", false)
        set(value) = editor.putBoolean("pressVolumeKey", value).apply()

    var watermark: Boolean
        get() = preferences.getBoolean("watermark", false)
        set(value) = editor.putBoolean("watermark", value).apply()

    var watermarkPos: Int
        get() = preferences.getInt("watermarkPos", 0)
        set(value) = editor.putInt("watermarkPos", value).apply()

    var cameraType: Int
        get() = preferences.getInt("cameraType", 0)
        set(value) = editor.putInt("cameraType", value).apply()

    var timerType: Int
        get() = preferences.getInt("timerType", 0)
        set(value) = editor.putInt("timerType", value).apply()

    var aspectRatio: Int
        get() = preferences.getInt("aspectRatio", 0)
        set(value) = editor.putInt("aspectRatio", value).apply()
//
//
//    var isPlaying: Boolean
//        get() = preferences.getBoolean(IS_PLAYING, false)
//        set(value) = editor.putBoolean(IS_PLAYING, value).apply()
//
//    var score: Int
//        get() = preferences.getInt(SCORE, 0)
//        set(value) = editor.putInt(SCORE, value).apply()
//
//    var pauseTime: Long
//        get() = preferences.getLong(PAUSE_TIME, 0)
//        set(value) = editor.putLong(PAUSE_TIME, value).apply()
//
//    var musicPosition: Int
//        get() = preferences.getInt(MUSIC_POSITION, 0)
//        set(value) = editor.putInt(MUSIC_POSITION, value).apply()
//
//    var isMusic: Boolean
//        get() = preferences.getBoolean(IS_MUSIC, false)
//        set(value) = editor.putBoolean(IS_MUSIC, value).apply()

    companion object {
        private lateinit var instance: SharedPref

        fun init(context: Context) {
            instance = SharedPref(context)
        }

        fun getInstance() = instance
    }
}
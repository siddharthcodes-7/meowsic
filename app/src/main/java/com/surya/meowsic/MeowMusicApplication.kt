package com.surya.meowsic

import android.app.Application
import com.surya.meowsic.data.stream.YouTubeStreamResolver

class MeowMusicApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize NewPipe safely at application startup
        YouTubeStreamResolver.initialize()
    }
}

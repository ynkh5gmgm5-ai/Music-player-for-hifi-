package com.yuandao.music

import android.app.Application
import com.yuandao.music.core.AppContainer

class YuandaoApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}


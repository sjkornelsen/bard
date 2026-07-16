package com.stan.libbylight

import android.app.Application

class LibbyLightApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LibbyWebPlayer.init(this)
    }
}

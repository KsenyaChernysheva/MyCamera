package com.kpfu.itis.mycamera

import android.app.Application
import com.kpfu.itis.mycamera.di.Injector
import moxy.MvpFacade

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        MvpFacade.init()
        Injector.init(this)
    }
}
package com.kpfu.itis.mycamera.di

import com.kpfu.itis.mycamera.App
import com.kpfu.itis.mycamera.di.component.AppComponent
import com.kpfu.itis.mycamera.di.component.DaggerAppComponent

object Injector {

    lateinit var appComponent: AppComponent

    fun init(app: App) {
        appComponent = DaggerAppComponent.builder()
            .application(app)
            .build()
    }

}
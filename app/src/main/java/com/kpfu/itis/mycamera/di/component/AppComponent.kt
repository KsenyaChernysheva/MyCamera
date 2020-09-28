package com.kpfu.itis.mycamera.di.component

import android.content.Context
import com.kpfu.itis.mycamera.App
import com.kpfu.itis.mycamera.di.module.AppModule
import com.kpfu.itis.mycamera.di.module.CameraModule
import com.kpfu.itis.mycamera.presentation.fragments.AddDialogFragment
import com.kpfu.itis.mycamera.presentation.fragments.CameraFragment
import com.kpfu.itis.mycamera.presentation.fragments.PermissionsFragment
import com.kpfu.itis.mycamera.presentation.fragments.PresetsFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, CameraModule::class])
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: App): Builder
        fun build(): AppComponent
    }

    fun getContext(): Context

    fun inject(fragment: AddDialogFragment)

    fun inject(fragment: CameraFragment)

    fun inject(fragment: PresetsFragment)

    fun inject(fragment: PermissionsFragment)
}
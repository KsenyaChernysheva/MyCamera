package com.kpfu.itis.mycamera.di.module

import android.content.Context
import com.kpfu.itis.mycamera.App
import com.kpfu.itis.mycamera.domain.PermissionService
import com.kpfu.itis.mycamera.domain.PreferencesService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideContext(application: App): Context = application.applicationContext

    @Provides
    @Singleton
    fun providePermissionService(): PermissionService = PermissionService()

    @Provides
    @Singleton
    fun provideSharedPreferencesService(): PreferencesService = PreferencesService()

}
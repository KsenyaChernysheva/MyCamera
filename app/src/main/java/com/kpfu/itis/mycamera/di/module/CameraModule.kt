package com.kpfu.itis.mycamera.di.module

import com.kpfu.itis.mycamera.domain.StorageService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CameraModule {

    @Provides
    @Singleton
    fun provideStorageService(): StorageService = StorageService()

}
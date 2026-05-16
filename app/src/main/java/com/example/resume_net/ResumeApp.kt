package com.example.resume_net

import android.app.Application
import android.util.Log
import com.example.resume_net.di.appModule
import com.example.resume_net.di.dataModule
import com.example.resume_net.di.domainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ResumeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ResumeApp)
            modules(domainModule, dataModule, appModule)
        }

        Log.d("ResumeApp", "Application started, model will be loaded on first analysis")
    }
}
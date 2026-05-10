package com.example.resume_net

import android.app.Application
import com.example.resume_net.data.repository.ResumeRepositoryImpl
import com.example.resume_net.di.appModule
import com.example.resume_net.di.dataModule
import com.example.resume_net.di.domainModule
import com.example.resume_net.domain.repository.ResumeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ResumeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ResumeApp)
            modules(domainModule, dataModule, appModule)
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = get<ResumeRepository>() as ResumeRepositoryImpl
                repo.loadModel()
            } catch (e: Exception) {
                android.util.Log.e("ResumeApp", "Failed to load model", e)
            }
        }
    }
}
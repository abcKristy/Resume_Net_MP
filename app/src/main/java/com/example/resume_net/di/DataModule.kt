package com.example.resume_net.di

import com.example.resume_net.data.cache.AnalysisCache
import com.example.resume_net.data.db.AppDatabase
import com.example.resume_net.data.repository.ModelDownloader
import com.example.resume_net.data.repository.ResumeRepositoryImpl
import com.example.resume_net.data.tokenizer.BertTokenizer
import com.example.resume_net.domain.repository.ResumeRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single { BertTokenizer(androidContext()) }
    single { ModelDownloader(androidContext()) }
    single { AppDatabase.getInstance(androidContext()) }
    single { get<AppDatabase>().analysisDao() }
    single { AnalysisCache(get()) }

    single<ResumeRepository> {
        ResumeRepositoryImpl(
            context = androidContext(),
            tokenizer = get(),
            modelDownloader = get()
        )
    }
}
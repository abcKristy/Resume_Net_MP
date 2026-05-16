package com.example.resume_net.di

import com.example.resume_net.data.cache.AnalysisCache
import com.example.resume_net.data.db.AppDatabase
import com.example.resume_net.data.repository.ConversationRepositoryImpl
import com.example.resume_net.data.repository.ModelDownloader
import com.example.resume_net.data.repository.ResumeRepositoryImpl
import com.example.resume_net.data.tokenizer.BertTokenizer
import com.example.resume_net.domain.repository.ConversationRepository
import com.example.resume_net.domain.repository.ResumeRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single { BertTokenizer(androidContext()) }
    single { ModelDownloader(androidContext()) }
    single { AppDatabase.getInstance(androidContext()) }
    single { get<AppDatabase>().analysisDao() }
    single { AnalysisCache(get()) }
    single { get<AppDatabase>().conversationDao() }
    single { get<AppDatabase>().messageDao() }

    single<ResumeRepository> {
        ResumeRepositoryImpl(
            context = androidContext(),
            tokenizer = get(),
            modelDownloader = get(),
            analysisCache = get()
        )
    }

    single<ConversationRepository> {
        ConversationRepositoryImpl(
            conversationDao = get(),
            messageDao = get()
        )
    }
}
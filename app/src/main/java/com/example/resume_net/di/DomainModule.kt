package com.example.resume_net.di

import com.example.resume_net.domain.usecase.AnalyzeResumeUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { AnalyzeResumeUseCase(get()) }
}
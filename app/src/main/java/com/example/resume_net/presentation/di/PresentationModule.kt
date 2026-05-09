package com.example.resume_net.presentation.di

import com.example.resume_net.presentation.viewmodel.ResumeViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val presentationModule: Module = module {
    factory { params ->
        ResumeViewModel(
            analyzeResumeUseCase = get(),
            getModelStatusUseCase = get(),
            validateTextUseCase = get()
        )
    }
}
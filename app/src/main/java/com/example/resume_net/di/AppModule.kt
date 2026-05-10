package com.example.resume_net.di

import com.example.resume_net.presentation.analysis.AnalysisViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { AnalysisViewModel(get()) }
}
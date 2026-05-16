package com.example.resume_net.di

import com.example.resume_net.domain.usecase.AddMessageUseCase
import com.example.resume_net.domain.usecase.AnalyzeAndAddToConversationUseCase
import com.example.resume_net.domain.usecase.AnalyzeResumeUseCase
import com.example.resume_net.domain.usecase.CreateConversationUseCase
import com.example.resume_net.domain.usecase.DeleteConversationUseCase
import com.example.resume_net.domain.usecase.GetConversationsUseCase
import com.example.resume_net.presentation.conversations.ConversationListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val domainModule = module {
    factory { AnalyzeResumeUseCase(get()) }

    factory { CreateConversationUseCase(
        conversationRepository = get(),
        resumeRepository = get()
    ) }

    factory { GetConversationsUseCase(
        conversationRepository = get()
    ) }

    factory { DeleteConversationUseCase(
        conversationRepository = get()
    ) }

    factory { AddMessageUseCase(
        conversationRepository = get(),
        resumeRepository = get()
    ) }

    factory { AnalyzeAndAddToConversationUseCase(
        analyzeResumeUseCase = get(),
        conversationRepository = get()
    ) }

    viewModel { ConversationListViewModel(
        getConversationsUseCase = get(),
        deleteConversationUseCase = get(),
        conversationRepository = get()
    ) }
}
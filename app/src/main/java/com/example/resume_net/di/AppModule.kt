package com.example.resume_net.di

import com.example.resume_net.presentation.chat.ChatViewModel
import com.example.resume_net.presentation.conversations.ConversationListViewModel
import com.example.resume_net.presentation.newanalysis.NewAnalysisViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    viewModel { ConversationListViewModel(
        getConversationsUseCase = get(),
        deleteConversationUseCase = get(),
        conversationRepository = get()
    ) }

    viewModel { NewAnalysisViewModel(
        createConversationUseCase = get()
    ) }

    viewModel { params ->
        ChatViewModel(
            savedStateHandle = params.get(),
            conversationRepository = get(),
            resumeRepository = get(),
            addMessageUseCase = get(),
            analyzeAndAddToConversationUseCase = get()
        )
    }
}
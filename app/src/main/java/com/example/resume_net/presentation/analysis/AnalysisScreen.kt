package com.example.resume_net.presentation.analysis

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.resume_net.presentation.components.IssueList
import com.example.resume_net.presentation.components.ScoreCard
import com.example.resume_net.presentation.components.WarningList
import org.koin.androidx.compose.koinViewModel

@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel = koinViewModel(),
    onNavigateToResult: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AnalysisEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is AnalysisEffect.NavigateToResult -> {
                    onNavigateToResult()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = state.resumeText,
                onValueChange = { viewModel.handleIntent(AnalysisIntent.UpdateText(it)) },
                label = { Text("Текст резюме") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                maxLines = 20,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.handleIntent(AnalysisIntent.Analyze) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && state.resumeText.isNotBlank()
            ) {
                Text("Анализировать")
            }

            if (state.isLoading) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator()
            }

            state.result?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                ScoreCard(score = result.score)

                if (result.issues.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    IssueList(issues = result.issues)
                }

                if (result.warnings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    WarningList(warnings = result.warnings)
                }
            }
        }
    }
}
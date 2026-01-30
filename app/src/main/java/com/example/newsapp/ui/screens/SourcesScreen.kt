package com.example.newsapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.newsapp.ui.components.SourceItem
import com.example.newsapp.viewmodel.SourcesUiState
import com.example.newsapp.viewmodel.SourcesViewModel

@Composable
fun SourcesScreen(viewModel: SourcesViewModel = hiltViewModel()) {
    val state by viewModel.sourcesState.collectAsState()
    val selectedIds by viewModel.selectedSourceIds.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val uiState = state) {
            is SourcesUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is SourcesUiState.Error -> {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error: ${uiState.message}", color = Color.Red)
                    Button(onClick = { viewModel.loadSources() }) { Text("Retry") }
                }
            }
            is SourcesUiState.Success -> {
                LazyColumn {
                    items(uiState.sources) { source ->
                        SourceItem(
                            source = source,
                            isSelected = selectedIds.contains(source.id),
                            onToggle = { viewModel.onSourceToggled(source.id) }
                        )
                    }
                }
            }
        }
    }
}
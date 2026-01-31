package com.example.newsapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.newsapp.ui.components.CommonSearchBar
import com.example.newsapp.ui.components.SourceItem
import com.example.newsapp.viewmodel.SourcesUiState
import com.example.newsapp.viewmodel.SourcesViewModel

@Composable
fun SourcesScreen(viewModel: SourcesViewModel = hiltViewModel()) {
    val state by viewModel.sourcesState.collectAsState()
    val selectedIds by viewModel.selectedSourceIds.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // 1. Observe the filtered sources from the ViewModel
    val filteredSources by viewModel.filteredSources.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        CommonSearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.onSearchQueryChange(it) },
            placeholder = "Search sources..."
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            when (val uiState = state) {
                is SourcesUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is SourcesUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error: ${uiState.message}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadSources() }) { Text("Retry") }
                    }
                }

                is SourcesUiState.Success -> {
                    // 2. Handle the "No results found" state for search
                    if (filteredSources.isEmpty() && searchQuery.isNotEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No sources match your search.")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            // 3. Use filteredSources here
                            items(filteredSources) { source ->
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
    }
}
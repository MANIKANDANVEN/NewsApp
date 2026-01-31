package com.example.newsapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.newsapp.data.remote.models.SourceDto
import com.example.newsapp.ui.components.CommonSearchBar
import com.example.newsapp.ui.components.SourceItem
import com.example.newsapp.ui.theme.NewsAppTheme
import com.example.newsapp.viewmodel.SourcesUiState
import com.example.newsapp.viewmodel.SourcesViewModel

@Composable
fun SourcesScreen(viewModel: SourcesViewModel = hiltViewModel()) {
    val state by viewModel.sourcesState.collectAsState()
    val selectedIds by viewModel.selectedSourceIds.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredSources by viewModel.filteredSources.collectAsState()

    SourcesContent(
        uiState = state,
        searchQuery = searchQuery,
        filteredSources = filteredSources,
        selectedIds = selectedIds,
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        onToggleSource = { viewModel.onSourceToggled(it) },
        onRetry = { viewModel.loadSources() }
    )
}

@Composable
fun SourcesContent(
    uiState: SourcesUiState,
    searchQuery: String,
    filteredSources: List<SourceDto>,
    selectedIds: Set<String>,
    onSearchQueryChange: (String) -> Unit,
    onToggleSource: (String) -> Unit,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        CommonSearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            placeholder = "Search sources..."
        )

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (uiState) {
                is SourcesUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is SourcesUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error: ${uiState.message}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = onRetry) { Text("Retry") }
                    }
                }

                is SourcesUiState.Success -> {
                    if (filteredSources.isEmpty() && searchQuery.isNotEmpty()) {
                        Text(
                            "No sources match your search.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredSources) { source ->
                                SourceItem(
                                    source = source,
                                    isSelected = selectedIds.contains(source.id),
                                    onToggle = { onToggleSource(source.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSourcesContentSuccess() {
    NewsAppTheme {
        SourcesContent(
            uiState = SourcesUiState.Success(emptyList()),
            searchQuery = "",
            filteredSources = listOf(/* Add mock SourceDto here */),
            selectedIds = setOf("bbc-news"),
            onSearchQueryChange = {},
            onToggleSource = {},
            onRetry = {}
        )
    }
}
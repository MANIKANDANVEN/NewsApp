package com.example.newsapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.newsapp.ui.components.CommonSearchBar
import com.example.newsapp.ui.components.ErrorContent
import com.example.newsapp.ui.components.NewsCard
import com.example.newsapp.viewmodel.NewsViewModel
import com.example.newsapp.viewmodel.state.NewsUiState
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HeadlinesScreen(
    navController: NavController,
    viewModel: NewsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val savedUrls by viewModel.savedArticleUrls.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredArticles by viewModel.filteredHeadlines.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        CommonSearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.onSearchQueryChange(it) },
            placeholder = "Search headlines..."
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (val state = uiState) {
                is NewsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is NewsUiState.Empty -> {
                    NoSourcesSelectedContent(Modifier.align(Alignment.Center))
                }

                is NewsUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        modifier = Modifier.align(Alignment.Center),
                        onRetry = { viewModel.refreshHeadlines() }
                    )
                }

                is NewsUiState.Success -> {
                    if (filteredArticles.isEmpty() && searchQuery.isNotEmpty()) {
                        Text(
                            text = "No matching articles found.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredArticles) { article ->
                                val isSaved = savedUrls.contains(article.url)
                                NewsCard(
                                    article = article,
                                    isSaved = isSaved,
                                    onToggleSave = { viewModel.toggleSave(article, isSaved) },
                                    onClick = {
                                        val encodedUrl = URLEncoder.encode(
                                            article.url,
                                            StandardCharsets.UTF_8.toString()
                                        )
                                        navController.navigate("detail/$encodedUrl")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoSourcesSelectedContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No Sources Selected", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Go to the Sources tab to pick your favorite news outlets.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
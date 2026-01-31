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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.newsapp.data.remote.models.ArticleDto
import com.example.newsapp.ui.components.CommonSearchBar
import com.example.newsapp.ui.components.ErrorContent
import com.example.newsapp.ui.components.NewsCard
import com.example.newsapp.ui.theme.NewsAppTheme
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

    HeadlinesContent(
        uiState = uiState,
        searchQuery = searchQuery,
        filteredArticles = filteredArticles,
        savedUrls = savedUrls,
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        onRefresh = { viewModel.refreshHeadlines() },
        onToggleSave = { article, isSaved -> viewModel.toggleSave(article, isSaved) },
        onArticleClick = { article ->
            val encodedUrl = URLEncoder.encode(article.url, StandardCharsets.UTF_8.toString())
            navController.navigate("detail/$encodedUrl")
        }
    )
}

@Composable
fun HeadlinesContent(
    uiState: NewsUiState,
    searchQuery: String,
    filteredArticles: List<ArticleDto>,
    savedUrls: Set<String>,
    onSearchQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onToggleSave: (ArticleDto, Boolean) -> Unit,
    onArticleClick: (ArticleDto) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        CommonSearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            placeholder = "Search headlines..."
        )

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (uiState) {
                is NewsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is NewsUiState.Empty -> {
                    NoSourcesSelectedContent(Modifier.align(Alignment.Center))
                }
                is NewsUiState.Error -> {
                    ErrorContent(
                        message = uiState.message,
                        modifier = Modifier.align(Alignment.Center),
                        onRetry = onRefresh
                    )
                }
                is NewsUiState.Success -> {
                    if (filteredArticles.isEmpty() && searchQuery.isNotEmpty()) {
                        Text("No matching articles found.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredArticles) { article ->
                                val isSaved = savedUrls.contains(article.url)
                                NewsCard(
                                    article = article,
                                    isSaved = isSaved,
                                    onToggleSave = { onToggleSave(article, isSaved) },
                                    onClick = { onArticleClick(article) }
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


@Preview(showBackground = true, name = "Success State")
@Composable
fun PreviewHeadlinesContent() {
    NewsAppTheme {
        HeadlinesContent(
            uiState = NewsUiState.Success(emptyList()), // Mocking success
            searchQuery = "",
            filteredArticles = listOf(
                // article 1, article 2...
            ),
            savedUrls = emptySet(),
            onSearchQueryChange = {},
            onRefresh = {},
            onToggleSave = { _, _ -> },
            onArticleClick = {}
        )
    }
}
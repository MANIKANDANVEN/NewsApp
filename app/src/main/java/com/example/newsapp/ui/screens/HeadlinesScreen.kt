package com.example.newsapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.newsapp.ui.components.NewsCard
import com.example.newsapp.viewmodel.NewsViewModel
import java.net.URLEncoder

@Composable
fun HeadlinesScreen(
    navController: NavController,
    viewModel: NewsViewModel = hiltViewModel()
) {
    val articles by viewModel.headlines.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val savedUrls by viewModel.savedArticleUrls.collectAsState() // Observe saved articles

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (articles.isEmpty()) {
            // This handles the "Unselected All" state
            Column(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "No Sources Selected",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "Go to the Sources tab to pick your favorite news outlets.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn {
                items(articles) { article ->
                    val isSaved = savedUrls.contains(article.url)
                    NewsCard(
                        article = article,
                        isSaved = isSaved,
                        onToggleSave = { viewModel.toggleSave(article, isSaved) },
                        onClick = { /* navigate to detail */ }
                    )
                }
            }
        }
    }
}
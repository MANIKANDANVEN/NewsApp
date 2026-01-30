package com.example.newsapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    if (articles.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Select sources in the 'Sources' tab to see news.")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(articles) { article ->
                NewsCard(
                    article = article,
                    onSave = { viewModel.saveArticle(article) },
                    onClick = {
                        val encodedUrl = URLEncoder.encode(article.url, "UTF-8")
                        navController.navigate("detail/$encodedUrl")
                    }
                )
            }
        }
    }
}
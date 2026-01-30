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
import com.example.newsapp.ui.components.SavedArticleRow
import com.example.newsapp.viewmodel.SavedViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun SavedScreen(
    navController: NavController,
    viewModel: SavedViewModel = hiltViewModel()
) {
    val articles by viewModel.savedArticles.collectAsState()

    if (articles.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No saved articles yet.")
        }
    } else {
        LazyColumn {
            items(articles) { article ->
                // We reuse a simplified version of the NewsCard or a custom SavedRow
                SavedArticleRow(
                    article = article,
                    onDelete = { viewModel.deleteArticle(article) },
                    onClick = {
                        val encodedUrl = URLEncoder.encode(article.url, StandardCharsets.UTF_8.toString())
                        navController.navigate("detail/$encodedUrl")
                    }
                )
            }
        }
    }
}
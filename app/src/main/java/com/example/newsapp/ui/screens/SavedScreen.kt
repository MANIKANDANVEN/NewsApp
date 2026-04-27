package com.example.newsapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.newsapp.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.newsapp.data.local.ArticleEntity
import com.example.newsapp.ui.components.CommonSearchBar
import com.example.newsapp.ui.components.SavedArticleRow
import com.example.newsapp.ui.theme.NewsAppTheme
import com.example.newsapp.viewmodel.SavedViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun SavedScreen(
    navController: NavController,
    viewModel: SavedViewModel = hiltViewModel()
) {
    val articles by viewModel.filteredSavedArticles.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    SavedContent(
        articles = articles,
        searchQuery = searchQuery,
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        onDeleteArticle = { viewModel.deleteArticle(it) },
        onArticleClick = { url ->
            val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
            navController.navigate("detail/$encodedUrl")
        }
    )
}

@Composable
fun SavedContent(
    articles: List<ArticleEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDeleteArticle: (ArticleEntity) -> Unit,
    onArticleClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        CommonSearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            placeholder = stringResource(R.string.SaveScreen_searchPlaceholder)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (articles.isEmpty()) {
                val emptyMessage = if (searchQuery.isEmpty()) {
                    "No saved articles yet."
                } else {
                    "No results match your search."
                }

                Text(
                    text = emptyMessage,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(articles) { article ->
                        SavedArticleRow(
                            article = article,
                            onDelete = { onDeleteArticle(article) },
                            onClick = { onArticleClick(article.url) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Empty State")
@Composable
fun PreviewSavedContentEmpty() {
    NewsAppTheme {
        SavedContent(
            articles = emptyList(),
            searchQuery = "",
            onSearchQueryChange = {},
            onDeleteArticle = {},
            onArticleClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Search Result State")
@Composable
fun PreviewSavedContentSearch() {
    NewsAppTheme {
        SavedContent(
            articles = emptyList(),
            searchQuery = "Unicorns",
            onSearchQueryChange = {},
            onDeleteArticle = {},
            onArticleClick = {}
        )
    }
}
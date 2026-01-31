package com.example.newsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.data.local.ArticleEntity
import com.example.newsapp.data.repository.NewsRepository
import com.example.newsapp.utils.SearchDelegate
import com.example.newsapp.utils.SearchDelegateImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class SavedViewModel @Inject constructor(private val repo: NewsRepository) : ViewModel(), SearchDelegate by SearchDelegateImpl() {

    // Combine the Room flow with the SearchQuery flow
    val filteredSavedArticles: StateFlow<List<ArticleEntity>> = repo.getSavedArticles()
        .combine(searchQuery) { articles, query ->
            if (query.isBlank()) {
                articles
            } else {
                articles.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.description?.contains(query, ignoreCase = true) == true
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteArticle(article: ArticleEntity) {
        viewModelScope.launch {
            repo.deleteArticle(article)
        }
    }
}
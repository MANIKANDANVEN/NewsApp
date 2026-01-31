package com.example.newsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.data.local.ArticleEntity
import com.example.newsapp.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class SavedViewModel @Inject constructor(private val repo: NewsRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Combine the Room flow with the SearchQuery flow
    val filteredSavedArticles: StateFlow<List<ArticleEntity>> = repo.getSavedArticles()
        .combine(_searchQuery) { articles, query ->
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

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun deleteArticle(article: ArticleEntity) {
        viewModelScope.launch {
            repo.deleteArticle(article)
        }
    }
}
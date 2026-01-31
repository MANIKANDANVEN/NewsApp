package com.example.newsapp.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.newsapp.data.local.ArticleEntity
import com.example.newsapp.data.repository.NewsRepository
import com.example.newsapp.viewmodel.base.BaseViewModel
import com.example.newsapp.delegate.SearchDelegate
import com.example.newsapp.delegate.SearchDelegateImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.*

@HiltViewModel
class SavedViewModel @Inject constructor(private val repo: NewsRepository) : BaseViewModel(), SearchDelegate by SearchDelegateImpl() {

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
        safeLaunch {
            repo.deleteArticle(article)
        }
    }
}
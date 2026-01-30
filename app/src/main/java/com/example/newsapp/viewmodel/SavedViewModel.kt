package com.example.newsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.data.local.ArticleEntity
import com.example.newsapp.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SavedViewModel @Inject constructor(private val repo: NewsRepository) : ViewModel() {

    // Automatically updates the UI whenever the Room database changes
    val savedArticles: StateFlow<List<ArticleEntity>> = repo.getSavedArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteArticle(article: ArticleEntity) {
        viewModelScope.launch {
            repo.deleteArticle(article)
        }
    }
}
package com.example.newsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.data.local.ArticleEntity
import com.example.newsapp.data.remote.models.ArticleDto
import com.example.newsapp.data.repository.NewsRepository
import com.example.newsapp.viewmodel.state.NewsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repo: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    // Keep track of IDs for the refresh action
    private var currentSourceIds: String? = null

    init {
        observeSourceChanges()
    }

    private fun observeSourceChanges() {
        viewModelScope.launch {
            repo.getSelectedSourceIds().collect { ids ->
                if (ids.isEmpty()) {
                    currentSourceIds = null
                    _uiState.value = NewsUiState.Empty
                } else {
                    currentSourceIds = ids.joinToString(",")
                    refreshHeadlines() // Call our new public refresh function
                }
            }
        }
    }

    //function for Pull-to-Refresh and Retry Button
    fun refreshHeadlines() {
        val ids = currentSourceIds ?: return
        viewModelScope.launch {
            _uiState.value = NewsUiState.Loading
            try {
                // Using your repo's logic
                val response = repo.getTopHeadlines(ids)
                if (response.articles.isEmpty()) {
                    _uiState.value = NewsUiState.Empty
                } else {
                    _uiState.value = NewsUiState.Success(response.articles)
                }
            } catch (e: Exception) {
                _uiState.value = NewsUiState.Error("Failed to load news. Check your connection.")
            }
        }
    }

    fun saveArticle(dto: ArticleDto) {
        viewModelScope.launch {
            repo.saveArticle(
                ArticleEntity(
                    url = dto.url,
                    title = dto.title,
                    description = dto.description,
                    author = dto.author,
                    urlToImage = dto.urlToImage,
                    publishedAt = dto.publishedAt
                )
            )
        }
    }

    // Add this inside NewsViewModel
    val savedArticleUrls: StateFlow<Set<String>> = repo.getSavedArticles()
        .map { articles -> articles.map { it.url }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun toggleSave(dto: ArticleDto, isSaved: Boolean) {
        viewModelScope.launch {
            if (isSaved) {
                // If already saved, delete it (Pass a dummy entity with the same URL)
                repo.deleteArticle(
                    ArticleEntity(
                        url = dto.url,
                        title = dto.title,
                        description = null,
                        author = null,
                        urlToImage = null,
                        publishedAt = ""
                    )
                )
            } else {
                // If not saved, add it
                repo.saveArticle(
                    ArticleEntity(
                        dto.url,
                        dto.title,
                        dto.description,
                        dto.author,
                        dto.urlToImage,
                        dto.publishedAt
                    )
                )
            }
        }
    }
}
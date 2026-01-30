package com.example.newsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.data.local.ArticleEntity
import com.example.newsapp.data.remote.models.ArticleDto
import com.example.newsapp.data.repository.NewsRepository
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

    private val _headlines = MutableStateFlow<List<ArticleDto>>(emptyList())
    val headlines = _headlines.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        observeSourceChanges()
    }

    private fun observeSourceChanges() {
        viewModelScope.launch {
            repo.getSelectedSourceIds().collect { ids ->
                if (ids.isEmpty()) {
                    // 1. If no sources are selected, clear the list immediately
                    _headlines.value = emptyList()
                } else {
                    // 2. If sources exist, fetch the news
                    fetchHeadlines(ids.joinToString(","))
                }
            }
        }
    }

    private suspend fun fetchHeadlines(sourceIds: String) {
        _isLoading.value = true
        try {
            val response = repo.getTopHeadlines(sourceIds)
            _headlines.value = response.articles
        } catch (e: Exception) {
            // Log error or show snackbar
            _headlines.value = emptyList()
        } finally {
            _isLoading.value = false
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
                repo.deleteArticle(ArticleEntity(url = dto.url, title = dto.title, description = null, author = null, urlToImage = null, publishedAt = ""))
            } else {
                // If not saved, add it
                repo.saveArticle(ArticleEntity(dto.url, dto.title, dto.description, dto.author, dto.urlToImage, dto.publishedAt))
            }
        }
    }
}
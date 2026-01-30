package com.example.newsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.data.local.ArticleEntity
import com.example.newsapp.data.remote.models.ArticleDto
import com.example.newsapp.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class NewsViewModel @Inject constructor(private val repo: NewsRepository) : ViewModel() {
    private val _headlines = MutableStateFlow<List<ArticleDto>>(emptyList())
    val headlines = _headlines.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getSelectedSourceIds().collect { ids ->
                if (ids.isNotEmpty()) {
                    val response = repo.getTopHeadlines(ids.joinToString(","))
                    _headlines.value = response.articles
                }
            }
        }
    }

    fun saveArticle(dto: ArticleDto) {
        viewModelScope.launch {
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
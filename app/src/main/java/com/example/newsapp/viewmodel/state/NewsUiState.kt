package com.example.newsapp.viewmodel.state

import com.example.newsapp.data.remote.models.ArticleDto

sealed class NewsUiState {
    object Loading : NewsUiState()
    data class Success(val articles: List<ArticleDto>) : NewsUiState()
    data class Error(val message: String) : NewsUiState()
    object Empty : NewsUiState() // Handles the "No Sources Selected" state
}
package com.example.newsapp.data.repository

import com.example.newsapp.data.local.ArticleDao
import com.example.newsapp.data.local.ArticleEntity
import com.example.newsapp.data.local.SourcePreferences
import com.example.newsapp.data.remote.models.SourceResponse
import com.example.newsapp.services.NewsApiService
import jakarta.inject.Inject

class NewsRepository @Inject constructor(
    private val api: NewsApiService,
    private val dao: ArticleDao,
    private val prefs: SourcePreferences
) {
    fun getSavedArticles() = dao.getAllSavedArticles()
    suspend fun saveArticle(article: ArticleEntity) = dao.saveArticle(article)
    suspend fun deleteArticle(article: ArticleEntity) = dao.deleteArticle(article)
    suspend fun getTopHeadlines(sourceIds: String) =
        api.getHeadlines(sourceIds, "ca734ffd56ec4612b32def016d40ff67")

    suspend fun getSources(): SourceResponse {
        return api.getSources(language = "en", apiKey = "ca734ffd56ec4612b32def016d40ff67")
    }

    fun getSelectedSourceIds() = prefs.selectedSources
    suspend fun saveSelectedSources(ids: Set<String>) = prefs.saveSources(ids)
}
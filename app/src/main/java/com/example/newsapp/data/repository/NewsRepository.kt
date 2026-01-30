package com.example.newsapp.data.repository

import com.example.newsapp.data.local.ArticleDao
import com.example.newsapp.data.local.ArticleEntity
import com.example.newsapp.data.local.SourcePreferences
import com.example.newsapp.data.remote.models.SourceDto
import com.example.newsapp.data.remote.services.NewsApiService
import jakarta.inject.Inject

class NewsRepository @Inject constructor(
    private val api: NewsApiService,
    private val dao: ArticleDao,
    private val prefs: SourcePreferences
) {
    fun getSavedArticles() = dao.getAllSavedArticles()

    suspend fun saveArticle(article: ArticleEntity) = dao.saveArticle(article)

    suspend fun deleteArticle(article: ArticleEntity) = dao.deleteArticle(article)

    suspend fun getTopHeadlines(sourceIds: String) = api.getHeadlines(sourceIds, "ca734ffd56ec4612b32def016d40ff67")

    suspend fun getSources() = api.getSources(apiKey = "ca734ffd56ec4612b32def016d40ff67")

    fun getSelectedSourceIds() = prefs.selectedSources

    suspend fun saveSelectedSources(ids: Set<String>) = prefs.saveSources(ids)

    // Fetch available English sources from API
    suspend fun fetchAvailableSources(): Result<List<SourceDto>> {
        return try {
            val response = api.getSources(language = "en", apiKey = "YOUR_API_KEY")
            Result.success(response.sources)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Persist the user's selection to DataStore
    suspend fun toggleSourceSelection(sourceId: String, currentSelection: Set<String>) {
        val newSelection = currentSelection.toMutableSet()
        if (newSelection.contains(sourceId)) {
            newSelection.remove(sourceId)
        } else {
            newSelection.add(sourceId)
        }
        prefs.saveSources(newSelection)
    }
}
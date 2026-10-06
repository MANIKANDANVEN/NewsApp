package com.example.newsapp.data.repository

import com.example.newsapp.BuildConfig
import com.example.newsapp.data.local.ArticleDao
import com.example.newsapp.data.local.ArticleEntity
import com.example.newsapp.data.local.CachedHeadlineEntity
import com.example.newsapp.data.local.HeadlineDao
import com.example.newsapp.data.local.SourcePreferences
import com.example.newsapp.data.remote.models.ArticleDto
import com.example.newsapp.data.remote.models.SourceResponse
import com.example.newsapp.services.NewsApiService
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NewsRepository @Inject constructor(
    private val api: NewsApiService,
    private val savedArticleDao: ArticleDao,
    private val headlineDao: HeadlineDao,
    private val prefs: SourcePreferences
) {
    fun getSavedArticles() = savedArticleDao.getAllSavedArticles()
    suspend fun saveArticle(article: ArticleEntity) = savedArticleDao.saveArticle(article)
    suspend fun deleteArticle(article: ArticleEntity) = savedArticleDao.deleteArticle(article)

    // Whatever headlines we last fetched for this source selection, read straight from
    // Room. The ViewModel renders this immediately so the UI never has to sit on a
    // blank loading screen while waiting on the network.
    fun observeCachedHeadlines(sourceIds: String): Flow<List<ArticleDto>> =
        headlineDao.observeHeadlines(sourceIds).map { cached -> cached.map { it.toDto() } }

    // Hits the network and replaces the cached page for these sourceIds. Callers observe
    // the result through observeCachedHeadlines rather than this return value.
    suspend fun refreshHeadlines(sourceIds: String): List<ArticleDto> {
        val response = api.getHeadlines(sourceIds, BuildConfig.NEWS_API_KEY)
        headlineDao.replaceHeadlines(sourceIds, response.articles.map { it.toCacheEntity(sourceIds) })
        return response.articles
    }

    suspend fun getSources(): SourceResponse =
        api.getSources(language = "en", apiKey = BuildConfig.NEWS_API_KEY)

    fun getSelectedSourceIds() = prefs.selectedSources
    suspend fun saveSelectedSources(ids: Set<String>) = prefs.saveSources(ids)
}

private fun ArticleDto.toCacheEntity(sourceIds: String) = CachedHeadlineEntity(
    url = url,
    sourceIds = sourceIds,
    title = title,
    description = description,
    author = author,
    urlToImage = urlToImage,
    publishedAt = publishedAt
)

private fun CachedHeadlineEntity.toDto() = ArticleDto(
    author = author,
    title = title,
    description = description,
    url = url,
    urlToImage = urlToImage,
    publishedAt = publishedAt
)

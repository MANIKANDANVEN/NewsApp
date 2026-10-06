package com.example.newsapp.data.repository

import com.example.newsapp.data.local.ArticleDao
import com.example.newsapp.data.local.CachedHeadlineEntity
import com.example.newsapp.data.local.HeadlineDao
import com.example.newsapp.data.local.SourcePreferences
import com.example.newsapp.data.remote.models.ArticleDto
import com.example.newsapp.data.remote.models.NewsResponse
import com.example.newsapp.data.remote.models.SourceDto
import com.example.newsapp.data.remote.models.SourceResponse
import com.example.newsapp.services.NewsApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NewsRepositoryTest {
    private val api: NewsApiService = mockk()
    private val savedArticleDao: ArticleDao = mockk()
    private val headlineDao: HeadlineDao = mockk()
    private val prefs: SourcePreferences = mockk()
    private lateinit var repository: NewsRepository

    @Before
    fun setup() {
        repository = NewsRepository(api, savedArticleDao, headlineDao, prefs)
    }

    @Test
    fun `fetchAvailableSources returns success when API call is successful`() = runTest {
        // Arrange
        val mockResponse =
            SourceResponse("ok", listOf(SourceDto("id", "name", "desc", "url", "cat", "en", "us")))
        coEvery { api.getSources(any(), any()) } returns mockResponse

        // Act
        val result = repository.getSources()

        // Assert
        assertEquals("ok", result.status)
        assertEquals(1, result.sources.size)
        coVerify { api.getSources(language = "en", apiKey = any()) }
    }

    @Test
    fun `observeCachedHeadlines maps cached entities to ArticleDto`() = runTest {
        val cached = listOf(
            CachedHeadlineEntity(
                url = "https://example.com",
                sourceIds = "bbc-news",
                title = "Title",
                description = "Desc",
                author = "Author",
                urlToImage = null,
                publishedAt = "2026-01-01"
            )
        )
        every { headlineDao.observeHeadlines("bbc-news") } returns flowOf(cached)

        val result = repository.observeCachedHeadlines("bbc-news")

        assertEquals(
            listOf(
                ArticleDto(
                    author = "Author",
                    title = "Title",
                    description = "Desc",
                    url = "https://example.com",
                    urlToImage = null,
                    publishedAt = "2026-01-01"
                )
            ),
            result.first()
        )
    }

    @Test
    fun `refreshHeadlines fetches from API and replaces the cache`() = runTest {
        val article = ArticleDto(
            author = "Author",
            title = "Title",
            description = "Desc",
            url = "https://example.com",
            urlToImage = null,
            publishedAt = "2026-01-01"
        )
        coEvery { api.getHeadlines("bbc-news", any()) } returns NewsResponse(listOf(article))
        coEvery { headlineDao.replaceHeadlines(any(), any()) } returns Unit

        val result = repository.refreshHeadlines("bbc-news")

        assertEquals(listOf(article), result)
        coVerify {
            headlineDao.replaceHeadlines(
                "bbc-news",
                match { it.size == 1 && it[0].url == article.url }
            )
        }
    }
}
package com.example.newsapp.data.repository

import com.example.newsapp.data.local.ArticleDao
import com.example.newsapp.data.local.SourcePreferences
import com.example.newsapp.data.remote.models.SourceDto
import com.example.newsapp.data.remote.models.SourceResponse
import com.example.newsapp.services.NewsApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NewsRepositoryTest {
    private val api: NewsApiService = mockk()
    private val dao: ArticleDao = mockk()
    private val prefs: SourcePreferences = mockk()
    private lateinit var repository: NewsRepository

    @Before
    fun setup() {
        repository = NewsRepository(api, dao, prefs)
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
}
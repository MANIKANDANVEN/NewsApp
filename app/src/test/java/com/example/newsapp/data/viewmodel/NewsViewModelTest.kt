package com.example.newsapp.data.viewmodel

import app.cash.turbine.test
import com.example.newsapp.data.remote.models.ArticleDto
import com.example.newsapp.data.remote.models.NewsResponse
import com.example.newsapp.data.repository.NewsRepository
import com.example.newsapp.viewmodel.NewsViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModelTest {

    private val repository: NewsRepository = mockk()
    private lateinit var viewModel: NewsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Default mock: no sources selected
        every { repository.getSelectedSourceIds() } returns flowOf(emptySet())
        every { repository.getSavedArticles() } returns flowOf(emptyList())

        viewModel = NewsViewModel(repository)
    }

    @Test
    fun `when no sources are selected, headlines should be empty`() = runTest {
        viewModel.headlines.test {
            assertEquals(emptyList<ArticleDto>(), awaitItem())
        }
    }

    @Test
    fun `when source is selected, fetchHeadlines is called and updates state`() = runTest {
        // Arrange
        val mockArticles = listOf(ArticleDto("Author", "Title", "Desc", "url", null, "date"))
        every { repository.getSelectedSourceIds() } returns flowOf(setOf("bbc-news"))
        coEvery { repository.getTopHeadlines("bbc-news") } returns NewsResponse(mockArticles)

        // Act - Re-init to trigger the flow collection logic
        viewModel = NewsViewModel(repository)

        // Assert
        viewModel.headlines.test {
            assertEquals(mockArticles, awaitItem())
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
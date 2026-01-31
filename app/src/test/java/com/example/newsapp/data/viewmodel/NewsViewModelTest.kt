package com.example.newsapp.data.viewmodel

import app.cash.turbine.test
import com.example.newsapp.data.remote.models.ArticleDto
import com.example.newsapp.data.remote.models.NewsResponse
import com.example.newsapp.data.repository.NewsRepository
import com.example.newsapp.viewmodel.NewsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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

    @Test
    fun `when API returns error, headlines are cleared and loading is false`() = runTest {
        // Arrange: Mock an Exception
        every { repository.getSelectedSourceIds() } returns flowOf(setOf("bbc-news"))
        coEvery { repository.getTopHeadlines(any()) } throws Exception("Network Timeout")

        // Act
        viewModel = NewsViewModel(repository)
        advanceUntilIdle()

        // Assert
        assertEquals(emptyList<ArticleDto>(), viewModel.headlines.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `when fetch fails headlines are cleared and loading becomes false`() = runTest {
        // Arrange
        every { repository.getSelectedSourceIds() } returns flowOf(setOf("bbc-news"))
        coEvery { repository.getTopHeadlines(any()) } throws Exception("API Error")

        // Act
        viewModel = NewsViewModel(repository)
        advanceUntilIdle()

        // Assert
        assertEquals(emptyList<ArticleDto>(), viewModel.headlines.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `verify loading state transitions correctly`() = runTest {
        // 1. Arrange: Use a SharedFlow to control emission timing
        val sourceIdsFlow = MutableSharedFlow<Set<String>>()
        val mockArticles = listOf(ArticleDto("Title", "Author", "URL", "Image", "Desc", "Date"))

        every { repository.getSelectedSourceIds() } returns sourceIdsFlow
        every { repository.getSavedArticles() } returns flowOf(emptyList())

        coEvery { repository.getTopHeadlines(any()) } coAnswers {
            delay(100) // Delay to ensure Turbine captures the 'true' state
            NewsResponse(mockArticles)
        }

        // 2. Act: Create ViewModel (it's now collecting from our sourceIdsFlow)
        val viewModel = NewsViewModel(repository)

        // 3. Assert
        viewModel.isLoading.test {
            // Initial state is false
            assertEquals(false, awaitItem())

            // Trigger the loading state by emitting IDs NOW
            sourceIdsFlow.emit(setOf("bbc-news"))

            // Now Turbine will catch the transitions because it's already listening
            assertEquals(true, awaitItem())
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun `when source ids are empty headlines are cleared`() = runTest {
        val sourceIdsFlow = MutableSharedFlow<Set<String>>()
        every { repository.getSelectedSourceIds() } returns sourceIdsFlow

        val viewModel = NewsViewModel(repository)

        viewModel.headlines.test {
            assertEquals(emptyList<ArticleDto>(), awaitItem()) // Initial
            sourceIdsFlow.emit(emptySet())
            // If it was already empty, StateFlow might not emit again.
            // To be sure, you can check the value directly:
            assertEquals(0, viewModel.headlines.value.size)
        }
    }

    @Test
    fun `successful fetch updates headlines`() = runTest {
        val sourceIdsFlow = MutableSharedFlow<Set<String>>()
        val mockArticles = listOf(ArticleDto("Title", "Author", "URL", "Image", "Desc", "Date"))
        every { repository.getSelectedSourceIds() } returns sourceIdsFlow
        coEvery { repository.getTopHeadlines(any()) } returns NewsResponse(mockArticles)

        val viewModel = NewsViewModel(repository)
        sourceIdsFlow.emit(setOf("tech-crunch"))

        advanceUntilIdle()
        assertEquals(mockArticles, viewModel.headlines.value)
    }

    @Test
    fun `saveArticle calls repository with correct entity`() = runTest {
        // Arrange
        val dto = ArticleDto("Author", "Title", "Desc", "url.com", "image.jpg", "2024-01-01")
        coEvery { repository.saveArticle(any()) } returns Unit

        // Act
        viewModel.saveArticle(dto)
        advanceUntilIdle() // Ensure the launch { } block finishes

        // Assert
        coVerify {
            repository.saveArticle(match { entity ->
                entity.url == dto.url && entity.title == dto.title
            })
        }
    }

    @Test
    fun `toggleSave calls delete when isSaved is true`() = runTest {
        val dto = ArticleDto("Author", "Title", "Desc", "url.com", "image.jpg", "2024-01-01")
        coEvery { repository.deleteArticle(any()) } returns Unit

        // Act: Pass isSaved = true
        viewModel.toggleSave(dto, isSaved = true)
        advanceUntilIdle()

        // Assert: Verify delete was called
        coVerify { repository.deleteArticle(match { it.url == dto.url }) }
        coVerify(exactly = 0) { repository.saveArticle(any()) } // Ensure save wasn't called
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
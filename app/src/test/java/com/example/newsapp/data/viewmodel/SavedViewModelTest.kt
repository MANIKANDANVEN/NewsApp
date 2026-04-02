package com.example.newsapp.data.viewmodel

import app.cash.turbine.test
import com.example.newsapp.data.local.ArticleEntity
import com.example.newsapp.data.repository.NewsRepository
import com.example.newsapp.viewmodel.SavedViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SavedViewModelTest {

    private val repository: NewsRepository = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    // Backing flow we push updates into — simulates Room emitting new snapshots
    private val savedArticlesFlow = MutableStateFlow<List<ArticleEntity>>(emptyList())

    // Helpers — ArticleEntity constructor: url, title, description, author, urlToImage, publishedAt
    private val articleKotlin = ArticleEntity(
        url = "https://example.com/kotlin",
        title = "Kotlin Coroutines Guide",
        description = "Deep dive into coroutines",
        author = "Alice",
        urlToImage = "https://img.example.com/1",
        publishedAt = "2024-01-01"
    )
    private val articleAndroid = ArticleEntity(
        url = "https://example.com/android",
        title = "Android Jetpack Compose",
        description = "Build UIs with Compose",
        author = "Bob",
        urlToImage = "https://img.example.com/2",
        publishedAt = "2024-02-01"
    )
    private val articleNoDesc = ArticleEntity(
        url = "https://example.com/nodesc",
        title = "No Description Article",
        description = null,
        author = "Carol",
        urlToImage = null,
        publishedAt = "2024-03-01"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getSavedArticles() } returns savedArticlesFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // -------------------------------------------------------------------------
    // Initial state
    // -------------------------------------------------------------------------

    @Test
    fun `filteredSavedArticles initial value is empty list`() = runTest {
        val viewModel = SavedViewModel(repository)

        assertEquals(emptyList<ArticleEntity>(), viewModel.filteredSavedArticles.value)
    }

    // -------------------------------------------------------------------------
    // Repository updates propagate through
    // -------------------------------------------------------------------------

    @Test
    fun `filteredSavedArticles reflects repository emissions when query is blank`() = runTest {
        val viewModel = SavedViewModel(repository)

        viewModel.filteredSavedArticles.test {
            assertEquals(emptyList<ArticleEntity>(), awaitItem()) // stateIn seed

            savedArticlesFlow.value = listOf(articleKotlin)
            assertEquals(listOf(articleKotlin), awaitItem())

            savedArticlesFlow.value = listOf(articleKotlin, articleAndroid)
            assertEquals(listOf(articleKotlin, articleAndroid), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filteredSavedArticles reflects removal from repository`() = runTest {
        savedArticlesFlow.value = listOf(articleKotlin, articleAndroid)
        val viewModel = SavedViewModel(repository)

        viewModel.filteredSavedArticles.test {
            assertEquals(listOf(articleKotlin, articleAndroid), awaitItem())

            savedArticlesFlow.value = listOf(articleAndroid)
            assertEquals(listOf(articleAndroid), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    // -------------------------------------------------------------------------
    // Search — title matching
    // -------------------------------------------------------------------------

    @Test
    fun `filteredSavedArticles filters by title case-insensitively`() = runTest {
        savedArticlesFlow.value = listOf(articleKotlin, articleAndroid)
        val viewModel = SavedViewModel(repository)

        viewModel.onSearchQueryChange("kotlin")

        viewModel.filteredSavedArticles.test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(articleKotlin, result.first())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filteredSavedArticles title match is case-insensitive for uppercase query`() = runTest {
        savedArticlesFlow.value = listOf(articleKotlin, articleAndroid)
        val viewModel = SavedViewModel(repository)

        viewModel.onSearchQueryChange("ANDROID")

        viewModel.filteredSavedArticles.test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(articleAndroid, result.first())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // -------------------------------------------------------------------------
    // Search — description matching
    // -------------------------------------------------------------------------

    @Test
    fun `filteredSavedArticles filters by description when title does not match`() = runTest {
        savedArticlesFlow.value = listOf(articleKotlin, articleAndroid)
        val viewModel = SavedViewModel(repository)

        // "coroutines" only appears in articleKotlin's description
        viewModel.onSearchQueryChange("coroutines")

        viewModel.filteredSavedArticles.test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(articleKotlin, result.first())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filteredSavedArticles description match is case-insensitive`() = runTest {
        savedArticlesFlow.value = listOf(articleKotlin, articleAndroid)
        val viewModel = SavedViewModel(repository)

        viewModel.onSearchQueryChange("COMPOSE")

        viewModel.filteredSavedArticles.test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(articleAndroid, result.first())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filteredSavedArticles does not crash when description is null`() = runTest {
        savedArticlesFlow.value = listOf(articleNoDesc, articleKotlin)
        val viewModel = SavedViewModel(repository)

        // Query matches articleKotlin title but articleNoDesc has null description — no crash
        viewModel.onSearchQueryChange("kotlin")

        viewModel.filteredSavedArticles.test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(articleKotlin, result.first())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `article with null description is still returned when title matches`() = runTest {
        savedArticlesFlow.value = listOf(articleNoDesc)
        val viewModel = SavedViewModel(repository)

        viewModel.onSearchQueryChange("No Description")

        viewModel.filteredSavedArticles.test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(articleNoDesc, result.first())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filteredSavedArticles returns empty list when query matches nothing`() = runTest {
        savedArticlesFlow.value = listOf(articleKotlin, articleAndroid)
        val viewModel = SavedViewModel(repository)

        viewModel.onSearchQueryChange("swift")

        viewModel.filteredSavedArticles.test {
            assertEquals(emptyList<ArticleEntity>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // -------------------------------------------------------------------------
    // Search — clearing query
    // -------------------------------------------------------------------------

    @Test
    fun `clearing search query restores full article list`() = runTest {
        savedArticlesFlow.value = listOf(articleKotlin, articleAndroid)
        val viewModel = SavedViewModel(repository)

        viewModel.onSearchQueryChange("kotlin")
        viewModel.onSearchQueryChange("") // clear

        viewModel.filteredSavedArticles.test {
            assertEquals(listOf(articleKotlin, articleAndroid), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // -------------------------------------------------------------------------
    // deleteArticle
    // -------------------------------------------------------------------------

    @Test
    fun `deleteArticle calls repository deleteArticle with the correct entity`() = runTest {
        coEvery { repository.deleteArticle(any()) } returns Unit
        val viewModel = SavedViewModel(repository)

        viewModel.deleteArticle(articleKotlin)
        advanceUntilIdle() // wait for safeLaunch coroutine to complete

        coVerify(exactly = 1) { repository.deleteArticle(articleKotlin) }
    }

    @Test
    fun `deleteArticle does not call saveArticle`() = runTest {
        coEvery { repository.deleteArticle(any()) } returns Unit
        val viewModel = SavedViewModel(repository)

        viewModel.deleteArticle(articleKotlin)
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.saveArticle(any()) }
    }

    @Test
    fun `deleteArticle can be called multiple times for different articles`() = runTest {
        coEvery { repository.deleteArticle(any()) } returns Unit
        val viewModel = SavedViewModel(repository)

        viewModel.deleteArticle(articleKotlin)
        viewModel.deleteArticle(articleAndroid)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.deleteArticle(articleKotlin) }
        coVerify(exactly = 1) { repository.deleteArticle(articleAndroid) }
    }

    @Test
    fun `UI reflects deletion after repository emits updated list`() = runTest {
        savedArticlesFlow.value = listOf(articleKotlin, articleAndroid)
        coEvery { repository.deleteArticle(any()) } answers {
            // Simulate Room removing the article and re-emitting
            savedArticlesFlow.value = listOf(articleAndroid)
        }

        val viewModel = SavedViewModel(repository)

        viewModel.filteredSavedArticles.test {
            assertEquals(listOf(articleKotlin, articleAndroid), awaitItem())

            viewModel.deleteArticle(articleKotlin)
            advanceUntilIdle()

            assertEquals(listOf(articleAndroid), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
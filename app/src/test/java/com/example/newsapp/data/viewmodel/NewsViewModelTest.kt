package com.example.newsapp.data.viewmodel

import android.content.Context
import app.cash.turbine.test
import com.example.newsapp.connectivity.ConnectivityObserver
import com.example.newsapp.data.local.ArticleEntity
import com.example.newsapp.data.remote.models.ArticleDto
import com.example.newsapp.data.repository.NewsRepository
import com.example.newsapp.viewmodel.NewsViewModel
import com.example.newsapp.viewmodel.state.NewsUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModelTest {

    private val repository: NewsRepository = mockk()
    private val connectivityObserver: ConnectivityObserver = mockk()
    private val context: Context = mockk(relaxed = true)
    private lateinit var viewModel: NewsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    // Canonical ArticleDto arg order: author, title, description, url, urlToImage, publishedAt
    private val mockArticle = ArticleDto(
        author = "Author",
        title = "Title",
        description = "Desc",
        url = "https://example.com",
        urlToImage = "https://img.example.com",
        publishedAt = "2024-01-01"
    )

    private fun newViewModel() = NewsViewModel(repository, connectivityObserver, context)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { connectivityObserver.isConnected } returns flowOf(true)
        every { repository.getSelectedSourceIds() } returns flowOf(emptySet())
        every { repository.getSavedArticles() } returns flowOf(emptyList())
        every { repository.observeCachedHeadlines(any()) } returns flowOf(emptyList())
        viewModel = newViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // -------------------------------------------------------------------------
    // Initial / Empty state
    // -------------------------------------------------------------------------

    @Test
    fun `initial uiState is Loading then transitions to Empty when no sources selected`() =
        runTest {
            // UnconfinedTestDispatcher resolves the init block synchronously,
            // so by the time we assert the state has already settled to Empty.
            assertEquals(NewsUiState.Empty, viewModel.uiState.value)
        }

    @Test
    fun `filteredHeadlines emits empty list when uiState is Empty`() = runTest {
        viewModel.filteredHeadlines.test {
            assertEquals(emptyList<ArticleDto>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // -------------------------------------------------------------------------
    // Success state (cache + refresh)
    // -------------------------------------------------------------------------

    @Test
    fun `when source is selected uiState becomes Success once the cache is populated`() =
        runTest {
            val cacheFlow = MutableStateFlow<List<ArticleDto>>(emptyList())
            every { repository.getSelectedSourceIds() } returns flowOf(setOf("bbc-news"))
            every { repository.observeCachedHeadlines("bbc-news") } returns cacheFlow
            coEvery { repository.refreshHeadlines("bbc-news") } coAnswers {
                cacheFlow.value = listOf(mockArticle)
                listOf(mockArticle)
            }

            viewModel = newViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is NewsUiState.Success)
            assertEquals(listOf(mockArticle), (state as NewsUiState.Success).articles)
        }

    @Test
    fun `cached headlines render immediately, before the network refresh resolves`() = runTest {
        // Simulates the "first response" path: the page was already cached from a
        // previous visit, so it's visible as soon as the source is selected.
        val cacheFlow = MutableStateFlow(listOf(mockArticle))
        every { repository.getSelectedSourceIds() } returns flowOf(setOf("bbc-news"))
        every { repository.observeCachedHeadlines("bbc-news") } returns cacheFlow
        coEvery { repository.refreshHeadlines("bbc-news") } coAnswers {
            delay(10_000) // network hasn't responded yet
            listOf(mockArticle)
        }

        viewModel = newViewModel()

        // Before the (still-pending) network refresh ever resolves, the cached
        // article is already on screen.
        val state = viewModel.uiState.value
        assertTrue(state is NewsUiState.Success)
        assertEquals(listOf(mockArticle), (state as NewsUiState.Success).articles)
    }

    @Test
    fun `filteredHeadlines emits articles once cache is populated`() = runTest {
        val cacheFlow = MutableStateFlow<List<ArticleDto>>(emptyList())
        every { repository.getSelectedSourceIds() } returns flowOf(setOf("bbc-news"))
        every { repository.observeCachedHeadlines("bbc-news") } returns cacheFlow
        coEvery { repository.refreshHeadlines("bbc-news") } coAnswers {
            cacheFlow.value = listOf(mockArticle)
            listOf(mockArticle)
        }

        viewModel = newViewModel()
        advanceUntilIdle()

        viewModel.filteredHeadlines.test {
            assertEquals(listOf(mockArticle), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when API returns empty articles uiState becomes Empty`() = runTest {
        every { repository.getSelectedSourceIds() } returns flowOf(setOf("bbc-news"))
        every { repository.observeCachedHeadlines("bbc-news") } returns flowOf(emptyList())
        coEvery { repository.refreshHeadlines("bbc-news") } returns emptyList()

        viewModel = newViewModel()
        advanceUntilIdle()

        assertEquals(NewsUiState.Empty, viewModel.uiState.value)
    }

    // -------------------------------------------------------------------------
    // Error state
    // -------------------------------------------------------------------------

    @Test
    fun `when refresh throws and there is no cache uiState becomes Error`() = runTest {
        every { repository.getSelectedSourceIds() } returns flowOf(setOf("bbc-news"))
        every { repository.observeCachedHeadlines("bbc-news") } returns flowOf(emptyList())
        coEvery { repository.refreshHeadlines(any()) } throws Exception("Boom")

        viewModel = newViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is NewsUiState.Error)
    }

    @Test
    fun `error message is propagated into Error state`() = runTest {
        val errorMessage = "Something broke"
        every { repository.getSelectedSourceIds() } returns flowOf(setOf("bbc-news"))
        every { repository.observeCachedHeadlines("bbc-news") } returns flowOf(emptyList())
        coEvery { repository.refreshHeadlines(any()) } throws Exception(errorMessage)

        viewModel = newViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value as? NewsUiState.Error
        assertEquals(errorMessage, state?.message)
        assertEquals(false, state?.isOffline)
    }

    @Test
    fun `when offline and there is no cache uiState becomes an offline Error`() = runTest {
        every { connectivityObserver.isConnected } returns flowOf(false)
        every { repository.getSelectedSourceIds() } returns flowOf(setOf("bbc-news"))
        every { repository.observeCachedHeadlines("bbc-news") } returns flowOf(emptyList())

        viewModel = newViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value as? NewsUiState.Error
        assertTrue(state != null && state.isOffline)
        coVerify(exactly = 0) { repository.refreshHeadlines(any()) }
    }

    @Test
    fun `an IOException from the network is treated as an offline error`() = runTest {
        every { repository.getSelectedSourceIds() } returns flowOf(setOf("bbc-news"))
        every { repository.observeCachedHeadlines("bbc-news") } returns flowOf(emptyList())
        coEvery { repository.refreshHeadlines(any()) } throws IOException("timeout")

        viewModel = newViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value as? NewsUiState.Error
        assertTrue(state != null && state.isOffline)
    }

    @Test
    fun `going offline does not clobber headlines already on screen`() = runTest {
        // Cache already has data (e.g. from a previous successful refresh); a later
        // refresh attempt while offline should leave the Success state alone instead
        // of replacing it with an error.
        val cacheFlow = MutableStateFlow(listOf(mockArticle))
        every { connectivityObserver.isConnected } returns flowOf(false)
        every { repository.getSelectedSourceIds() } returns flowOf(setOf("bbc-news"))
        every { repository.observeCachedHeadlines("bbc-news") } returns cacheFlow

        viewModel = newViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is NewsUiState.Success)
        assertEquals(listOf(mockArticle), (state as NewsUiState.Success).articles)
    }

    @Test
    fun `filteredHeadlines emits empty list when uiState is Error`() = runTest {
        every { repository.getSelectedSourceIds() } returns flowOf(setOf("bbc-news"))
        every { repository.observeCachedHeadlines("bbc-news") } returns flowOf(emptyList())
        coEvery { repository.refreshHeadlines(any()) } throws Exception("API Error")

        viewModel = newViewModel()

        viewModel.filteredHeadlines.test {
            assertEquals(emptyList<ArticleDto>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // -------------------------------------------------------------------------
    // refreshHeadlines
    // -------------------------------------------------------------------------

    @Test
    fun `refreshHeadlines re-fetches and updates uiState to Success`() = runTest {
        val cacheFlow = MutableStateFlow<List<ArticleDto>>(emptyList())
        every { repository.getSelectedSourceIds() } returns flowOf(setOf("bbc-news"))
        every { repository.observeCachedHeadlines("bbc-news") } returns cacheFlow
        coEvery { repository.refreshHeadlines("bbc-news") } coAnswers {
            cacheFlow.value = listOf(mockArticle)
            listOf(mockArticle)
        }

        viewModel = newViewModel()
        advanceUntilIdle()

        // Trigger a manual refresh
        viewModel.refreshHeadlines()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is NewsUiState.Success)
        assertEquals(listOf(mockArticle), (state as NewsUiState.Success).articles)
    }

    @Test
    fun `refreshHeadlines does nothing when no source ids are set`() = runTest {
        // currentSourceIds is null (no source selected), so early return expected
        viewModel.refreshHeadlines()
        advanceUntilIdle()

        // Should remain Empty — no network call
        assertEquals(NewsUiState.Empty, viewModel.uiState.value)
        coVerify(exactly = 0) { repository.refreshHeadlines(any()) }
    }

    // -------------------------------------------------------------------------
    // Search / filteredHeadlines
    // -------------------------------------------------------------------------

    @Test
    fun `filteredHeadlines returns all articles when query is blank`() = runTest {
        val articles = listOf(
            mockArticle.copy(title = "Kotlin Tips"),
            mockArticle.copy(title = "Android News")
        )
        every { repository.getSelectedSourceIds() } returns flowOf(setOf("bbc-news"))
        every { repository.observeCachedHeadlines("bbc-news") } returns flowOf(articles)

        viewModel = newViewModel()

        viewModel.filteredHeadlines.test {
            assertEquals(articles, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filteredHeadlines filters by search query case-insensitively`() = runTest {
        val articles = listOf(
            mockArticle.copy(title = "Kotlin Tips"),
            mockArticle.copy(title = "Android News")
        )
        every { repository.getSelectedSourceIds() } returns flowOf(setOf("bbc-news"))
        every { repository.observeCachedHeadlines("bbc-news") } returns flowOf(articles)

        viewModel = newViewModel()
        viewModel.onSearchQueryChange("kotlin")

        viewModel.filteredHeadlines.test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Kotlin Tips", result.first().title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filteredHeadlines returns empty list when query matches nothing`() = runTest {
        val articles = listOf(mockArticle.copy(title = "Kotlin Tips"))
        every { repository.getSelectedSourceIds() } returns flowOf(setOf("bbc-news"))
        every { repository.observeCachedHeadlines("bbc-news") } returns flowOf(articles)

        viewModel = newViewModel()
        viewModel.onSearchQueryChange("swift")

        viewModel.filteredHeadlines.test {
            assertEquals(emptyList<ArticleDto>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearing search query restores full article list`() = runTest {
        val articles = listOf(
            mockArticle.copy(title = "Kotlin Tips"),
            mockArticle.copy(title = "Android News")
        )
        every { repository.getSelectedSourceIds() } returns flowOf(setOf("bbc-news"))
        every { repository.observeCachedHeadlines("bbc-news") } returns flowOf(articles)

        viewModel = newViewModel()
        viewModel.onSearchQueryChange("kotlin")
        viewModel.onSearchQueryChange("") // clear

        viewModel.filteredHeadlines.test {
            assertEquals(articles, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // -------------------------------------------------------------------------
    // saveArticle
    // -------------------------------------------------------------------------

    @Test
    fun `saveArticle calls repository saveArticle with correct entity fields`() = runTest {
        coEvery { repository.saveArticle(any()) } returns Unit

        viewModel.saveArticle(mockArticle)
        advanceUntilIdle()

        coVerify {
            repository.saveArticle(match { entity ->
                entity.url == mockArticle.url && entity.title == mockArticle.title
            })
        }
    }

    // -------------------------------------------------------------------------
    // toggleSave
    // -------------------------------------------------------------------------

    @Test
    fun `toggleSave with isSaved=true calls deleteArticle not saveArticle`() = runTest {
        coEvery { repository.deleteArticle(any()) } returns Unit

        viewModel.toggleSave(mockArticle, isSaved = true)
        advanceUntilIdle()

        coVerify { repository.deleteArticle(match { it.url == mockArticle.url }) }
        coVerify(exactly = 0) { repository.saveArticle(any()) }
    }

    @Test
    fun `toggleSave with isSaved=false calls saveArticle not deleteArticle`() = runTest {
        coEvery { repository.saveArticle(any()) } returns Unit

        viewModel.toggleSave(mockArticle, isSaved = false)
        advanceUntilIdle()

        coVerify { repository.saveArticle(match { it.url == mockArticle.url }) }
        coVerify(exactly = 0) { repository.deleteArticle(any()) }
    }

    @Test
    fun `toggleSave passes all fields correctly to saveArticle`() = runTest {
        coEvery { repository.saveArticle(any()) } returns Unit

        viewModel.toggleSave(mockArticle, isSaved = false)
        advanceUntilIdle()

        coVerify {
            repository.saveArticle(match { entity ->
                entity.url == mockArticle.url &&
                        entity.title == mockArticle.title &&
                        entity.description == mockArticle.description &&
                        entity.author == mockArticle.author &&
                        entity.urlToImage == mockArticle.urlToImage &&
                        entity.publishedAt == mockArticle.publishedAt
            })
        }
    }

    // -------------------------------------------------------------------------
    // savedArticleUrls
    // -------------------------------------------------------------------------

    @Test
    fun `savedArticleUrls emits set of urls from saved articles`() = runTest {
        val savedEntities = listOf(
            ArticleEntity(
                url = "https://example.com",
                title = "Title",
                description = null,
                author = null,
                urlToImage = null,
                publishedAt = ""
            )
        )
        every { repository.getSavedArticles() } returns flowOf(savedEntities)
        viewModel = newViewModel()

        viewModel.savedArticleUrls.test {
            assertEquals(setOf("https://example.com"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `savedArticleUrls emits empty set when no articles are saved`() = runTest {
        every { repository.getSavedArticles() } returns flowOf(emptyList())
        viewModel = newViewModel()

        viewModel.savedArticleUrls.test {
            assertEquals(emptySet<String>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // -------------------------------------------------------------------------
    // Source switching
    // -------------------------------------------------------------------------

    @Test
    fun `switching source ids triggers a new fetch`() = runTest {
        val sourceIdsFlow = MutableSharedFlow<Set<String>>()
        val cacheFlowA = MutableStateFlow<List<ArticleDto>>(emptyList())
        val cacheFlowB = MutableStateFlow<List<ArticleDto>>(emptyList())
        val articlesA = listOf(mockArticle.copy(title = "Source A"))
        val articlesB = listOf(mockArticle.copy(title = "Source B"))

        every { repository.getSelectedSourceIds() } returns sourceIdsFlow
        every { repository.observeCachedHeadlines("source-a") } returns cacheFlowA
        every { repository.observeCachedHeadlines("source-b") } returns cacheFlowB
        coEvery { repository.refreshHeadlines("source-a") } coAnswers {
            cacheFlowA.value = articlesA
            articlesA
        }
        coEvery { repository.refreshHeadlines("source-b") } coAnswers {
            cacheFlowB.value = articlesB
            articlesB
        }

        val vm = newViewModel()
        sourceIdsFlow.emit(setOf("source-a"))
        advanceUntilIdle()

        val firstState = vm.uiState.value as NewsUiState.Success
        assertEquals(articlesA, firstState.articles)

        sourceIdsFlow.emit(setOf("source-b"))
        advanceUntilIdle()

        val secondState = vm.uiState.value as NewsUiState.Success
        assertEquals(articlesB, secondState.articles)
    }

    @Test
    fun `deselecting all sources resets uiState to Empty`() = runTest {
        val sourceIdsFlow = MutableSharedFlow<Set<String>>()
        val cacheFlow = MutableStateFlow<List<ArticleDto>>(emptyList())
        every { repository.getSelectedSourceIds() } returns sourceIdsFlow
        every { repository.observeCachedHeadlines("bbc-news") } returns cacheFlow
        coEvery { repository.refreshHeadlines("bbc-news") } coAnswers {
            cacheFlow.value = listOf(mockArticle)
            listOf(mockArticle)
        }

        val vm = newViewModel()
        sourceIdsFlow.emit(setOf("bbc-news"))
        advanceUntilIdle()
        assertTrue(vm.uiState.value is NewsUiState.Success)

        sourceIdsFlow.emit(emptySet())
        advanceUntilIdle()
        assertEquals(NewsUiState.Empty, vm.uiState.value)
    }
}

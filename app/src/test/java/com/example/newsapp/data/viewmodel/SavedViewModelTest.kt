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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SavedViewModelTest {

    private val repository: NewsRepository = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    // We use a StateFlow to push updates into the ViewModel's stateIn operator
    private val savedArticlesFlow = MutableStateFlow<List<ArticleEntity>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Mock the repository to return our flow before the ViewModel is initialized
        every { repository.getSavedArticles() } returns savedArticlesFlow
    }

    @Test
    fun `savedArticles stateIn should reflect repository updates`() = runTest {
        val viewModel = SavedViewModel(repository)
        val mockArticle = ArticleEntity("url.com", "Title", "Desc", "Author", "img.jpg", "2024")

        viewModel.savedArticles.test {
            // Initial emission from stateIn(emptyList())
            assertEquals(emptyList<ArticleEntity>(), awaitItem())

            // Emit new data from repository
            savedArticlesFlow.value = listOf(mockArticle)

            // Verify the StateFlow caught the update
            assertEquals(listOf(mockArticle), awaitItem())
        }
    }

    @Test
    fun `deleteArticle should call repository delete`() = runTest {
        val viewModel = SavedViewModel(repository)
        val article = ArticleEntity("url.com", "Title", "Desc", "Author", "img.jpg", "2024")
        coEvery { repository.deleteArticle(any()) } returns Unit

        viewModel.deleteArticle(article)

        coVerify { repository.deleteArticle(article) }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
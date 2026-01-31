package com.example.newsapp.data.viewmodel

import com.example.newsapp.data.remote.models.SourceResponse
import com.example.newsapp.data.repository.NewsRepository
import com.example.newsapp.viewmodel.SourcesViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

class SourcesViewModelTest {
    private val repository: NewsRepository = mockk()
    private lateinit var viewModel: SourcesViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getSources() } returns SourceResponse("ok", emptyList())
        every { repository.getSelectedSourceIds() } returns flowOf(emptySet())

        viewModel = SourcesViewModel(repository)
    }

    @Test
    fun `onSourceToggled calls repository to save new selection`() = runTest {
        // Arrange
        val sourceId = "techcrunch"
        val currentSelection = setOf<String>()
        coEvery { repository.saveSelectedSources(any()) } returns Unit

        // Act
        viewModel.onSourceToggled(sourceId)

        // Assert
        coVerify { repository.saveSelectedSources(setOf(sourceId)) }
    }
}
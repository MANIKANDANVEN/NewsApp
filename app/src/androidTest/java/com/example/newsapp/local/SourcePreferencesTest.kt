package com.example.newsapp.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.newsapp.data.local.SourcePreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SourcePreferencesTest {

    private lateinit var sourcePreferences: SourcePreferences
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setup() {
        sourcePreferences = SourcePreferences(context)
    }

    @Test
    fun saveAndReadSources() = runTest {
        val testSources = setOf("bbc-news", "cnn", "techcrunch")

        // Act
        sourcePreferences.saveSources(testSources)

        // Assert
        val result = sourcePreferences.selectedSources.first()
        assertEquals(testSources, result)
    }
}
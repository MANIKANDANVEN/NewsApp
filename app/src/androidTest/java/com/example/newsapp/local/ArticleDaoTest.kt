package com.example.newsapp.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.newsapp.data.local.AppDatabase
import com.example.newsapp.data.local.ArticleDao
import com.example.newsapp.data.local.ArticleEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArticleDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ArticleDao

    @Before
    fun setup() {
        // Create an in-memory version of the database
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.articleDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveArticle_and_read_in_flow() = runBlocking {
        // Arrange
        val article = ArticleEntity(
            url = "https://test.com",
            title = "Test News",
            description = "Description",
            author = "Author",
            urlToImage = null,
            publishedAt = "2026-01-31"
        )

        // Act
        dao.saveArticle(article)

        // Assert
        val result = dao.getAllSavedArticles().first() // Get the first emission from Flow
        assertEquals(1, result.size)
        assertEquals("Test News", result[0].title)
    }

    @Test
    fun deleteArticle_removes_from_db() = runBlocking {
        // Arrange
        val article = ArticleEntity("url", "Title", null, null, null, "date")
        dao.saveArticle(article)

        // Act
        dao.deleteArticle(article)

        // Assert
        val result = dao.getAllSavedArticles().first()
        assertTrue(result.isEmpty())
    }
}
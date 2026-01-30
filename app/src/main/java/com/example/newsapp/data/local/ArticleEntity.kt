package com.example.newsapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_articles")
data class ArticleEntity(
    @PrimaryKey val url: String, // Use URL as unique ID
    val title: String,
    val description: String?,
    val author: String?,
    val urlToImage: String?,
    val publishedAt: String
)
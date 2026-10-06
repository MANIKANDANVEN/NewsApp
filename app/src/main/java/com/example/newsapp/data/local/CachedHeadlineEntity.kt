package com.example.newsapp.data.local

import androidx.room.Entity

// sourceIds is the comma-joined key the headlines were fetched for (same value
// NewsRepository builds from the user's selected sources), so cached pages for
// different source selections don't collide.
@Entity(tableName = "cached_headlines", primaryKeys = ["url", "sourceIds"])
data class CachedHeadlineEntity(
    val url: String,
    val sourceIds: String,
    val title: String,
    val description: String?,
    val author: String?,
    val urlToImage: String?,
    val publishedAt: String
)

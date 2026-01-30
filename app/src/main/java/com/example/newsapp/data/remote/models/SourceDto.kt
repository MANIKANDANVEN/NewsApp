package com.example.newsapp.data.remote.models

// The wrapper for the sources list
data class SourceResponse(
    val status: String,
    val sources: List<SourceDto>
)

// Individual source details
data class SourceDto(
    val id: String,          // e.g., "bbc-news"
    val name: String,        // e.g., "BBC News"
    val description: String?,
    val url: String?,
    val category: String?,
    val language: String?,   // We will filter for "en"
    val country: String?
)
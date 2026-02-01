package com.example.newsapp.data.remote.models

data class SourceResponse(
    val status: String,
    val sources: List<SourceDto>
)

data class SourceDto(
    val id: String,
    val name: String,
    val description: String?,
    val url: String?,
    val category: String?,
    val language: String?,   // We will filter for "en"
    val country: String?
)
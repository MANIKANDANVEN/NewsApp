package com.example.newsapp.data.remote.models

// The wrapper for the sources list
data class SourceDto(
    val id: String,
    val name: String,
    val description: String?
)

// Individual source details
data class SourceResponse(val sources: List<SourceDto>)
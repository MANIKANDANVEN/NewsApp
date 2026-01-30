package com.example.newsapp.data.remote.models

data class ArticleDto(
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String
)

data class NewsResponse(val articles: List<ArticleDto>)
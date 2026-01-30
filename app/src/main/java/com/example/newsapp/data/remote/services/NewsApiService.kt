package com.example.newsapp.data.remote.services

import com.example.newsapp.data.remote.models.NewsResponse
import com.example.newsapp.data.remote.models.SourceResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("v2/top-headlines")
    suspend fun getHeadlines(
        @Query("sources") sources: String,
        @Query("apiKey") apiKey: String
    ): NewsResponse

    @GET("v2/top-headlines/sources")
    suspend fun getSources(
        @Query("language") language: String = "en",
        @Query("apiKey") apiKey: String
    ): SourceResponse
}
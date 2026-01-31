package com.example.newsapp.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface SearchDelegate {
    val searchQuery: StateFlow<String>
    fun onSearchQueryChange(newQuery: String)
}

class SearchDelegateImpl : SearchDelegate {
    private val _searchQuery = MutableStateFlow("")
    override val searchQuery = _searchQuery.asStateFlow()

    override fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }
}
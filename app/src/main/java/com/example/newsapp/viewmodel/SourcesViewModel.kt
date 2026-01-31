package com.example.newsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.data.remote.models.SourceDto
import com.example.newsapp.data.repository.NewsRepository
import com.example.newsapp.utils.SearchDelegate
import com.example.newsapp.utils.SearchDelegateImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SourcesViewModel @Inject constructor(
    private val repo: NewsRepository
) : ViewModel(), SearchDelegate by SearchDelegateImpl() {

    private val _sourcesState = MutableStateFlow<SourcesUiState>(SourcesUiState.Loading)
    val sourcesState = _sourcesState.asStateFlow()

    val filteredSources = combine(sourcesState, searchQuery) { state, query ->
        if (state is SourcesUiState.Success) {
            if (query.isBlank()) state.sources
            else state.sources.filter { it.name.contains(query, ignoreCase = true) }
        } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedSourceIds = repo.getSelectedSourceIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        loadSources()
    }

    fun loadSources() {
        viewModelScope.launch {
            _sourcesState.value = SourcesUiState.Loading
            try {
                val result = repo.getSources() // Make sure your repo returns SourceResponse
                if (result.sources.isNotEmpty()) {
                    _sourcesState.value = SourcesUiState.Success(result.sources)
                } else {
                    _sourcesState.value = SourcesUiState.Error("No English sources found.")
                }
            } catch (e: Exception) {
                _sourcesState.value = SourcesUiState.Error(e.localizedMessage ?: "Unknown Error")
            }
        }
    }

    fun onSourceToggled(sourceId: String) {
        viewModelScope.launch {
            repo.saveSelectedSources(selectedSourceIds.value.toMutableSet().apply {
                if (contains(sourceId)) remove(sourceId) else add(sourceId)
            })
        }
    }
}

sealed class SourcesUiState {
    object Loading : SourcesUiState()
    data class Success(val sources: List<SourceDto>) : SourcesUiState()
    data class Error(val message: String) : SourcesUiState()
}
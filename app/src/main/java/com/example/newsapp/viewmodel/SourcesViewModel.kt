package com.example.newsapp.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.newsapp.data.remote.models.SourceDto
import com.example.newsapp.data.repository.NewsRepository
import com.example.newsapp.viewmodel.base.BaseViewModel
import com.example.newsapp.delegate.SearchDelegate
import com.example.newsapp.delegate.SearchDelegateImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SourcesViewModel @Inject constructor(
    private val repo: NewsRepository
) : BaseViewModel(), SearchDelegate by SearchDelegateImpl() {

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
        // Use the generic safeLaunch for UI state updates
        safeLaunch(
            stateFlow = _sourcesState,
            loadingState = SourcesUiState.Loading,
            errorState = { SourcesUiState.Error(it) }
        ) {
            val result = repo.getSources()
            if (result.sources.isNotEmpty()) {
                _sourcesState.value = SourcesUiState.Success(result.sources)
            } else {
                _sourcesState.value = SourcesUiState.Error("No English sources found.")
            }
        }
    }

    fun onSourceToggled(sourceId: String) {
        // Use simple safeLaunch for fire-and-forget DB operations
        safeLaunch {
            val currentSet = selectedSourceIds.value.toMutableSet()
            if (currentSet.contains(sourceId)) {
                currentSet.remove(sourceId)
            } else {
                currentSet.add(sourceId)
            }
            repo.saveSelectedSources(currentSet)
        }
    }
}

sealed class SourcesUiState {
    object Loading : SourcesUiState()
    data class Success(val sources: List<SourceDto>) : SourcesUiState()
    data class Error(val message: String) : SourcesUiState()
}
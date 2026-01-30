package com.example.newsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.data.remote.models.SourceDto
import com.example.newsapp.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SourcesViewModel @Inject constructor(
    private val repo: NewsRepository
) : ViewModel() {

    // List of all available sources from the API
    private val _availableSources = MutableStateFlow<List<SourceDto>>(emptyList())
    val availableSources = _availableSources.asStateFlow()

    // Set of IDs currently selected by the user
    val selectedSourceIds = repo.getSelectedSourceIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        loadSources()
    }

    private fun loadSources() {
        viewModelScope.launch {
            repo.fetchAvailableSources().onSuccess {
                _availableSources.value = it
            }
        }
    }

    fun onSourceToggled(sourceId: String) {
        viewModelScope.launch {
            repo.toggleSourceSelection(sourceId, selectedSourceIds.value)
        }
    }
}
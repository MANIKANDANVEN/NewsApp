package com.example.newsapp.viewmodel

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.newsapp.R
import com.example.newsapp.connectivity.ConnectivityObserver
import com.example.newsapp.data.local.ArticleEntity
import com.example.newsapp.data.remote.models.ArticleDto
import com.example.newsapp.data.repository.NewsRepository
import com.example.newsapp.viewmodel.base.BaseViewModel
import com.example.newsapp.delegate.SearchDelegate
import com.example.newsapp.delegate.SearchDelegateImpl
import com.example.newsapp.viewmodel.state.NewsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repo: NewsRepository,
    private val connectivityObserver: ConnectivityObserver,
    @ApplicationContext private val appContext: Context
) : BaseViewModel(appContext), SearchDelegate by SearchDelegateImpl() {

    private val _uiState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    val filteredHeadlines = combine(uiState, searchQuery) { state, query ->
        if (state is NewsUiState.Success) {
            if (query.isBlank()) state.articles
            else state.articles.filter { it.title.contains(query, ignoreCase = true) }
        } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Keep track of IDs for the refresh action
    private var currentSourceIds: String? = null

    // Observes the Room cache for the current source selection so cached headlines
    // render instantly; cancelled/restarted whenever the selection changes.
    private var cacheObserverJob: Job? = null

    init {
        observeSourceChanges()
    }

    private fun observeSourceChanges() {
        // Use the simple safeLaunch for background flow observation
        safeLaunch {
            repo.getSelectedSourceIds().collect { ids ->
                cacheObserverJob?.cancel()
                if (ids.isEmpty()) {
                    currentSourceIds = null
                    _uiState.value = NewsUiState.Empty
                } else {
                    currentSourceIds = ids.joinToString(",")
                    loadHeadlines(currentSourceIds!!)
                }
            }
        }
    }

    // Cache-first: render whatever is already on disk immediately (the "first
    // response"), then kick off a network refresh. The cache flow keeps collecting
    // afterwards, so a successful refresh updates the UI as soon as it lands.
    private fun loadHeadlines(ids: String) {
        cacheObserverJob = viewModelScope.launch {
            repo.observeCachedHeadlines(ids).collect { cached ->
                if (cached.isNotEmpty()) {
                    _uiState.value = NewsUiState.Success(cached)
                } else if (_uiState.value !is NewsUiState.Success) {
                    _uiState.value = NewsUiState.Loading
                }
            }
        }
        refreshHeadlines()
    }

    //function for Pull-to-Refresh and Retry Button
    fun refreshHeadlines() {
        val ids = currentSourceIds ?: return
        viewModelScope.launch {
            if (!connectivityObserver.isConnected.first()) {
                // No network: if we already have cached headlines on screen, leave them
                // be. Only surface an error when there is nothing to show at all.
                if (_uiState.value !is NewsUiState.Success) {
                    _uiState.value = NewsUiState.Error(
                        appContext.getString(R.string.NewsViewModel_noConnection),
                        isOffline = true
                    )
                }
                return@launch
            }

            try {
                val articles = repo.refreshHeadlines(ids)
                if (articles.isEmpty()) {
                    _uiState.value = NewsUiState.Empty
                }
                // Non-empty results flow back into uiState via the cache observer above.
            } catch (e: IOException) {
                // Network dropped mid-request (timeout, DNS, connection reset, ...).
                if (_uiState.value !is NewsUiState.Success) {
                    _uiState.value = NewsUiState.Error(
                        appContext.getString(R.string.NewsViewModel_noConnection),
                        isOffline = true
                    )
                }
            } catch (e: Exception) {
                if (_uiState.value !is NewsUiState.Success) {
                    val message = e.localizedMessage
                        ?: appContext.getString(R.string.BaseViewModel_unknownError)
                    _uiState.value = NewsUiState.Error(message)
                }
            }
        }
    }

    fun saveArticle(dto: ArticleDto) {
        safeLaunch {
            repo.saveArticle(dto.toEntity())
        }
    }

    fun ArticleDto.toEntity() = ArticleEntity(
        url = url, title = title, description = description,
        author = author, urlToImage = urlToImage, publishedAt = publishedAt
    )

    // Add this inside NewsViewModel
    val savedArticleUrls: StateFlow<Set<String>> = repo.getSavedArticles()
        .map { articles -> articles.map { it.url }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun toggleSave(dto: ArticleDto, isSaved: Boolean) {
        viewModelScope.launch {
            if (isSaved) {
                // If already saved, delete it (Pass a dummy entity with the same URL)
                repo.deleteArticle(
                    ArticleEntity(
                        url = dto.url,
                        title = dto.title,
                        description = null,
                        author = null,
                        urlToImage = null,
                        publishedAt = ""
                    )
                )
            } else {
                // If not saved, add it
                repo.saveArticle(
                    ArticleEntity(
                        dto.url,
                        dto.title,
                        dto.description,
                        dto.author,
                        dto.urlToImage,
                        dto.publishedAt
                    )
                )
            }
        }
    }
}
